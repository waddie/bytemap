(ns bytemap.util
  "Low-level utility functions for bit manipulation and numeric operations."
  (:require [malli.core :as m]
            [malli.util :as mu]))

;; Malli Schemas
(def least-six-bit-mask 0x3f)
(def least-four-bit-mask 0x0f)

(def Int
  "Schema for integer values"
  int?)

(def Bit
  "Schema for bit positions (0-7)"
  [:int {:min 0 :max 7}])

(def ByteValue
  "Schema for byte values (0-255)"
  [:int {:min 0 :max 255}])

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
  #?(:clj  (long (Math/floor (/ a b)))
     :cljs (js/Math.floor (/ a b))))

(defn encode
  "Encodes a Unicode codepoint to UTF-8 bytes (3-byte format for braille range).

  Note: In Clojure/ClojureScript, this is primarily for educational purposes.
  The JVM and JS engines handle Unicode natively, so (char codepoint) works directly.

  Example:
    (encode 0x2800) => [0xe0 0xa0 0x80]"
  {:malli/schema [:=> [:cat Int] [:vector Int]]}
  [codepoint]
  (let [first-six  (bit-and codepoint least-six-bit-mask)
        next-six   (bit-and (bit-shift-right codepoint 6) least-six-bit-mask)
        next-four  (bit-and (bit-shift-right codepoint 12) least-four-bit-mask)]
    [(bit-or 0xe0 next-four)
     (bit-or 0x80 next-six)
     (bit-or 0x80 first-six)]))
