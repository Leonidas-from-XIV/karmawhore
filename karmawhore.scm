;;; Guile
(use-modules (ice-9 rdelim) (ice-9 regex) (ice-9 format))

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

(define nick-plus (make-regexp "(\\S*)\\+\\+"))
(define nick-minus (make-regexp "(\\S*)\\-\\-"))

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

(with-input-from-file "intum.log" handler)
(print-results (order-by-karma (histogram->list hash)))