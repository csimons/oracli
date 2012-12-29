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

;; Executes a shell command.
;;
(defn shell-command [cmd env]
  (cond
    (= cmd "/")         (bind-execute env)
    (= cmd "/exit")     (System/exit 0)
    (= cmd "/quit")     (System/exit 0)
    (= cmd "/set")      (print-symbols env)
    (= cmd "/print")    (print-buffer env)
    (= cmd "/rollback") (do (.rollback (:conn env)) env)
    (= cmd "/commit")   (do (.commit   (:conn env)) env)
    (= cmd "/help")     (do (print (str \newline
                                        (slurp (str project-dir
                                                    "src/help-msg.dat"))
                                        \newline))
                          env)
    (= cmd "/clear")    {:conn    (:conn env)
                         :symbols (:symbols env)
                         :buffer  ""
                         :lc      1}
    (.startsWith cmd "/set ")     (set-symbol (.substring cmd 5) env)
    (.startsWith cmd "/unset ")   (unset-symbol (.substring cmd 7) env)
    (.startsWith cmd "/save ")    (save-buffer (.substring cmd 6) env)
    (.startsWith cmd "/load ")    (try
                                    (load-buffer
                                      (.substring cmd 6) true env)
                                    (catch Exception e
                                      (println
                                        (str "File not found: "
                                             (.substring cmd 6)))
                                      env))
    (.startsWith cmd "/__load ")  ; Not catching here as dispatch needs
                                  ; know about exceptions to prevent execution
                                  ; on load-exec operations.
                                  (load-buffer
                                    (.substring cmd 8) false env)
    :default        (do
                      (println (str
                                 "Unrecognized command.  "
                                 "See /help for usage."))
                      env)))

;; Dispatches commands requiring less trivial logic.
;;
(defn dispatch [cmd env]
  (cond
    (.startsWith cmd "/")
    (try
      (shell-command cmd env)
      (catch Exception e
        (print (str
                 "\nCaught exception: "
                 (.getClass e) ": "
                 (.getMessage e) "\n\n"))
        env))

    (.startsWith cmd "@")
    (let [filename (str (.trim (.substring cmd 1)) ".sql")]
      (try
        (let [env (shell-command (str "/__load " filename) env)]
          (dispatch "/" env))
        (catch Exception e
          (println (str "File not found: " filename))
          env)))

    :default
    { :conn    (:conn env)
      :symbols (:symbols env)
      :buffer  (str (:buffer env) cmd "\n")
      :lc      (+ 1 (:lc env)) }))
