#!/usr/bin/guile \
-L . -e main -s
!#
;;; Karmawhore - an IRC karma tracker script
;;; Copyright (C) 2010  Leonidas
;;;
;;; This program is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU Affero General Public License as published by
;;; the Free Software Foundation, either version 3 of the License, or
;;; (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU Affero General Public License for more details.
;;;
;;; You should have received a copy of the GNU Affero General Public License
;;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(use-modules (karmalib)
  (srfi srfi-39))

(define main
  (lambda (args)
    (parameterize ((histogram (make-hash-table)))
      (with-input-from-file "intum.log" handler)
      (print-results (order-by-karma (histogram->list (histogram)))))))
