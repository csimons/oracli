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

;; Load JDBC drivers.
;;
(clojure.lang.RT/loadClassForName "oracle.jdbc.OracleDriver")

;; Check for USERNAME environment variable.  This is used when
;; establishing Oracle DB connections so that Oracle DBA tools
;; will work.
;;
(let [sys-env (System/getenv)]
  (def osuser (.get sys-env "USER"))
  (if (= nil osuser)
    (def osuser (.get sys-env "USERNAME")))
  (if (= nil osuser)
    (eprint "USER or USERNAME environment variable must be set.")))

;; Attempt to parse the required command-line arguments.
;;
(try
  (def config-file (nth *command-line-args* 0))
  (def db-name     (nth *command-line-args* 1))
  (catch IndexOutOfBoundsException e
    (eprint "usage: oracli <db-name>")))
