(ns mathdice-solver.core
  (:gen-class)
  (:require [clojure.math.numeric-tower :as math
             :refer [expt]]))

;;;
;;; This section deals with producing mathdice problems.
;;;

;; List of legal ways to combine two numbers in MathDice.
(def math-functions ['+ '- '* '/ 'expt])

(defn roll-d
  "Produces a lazy sequence of random integers from 1 to n."
  [n]
  (repeatedly #(inc (rand-int n))))

(defn generate-random-problem
  "Generates a random MathDice problem in the format (target die1 die2 die3)."
  []
  (cons (apply * (take 2 (roll-d 12)))
        (take 3 (roll-d 6))))

;;;
;;; This section contains functions used to generate potential solutions.
;;;

(defn drop-nth
  "Accepts a sequence, and returns that sequence with the nth element removed."
  [sqn n]
  (concat (take n sqn) (drop (inc n) sqn)))

(defn drop-each
  "Accepts a sequence and returns a list of all sequences created by removing one element from it."
  [sqn]
  (map #(drop-nth sqn %)
       (range (count sqn))))

(defn cons-to-all
  "Accepts a value and a collection of sequences. Prepends the value to each sequence."
  [x, sqns]
  (map #(cons x %) sqns))

(defn permute-n
	"Produces a collection of all possible unique sequences of n-many elements of the input sequence."
	[sqn n]
	(if (= 1 n)
	    (set (map #(list %) sqn))
	    (reduce into #{}
	            ;; Try each possible starting element, followed by all possible
	            ;; sequences of length n-1.
	            (map #(cons-to-all %1 (permute-n %2 (- n 1)))
	                 sqn
	                 (drop-each sqn)))))

;;;
;;; Construct all the possible expressions.
;;;

(defn pick-two
  "Produces all ways to pick two elements from a sequence, as well as the remainder after each one."
  [sqn]
  (let [n (count sqn)]
       (set (for [i (range n)
                  :let [x (nth sqn i)
                        sqn-i (drop-nth sqn i)]
                  j (range (dec n))
                  :let [y (nth sqn-i j)
                        sqn-i-j (drop-nth sqn-i j)]]
                 (list x y sqn-i-j)))))

(defn generate-expressions
  "Generate all possible expressions using the given two-operation operators on the given operands."
  [operators operands]
  (if (-> operands count (< 2))
      (set operands)
      (reduce into #{}
              (for [[x y more] (pick-two operands)
                    f operators]
                  (generate-expressions
                    operators
                    (conj more (list f x y)))))))

(defn unique-outputs [expressions]
  (reduce #(try (assoc %1 (eval %2) %2) (catch Exception e nil)) {} expressions))

(defn distance [x y]
  (let [t (- x y)]
       (if (>= t 0)
           t
           (- 1 t))))

(defn best-n [n guesses target]
  (take n (sort-by #(* 1 (distance % target)) guesses)))

(defn find-solution [[target & dice]]
  (let [solution-list (unique-outputs (generate-expressions math-functions dice))
        answer (first (sort-by #(distance % target) (keys solution-list)))]
      (list answer '= (get solution-list answer))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (str "Here, have a mathdice problem: " (generate-random-problem))))
