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

;; Add a shell variable to the symbol table.
;;
(defn set-symbol [token-string env]
  (let [symbol-name (.substring token-string
                                0
                                (.indexOf token-string " "))
        symbol-val  (.substring token-string
                                (+ 1 (.indexOf token-string " "))
                                (.length token-string))]
    { :conn    (:conn env)
      :symbols (assoc (:symbols env) symbol-name (.trim symbol-val))
      :buffer  (:buffer env)
      :lc      (:lc env) }))

;; Remove a shell variable from the symbol table.
;;
(defn unset-symbol [symbol-name env]
  { :conn    (:conn env)
    :symbols (dissoc (:symbols env) symbol-name)
    :buffer  (:buffer env)
    :lc      (:lc     env)})

;; Print symbol table.
;;
(defn print-symbols [env]
  (let [s (:symbols env)]
    (if (not (= nil s))
      (do
        (print "\nSYMBOL\tVALUE\n------\t-----\n")
        (doseq [kv s]
          (print (format "[%s]\t[%s]\n" (key kv) (val kv))))
        (print \newline))))
  env)

;; Print the text buffer.
;;
(defn print-buffer [env]
  (print (:buffer env))
  env)

;; Save the buffer to a file.
;;
(defn save-buffer [filename env]
  (with-open [w (clojure.java.io/writer filename)]
    (let [pw (java.io.PrintWriter. w)]
      (.print pw (:buffer env))
      (.close pw)))
  env)

;; Replace the buffer with file contents.
;;
(defn load-buffer [filename print-buffer env]
  (with-open [r (clojure.java.io/reader filename)]
    (loop [new-buffer ""
           new-lc     0]
      (let [next-line (.readLine r)]
        (if (= nil next-line)
          (do
            (if print-buffer
              (print new-buffer))
            { :conn    (:conn env)
              :symbols (:symbols env)
              :buffer  new-buffer
              :lc      new-lc })
          (recur (str new-buffer next-line \newline) (+ 1 new-lc)))))))

;; Attempt to execute the buffer.
;;
(defn execute [cmd env]
  (let [start-time (System/currentTimeMillis)
        x (.toLowerCase (.trim cmd))]
    (print \newline)
    (cond
      (.startsWith x "select")
      (execute-query cmd env start-time)

      (or
        (.startsWith x "update")
        (.startsWith x "insert")
        (.startsWith x "delete")
        (.startsWith x "create")
        (.startsWith x "drop")
        (.startsWith x "declare")
        (.startsWith x "begin"))
      (execute-update cmd env start-time)

      :default
      (do
        (println "Don't know how to execute this.")
        env))))

;; Attempt to execute an instance of the text buffer with
;; all shell variables bound.
;;
(defn bind-execute [env]
  (let [cmd (:buffer env)
        p   (java.util.regex.Pattern/compile "&\\w+")
        m   (.matcher p cmd)]
    (loop [matched   (vector)
           unmatched (vector)]
      (if (.find m)
        (let [s (.substring cmd (+ 1 (.start m)) (.end m))]
          (if (= nil (get (:symbols env) s))
            (recur matched (conj unmatched s))
            (recur (conj matched s) unmatched)))
        (if (not (empty? unmatched))
          (do
            (println (str "Unbound variables in command: " unmatched))
            env)
          (loop [bound-cmd cmd
                 unbound   matched]
            (if (not (empty? unbound))
              (let [i (first unbound)]
                (recur (.replaceAll bound-cmd
                                    (str "&" i #"(\W)")
                                    (str (get (:symbols env) i) "$1"))
                       (rest unbound)))
              (execute bound-cmd env))))))))
