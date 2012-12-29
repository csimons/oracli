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

;; User interface for the shell.
;;
(let [host     (:host db-conf)
      port     (:port db-conf)
      sid      (:sid  db-conf)
      user     (:user db-conf)
      pass     (:pass db-conf)
      db-props (java.util.Properties.)
      url      (cond (= (:type db-conf) :oracle)
                     (str "jdbc:oracle:thin:@" host ":" port ":" sid)

                     (= (:type db-conf :custom))
                     (:url db-conf))]
  (try
    (let [conn (cond
                 (= :oracle (:type db-conf))
                 (do
                   (.setProperty db-props "user" user)
                   (.setProperty db-props "password" pass)
                   (.setProperty db-props "v$session.osuser" osuser)
                   (.setProperty db-props "v$session.program" "oracli")
                   (java.sql.DriverManager/getConnection url db-props))

                 :default
                 (java.sql.DriverManager/getConnection url user pass))]
      (.setAutoCommit conn false)
      (loop [env {:conn conn :symbols nil :buffer "" :lc 1}]
        (print (format (str db-name ":"
                            (:line-num-fmt config-map)
                            (:prompt-char  config-map) " ") (:lc env)))
        (flush)
        (recur (dispatch (.trim (read-line)) env))))
    (catch Exception e
      (eprint (str "Could not connect to DB: " (.getMessage e))))))
