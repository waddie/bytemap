(ns bytemap.util
  "Low-level utility functions for bit manipulation and numeric operations.")

;; Malli Schemas
(def Int "Schema for integer values" int?)
(def Bit
  "Schema for bit positions (0-7)"
  [:int
   {:max 7
    :min 0}])
(def ByteValue
  "Schema for byte values (0-255)"
  [:int
   {:max 255
    :min 0}])

(defn set-bit
  "Sets or clears bit i in num.

  If value is truthy, sets the bit to 1.
  If value is falsy, clears the bit to 0.

  Example:
    (set-bit 0 3 true)  => 8
    (set-bit 15 0 false) => 14"
  {:malli/schema [:=> [:cat Int Bit :any] Int]}
  [num i value]
  (if value
    (bit-or num (bit-shift-left 1 i))
    (bit-and num (bit-not (bit-shift-left 1 i)))))

(defn idiv
  "Integer division (floor division).

  Returns the floor of a divided by b.

  Example:
    (idiv 7 2)  => 3
    (idiv -7 2) => -4"
  {:malli/schema [:=> [:cat Int Int] Int]}
  [a b]
  #?(:clj (long (Math/floor (/ a b)))
     :cljs (js/Math.floor (/ a b))))
