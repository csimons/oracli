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

;; Prints an error message and exits.
;;
(defn eprint [message]
  (println (str "oracli: " message))
  (System/exit 1))

;; Attempt to load the project path from the environment.
;;
(let [sys-env     (System/getenv)
      project-dir (.get sys-env "ORACLI_HOME")]
  (if (= nil project-dir)
    (eprint "ORACLI_HOME environment variable must be set.")
    (if (not (.endsWith project-dir "/"))
      (let [project-dir (str project-dir "/")]
        (def project-dir project-dir)))))

;; Bootstrap the rest of the application.
;;
(load-file (str project-dir "src/init-env.clj"))
(load-file (str project-dir "src/load-validate-config.clj"))
(load-file (str project-dir "src/db.clj"))
(load-file (str project-dir "src/shell.clj"))
(load-file (str project-dir "src/dispatch.clj"))
(load-file (str project-dir "src/command-loop.clj"))
