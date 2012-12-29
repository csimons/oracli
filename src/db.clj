;; Copyright 2011-2012 Christopher L. Simons
;;
;; Permission is hereby granted, free of charge, to any person obtaining a
;; copy of this software and associated documentation files (the "Software"),
;; to deal in the Software without restriction, including without limitation
;; the rights to use, copy, modify, merge, publish, distribute, sublicense,
;; and/or sell copies of the Software, and to permit persons to whom the
;; Software is furnished to do so, subject to the following conditions:
;;
;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.
;;
;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
;; THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
;; FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
;; DEALINGS IN THE SOFTWARE.

(ns oracli.core)

;; Return time elapsed since given time.
;;
(defn elapsed [start-time-millis]
  (/ (- (System/currentTimeMillis)
        start-time-millis)
     1000.0))

;; If first argument is nil, returns second argument.
;; Otherwise, returns first argument.
;;
(defn nvl [a b]
  (if (= nil a) b a))

;; Returns a vector of column names.
;;
(defn get-column-names [rs]
  (loop [col-names []
         meta-data (.getMetaData rs)]
    (if (> (.getColumnCount meta-data) (count col-names))
      (recur (conj col-names (.getColumnName meta-data
                                             (inc (count col-names))))
             meta-data)
      col-names)))

(defn print-grid [col-widths col-names col-data]
  (let [cols  (count col-widths)
        h-bar (fn []
                (loop [i 0]
                  (print "+")
                  (dotimes [j (+ 2 (get col-widths (inc i)))]
                    (print "-"))
                  (if (< (inc i) cols)
                    (recur (inc i))))
                (println "+"))]
    (h-bar)
    (print "|")
    (dotimes [i cols]
      (let [data (nth col-names i)]
        (print (str " " data " "))
        (dotimes [k (- (get col-widths (inc i)) (count data))]
          (print " "))
        (print "|")))
    (print \newline)
    (h-bar)
    (dotimes [i (count col-data)]
      ;; For each row:
      (print "|")
      (dotimes [j cols]
        ;; For each column:
        (let [data (nth (get col-data i) j)]
          (print (str " " data " "))
          (dotimes [k (- (get col-widths (inc j)) (count data))]
            (print " "))
          (print "|")))
      (print \newline))
    (h-bar)))

;; Prints result set within a grid, expanding grid columns to accomodate
;; the maximum length of the column's name and data:
;;
;; +-------+---------------------------+--------------+
;; | COL_A | COL_B                     | THIS_ONE_TOO |
;; +-------+---------------------------+--------------+
;; | foo   | This column has expanded. | bar          |
;; +-------+---------------------------+--------------+
;;
(defn pretty-print-result-set [rs]
  (let [col-names (get-column-names rs)]
    (loop [col-widths   {}
           col-data     {}
           another-row? (.next rs)]
      (if (not another-row?)
        ;; No more rows to process, so print if results were found.
        (if (> (count col-data) 0)
          (do
            (print-grid col-widths col-names col-data)
            (print (format "\n%d row(s) returned" (count col-data))))
          (print "No rows returned"))
        ;; There is another row to process.
        (let [last-row-processed (dec (count col-data))
              last-row-columns   (count (get col-data last-row-processed))
              last-row-full?     (or (< last-row-processed 0)
                                     (= last-row-columns (count col-names)))
              idx      (if last-row-full? 1 (inc last-row-columns))
              data     (.getString rs idx)
              row-data (if last-row-full?
                         []
                         (nvl (get col-data last-row-processed) []))]
          (recur (assoc col-widths
                        idx
                        (max (if (> idx (count col-widths))
                               0
                               (get col-widths idx))
                             (if (> idx (count col-names))
                               0
                               (count (nth col-names (dec idx))))
                             (count data)))
                 (assoc col-data
                        (if last-row-full?
                          (inc last-row-processed)
                          last-row-processed)
                        (conj row-data data))
                 (if (= (inc (count row-data)) (count col-names))
                   (.next rs)
                   another-row?)))))))

;; Prints column names and data, with each column separated by a tab.
;;
(defn ugly-print-result-set [rs]
  (let [meta-data (.getMetaData rs)]
    (loop [row-count 0]
      (if (not (.next rs))
        (do
          (if (> row-count 0)
            (print \newline))
          (print (format "%d row(s) returned" row-count)))
        (do
          (if (= row-count 0)
            (loop [col 1]
              (if (<= col (.getColumnCount meta-data))
                (do
                  (print (str (.getColumnName meta-data col) \tab))
                  (if (= col (.getColumnCount meta-data))
                    (print \newline))
                  (recur (+ 1 col))))))
          (loop [col 1]
            (if (<= col (.getColumnCount meta-data))
              (do
                (print (str (.getString rs col) \tab))
                (recur (+ 1 col)))))
          (print \newline)
          (recur (+ 1 row-count)))))))

;; Prints a database query result set.
;;
(defn print-result-set [rs]
  #_(ugly-print-result-set rs)
  (pretty-print-result-set rs))

;; Executes a database query (SELECT).
;;
(defn execute-query [cmd env start-time]
  (let [sth (.createStatement (:conn env))
        rs  (.executeQuery sth cmd)]
    (print-result-set rs)
    (println (format "; %.3fs elapsed.\n"
                     (elapsed start-time)))
    (.close rs)
    (.close sth)
    env))

;; Executes database-altering statements/commands, which include
;; UPDATE, DELETE, INSERT, etc., and DDL and PL/SQL statements.
;;
(defn execute-update [cmd env start-time]
  (let [sth      (.createStatement (:conn env))
        affected (.executeUpdate sth cmd)]
    (println (format "%d row(s) affected; %.3fs elapsed.\n"
                     affected
                     (elapsed start-time)))
    (.close sth)
  env))
