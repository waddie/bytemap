(ns bytemap.util
  "Low-level utility functions for bit manipulation and numeric operations."
  (:require [bytemap.schema :as schema]
            [still.core :refer [snap!]]))

(defn set-bit
  "Sets or clears bit i in num."
  {:malli/schema [:function [:=> [:cat :int schema/Bit :any] :int]]}
  [num i value]
  (if value
    (bit-or num (bit-shift-left 1 i))
    (bit-and num (bit-not (bit-shift-left 1 i)))))

(snap! (set-bit 0 3 true) 8)
(snap! (set-bit 15 0 false) 14)

(defn idiv
  "Returns the floor of a divided by b."
  {:malli/schema [:function [:=> [:cat :int :int] :int]]}
  [a b]
  #?(:clj (long (Math/floor (/ a b)))
     :cljs (js/Math.floor (/ a b))))

(snap! (idiv 7 2) 3)
(snap! (idiv -7 2) -4)

(defn format-float
  "Format a floating point number."
  {:malli/schema [:function [:=> [:cat number?] :string]]}
  [x]
  #?(:clj (format "%1$.6f" x)
     :cljs x))

(snap! (format-float 4.0) "4.000000")
(snap! (format-float 0.130434982092987) "0.130435")

(defn calculate-mean
  {:malli/schema [:function [:=> [:cat [:seqable number?]] :double]
                  [:=> [:cat [:seqable number?] number?] :double]]}
  [xs]
  (double (/ (reduce + xs) (count xs))))

(defn calculate-std-dev
  {:malli/schema [:function [:=> [:cat [:seqable number?]] :double]
                  [:=> [:cat [:seqable number?] number?] :double]]}
  [xs & rest]
  (let [m (or (first rest) (calculate-mean xs))]
    (->> xs
         (map #(- % m))
         (map #(* % %))
         (reduce +)
         (/ (count xs))
         Math/sqrt)))

(defn bin-overlap
  "Proportion of bin i overlapping the interval [start, end)."
  [i start end]
  (let [bin-start (double i)
        bin-end   (inc bin-start)]
    (max 0.0 (- (min end bin-end) (max start bin-start)))))

(defn bar-value
  "Weighted average of bins for a bar spanning [start, end)."
  [bins start end]
  (let [lo (int start)
        hi (min (count bins) (int (Math/ceil end)))]
    (/ (transduce (map (fn [i] (* (bins i) (bin-overlap i start end))))
                  +
                  (range lo hi))
       (- end start))))

(defn downsample-histogram
  "Reduce `bins` to `bar-count` values by weighted averaging."
  [bins bar-count]
  (let [bins  (mapv val bins)
        n     (count bins)
        ratio (/ (double n) bar-count)]
    (mapv (fn [p] (bar-value bins (* p ratio) (* (inc p) ratio)))
          (range bar-count))))
