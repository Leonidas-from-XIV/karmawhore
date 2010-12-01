#!/usr/bin/guile \
-e main -s
!#
;;; Karmawhore - an IRC karma tracker script
;;; Copyright (C) 2010  Leonidas
;;;
;;; This program is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published by
;;; the Free Software Foundation, either version 3 of the License, or
;;; (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(use-modules (ice-9 rdelim) (ice-9 regex) (ice-9 format))

;; according to ircd-seven everything between ASCII 64 and 128 is valid
;; I just take a subset of that. The nick has to be between 1 and 16 characters
;; (seven supports 16 as default, maximum 50)
(define allowed-nickname "[A-~]{1,16}")
(define nick-plus (make-regexp (format "(~a)\\+\\+" allowed-nickname)))
(define nick-minus (make-regexp (format "(~a)\\-\\-" allowed-nickname)))

(define hash (make-hash-table))

(define extract-nicks
  (lambda (regexp line)
    (map (lambda (item) (match:substring item 1))
         (list-matches regexp line))))

(define hash-inc
  (lambda (hash nick)
    (let ((current-value (hash-ref hash nick)))
      (if (not current-value) (hash-set! hash nick 1)
          (hash-set! hash nick (+ current-value 1))))))

(define hash-dec
  (lambda (hash nick)
    (let ((current-value (hash-ref hash nick)))
      (if (not current-value) (hash-set! hash nick -1)
          (hash-set! hash nick (- current-value 1))))))


(define parse-line
  (lambda (line)
    (let ((nicks-add (extract-nicks nick-plus line))
          (nicks-sub (extract-nicks nick-minus line)))
      (map (lambda (nick) (hash-inc hash nick)) nicks-add)
      (map (lambda (nick) (hash-dec hash nick)) nicks-sub))))

(define handler
  (lambda ()
    (do ((line (read-line) (read-line)))
        ((eof-object? line))
      (parse-line line))))

(define histogram->list
  (lambda (hash)
    (hash-map->list (lambda (key value) (cons value key))
		    hash)))

(define order-by-karma
  (lambda (lst)
    (sort lst (lambda (a b) (> (car a) (car b))))))

(define print-results
  (lambda (karma-list)
    (map (lambda (item) (display (format "~d ~a~%" (car item) (cdr item))))
	 karma-list)))

(define main
  (lambda (args)
    (with-input-from-file "intum.log" handler)
    (print-results (order-by-karma (histogram->list hash)))))
