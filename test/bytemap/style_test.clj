(ns bytemap.style-test
  "Tests for the character sets a canvas renders with."
  (:require [bytemap.core :as bm]
            [clojure.test :refer [deftest is testing]]))

(deftest block-glyphs-test
  (testing "Every byte maps to a distinct block octant character"
    (let [glyphs (map bm/block (range 256))]
      (is (= 256 (count (distinct glyphs))))
      (is (every? #(= 1 (.codePointCount ^String % 0 (count %))) glyphs))))
  (testing "Known patterns"
    (is (= " " (bm/block 0)))  ; empty
    (is (= "▘" (bm/block 3)))  ; quadrant upper left
    (is (= "▌" (bm/block 71))) ; left half
    (is (= "█" (bm/block 255))))) ; full block

(deftest default-style-test
  (testing "Canvases are braille unless asked otherwise"
    (is (= :braille (:style (bm/new-canvas 2 1))))
    (is (= "⠀⠀" (bm/canvas->string (bm/new-canvas 2 1)))))
  (testing "A canvas without a style renders as braille"
    (is (= "⠀⠀" (bm/canvas->string (dissoc (bm/new-canvas 2 1) :style))))))

(deftest unknown-style-test
  (testing "An unknown style is rejected"
    (is (thrown-with-msg? RuntimeException
                          #"Unknown style :dots"
                          (bm/canvas->string
                           (assoc (bm/new-canvas 2 1) :style :dots))))))
