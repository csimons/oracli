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

;; Load and validate the external configuration file.
;;
(let [corrupt #(eprint "Configuration file is corrupt.")]
  (try
    (def config-map (read-string (slurp config-file)))
    (catch Exception e (corrupt)))

  (def db-conf (get (:data-sources config-map) db-name))

  (if (= nil db-conf)
    (eprint (str "No configuration defined for database '" db-name "'.")))

  (if (or (= nil (:line-num-fmt config-map))
          (= nil (:prompt-char config-map)))
    (corrupt))

  (cond
    (= (:type db-conf) nil)     (corrupt)
    (= (:type db-conf) :custom) (if
                                  (= nil (:url db-conf))
                                  (corrupt))
    :default (if (not (some #{(:type db-conf)} [:oracle :custom]))
              (eprint (str "Unrecognized database type '"
                           (:type db-conf) "'."))
              (if (or (= nil (:host db-conf))
                      (= nil (:port db-conf))
                      (= nil (:sid  db-conf))
                      (= nil (:user db-conf))
                      (= nil (:pass db-conf)))
                (corrupt)))))
