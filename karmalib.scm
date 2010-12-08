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

(define-module (karmalib)
  #:use-module (ice-9 rdelim)
  #:use-module (ice-9 regex)
  #:use-module (ice-9 format)
  #:use-module (srfi srfi-39)
  #:export (allowed-nickname
	     histogram handler
	     print-results
	     order-by-karma
	     histogram->list))

;; according to ircd-seven everything between ASCII 64 and 128 is valid
;; I just take a subset of that. The nick has to be between 1 and 16 characters
;; (seven supports 16 as default, maximum 50)
(define allowed-nickname "[A-~]{1,16}")
(define nick-plus (make-regexp (format "(~a)\\+\\+" allowed-nickname)))
(define nick-minus (make-regexp (format "(~a)\\-\\-" allowed-nickname)))

(define histogram (make-parameter 'dummy))

(define extract-nicks
  (lambda (regexp line)
    (map (lambda (item) (match:substring item 1))
         (list-matches regexp line))))

(define increase-karma
  (lambda (nick)
    (let* ((data (histogram))
	   (current-value (hash-ref data nick)))
      (if (not current-value) (hash-set! data nick 1)
	(hash-set! data nick (+ current-value 1))))))

(define decrease-karma
  (lambda (nick)
    (let* ((data (histogram))
	   (current-value (hash-ref data nick)))
      (if (not current-value) (hash-set! data nick -1)
	(hash-set! data nick (- current-value 1))))))

(define parse-line
  (lambda (line)
    (let ((nicks-add (extract-nicks nick-plus line))
          (nicks-sub (extract-nicks nick-minus line)))
      (map increase-karma nicks-add)
      (map decrease-karma nicks-sub))))

(define handler
  (lambda ()
    (do ((line (read-line) (read-line)))
        ((eof-object? line))
      (parse-line line))))

(define histogram->list
  (lambda (histogram)
    (hash-map->list (lambda (key value) (cons value key))
		    histogram)))

(define order-by-karma
  (lambda (lst)
    (sort lst (lambda (a b) (> (car a) (car b))))))

(define print-results
  (lambda (karma-list)
    (map (lambda (item) (display (format "~d ~a~%" (car item) (cdr item))))
	 karma-list)))

