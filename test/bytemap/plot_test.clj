(ns bytemap.plot-test
  "Tests for bytemap plotting functionality."
  (:require [bytemap.core :as bm]
            [bytemap.plot :as bp]
            [clojure.string :as s]
            [clojure.test :refer [deftest is testing]]
            [still.core :refer [snap!]]))

(deftest intermediate-plot-test
  (testing "Plotting a function"
    (let [canvas (bm/new-canvas 10 5)]
      (snap! canvas
             {:height 5
              :pixels [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
                       0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
              :style  :braille
              :width  10})
      (let [canvas (bp/plot canvas #(Math/sqrt (abs (- 1 (Math/pow % 2)))))]
        (snap! canvas
               {:height 5
                :pixels [0 128 52 22 26 95 18 166 64 0 176 1 0 0 0 71 0 0 8 70
                         46 36 36 36 36 103 36 36 36 53 0 0 0 0 0 71 0 0 0 0 0 0
                         0 0 0 71 0 0 0 0]
                :style  :braille
                :width  10})
        (snap! (str "\n" (bm/canvas->string canvas))
               "
⠀⢀⠴⠖⠚⡟⠒⢦⡀⠀
⢰⠁⠀⠀⠀⡇⠀⠀⠈⡆
⠮⠤⠤⠤⠤⡧⠤⠤⠤⠵
⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀")))))

(deftest sine-wave-test
  (testing "Plotting a sine wave"
    (let
      [expected
       "
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⢀⠤⠖⠚⠒⠒⢤⡀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠈⠢⡀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⢠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠱⡀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡷⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢄
⠹⡉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⢉⠝⡏⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉
⠀⠘⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠊⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠈⠢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⠁⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀⡠⠊⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠑⢤⣀⣀⢀⣀⡤⠊⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
"]
      ;; Capture output from print-plot! function
      (is (= expected
             (with-out-str
               (println)
               (bp/print-plot! #(Math/sin %) [40 10] Math/PI 1)))))))

(deftest plot-draws-lines-test
  (testing "Plot draws lines rather than individual points"
    (let
      [expected
       "
⠀⠀⠀⠀⠀⠀⢠⠲⡀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⢇⠀
⠀⠀⠀⠀⠀⢰⠁⠀⠸⡀
⠀⠀⠀⠀⠀⡸⠀⠀⠀⡇
⠀⠀⠀⠀⠀⡇⠀⠀⠀⢣
⡇⠀⠀⠀⢰⠁⠀⠀⠀⠈
⢸⠀⠀⠀⡸⠀⠀⠀⠀⠀
⠈⡆⠀⠀⡇⠀⠀⠀⠀⠀
⠀⢇⠀⢸⠀⠀⠀⠀⠀⠀
⠀⠘⣄⠇⠀⠀⠀⠀⠀⠀
"]
      (is (=
           expected
           (with-out-str
             (println)
             (bp/print-plot! #(Math/sin %) [10 10] Math/PI 1 :axis false)))))))

(deftest plot->string-test
  (testing "plot->string returns string without printing"
    (let
      [expected
       "
⠀⠀⠀⠀⠀⠀⢠⠲⡀⠀
⠀⠀⠀⠀⠀⠀⡇⠀⢇⠀
⠀⠀⠀⠀⠀⢰⠁⠀⠸⡀
⠀⠀⠀⠀⠀⡸⠀⠀⠀⡇
⠀⠀⠀⠀⠀⡇⠀⠀⠀⢣
⡇⠀⠀⠀⢰⠁⠀⠀⠀⠈
⢸⠀⠀⠀⡸⠀⠀⠀⠀⠀
⠈⡆⠀⠀⡇⠀⠀⠀⠀⠀
⠀⢇⠀⢸⠀⠀⠀⠀⠀⠀
⠀⠘⣄⠇⠀⠀⠀⠀⠀⠀"
       result
       (str "\n" (bp/plot->string #(Math/sin %) [10 10] Math/PI 1 :axis false))]
      (is (= expected result)))))

(deftest plot->string-blocks-test
  (testing "plot->string renders with block octants"
    ;; Blank cells are spaces, so the expected lines live in a vector to keep
    ;; their trailing whitespace out of the source.
    (is (= (bp/plot->string #(Math/sin %) [10 10] Math/PI 1 :axis false :style :blocks)
           (s/join "\n"
                   ["      ▗𜴣𜺣 "
                    "      ▌ 𜶅 "
                    "     𜶖𜺨 𜴡𜺣"
                    "     𜵛   ▌"
                    "     ▌   ▚"
                    "▌   𜶖𜺨   𜺫"
                    "▐   𜵛     "
                    "𜺫𜵈  ▌     "
                    " 𜶅 ▐      "
                    " ▝𜶻𜴍      "])))))

(deftest histogram-blocks-test
  (testing "Vertical histogram with block octants"
    (let [bins   (sorted-map 0 1 1 3 2 5 3 2)
          canvas (bp/histogram (bm/new-canvas 4 5 :style :blocks) bins)]
      (is (= (bm/canvas->string canvas)
             (s/join "\n"
                     ["  █ "
                      "  █ "
                      " ██ "
                      " ███"
                      "████"]))))))

(deftest histogram-vertical-basic-test
  (testing "Vertical histogram with one column-pair per bin"
    (let [bins   (sorted-map 0 1 1 3 2 5 3 2)
          canvas (bp/histogram (bm/new-canvas 4 5) bins)]
      (snap! (str "\n" (bm/canvas->string canvas))
             "
⠀⠀⣿⠀
⠀⠀⣿⠀
⠀⣿⣿⠀
⠀⣿⣿⣿
⣿⣿⣿⣿"))))

(deftest histogram-horizontal-basic-test
  (testing "Horizontal histogram with one row-pair per bin"
    (let [bins   (sorted-map 0 1 1 3 2 5 3 2)
          canvas (bp/histogram (bm/new-canvas 5 4)
                               bins
                               :orientation
                               :horizontal)]
      (snap! (str "\n" (bm/canvas->string canvas))
             "
⣿⠀⠀⠀⠀
⣿⣿⣿⠀⠀
⣿⣿⣿⣿⣿
⣿⣿⠀⠀⠀"))))

(deftest histogram-downsampling-test
  (testing "Vertical histogram downsamples when bins exceed available columns"
    (let [bins   (into (sorted-map) (map vector (range 20) (repeat 1)))
          canvas (bp/histogram (bm/new-canvas 4 3) bins)]
      (snap! (str "\n" (bm/canvas->string canvas)) "
⣿⣿⣿⣿
⣿⣿⣿⣿
⣿⣿⣿⣿"))))

(deftest histogram-zero-bin-test
  (testing "Zero-valued bins draw nothing"
    (let [bins   (sorted-map 0 0 1 5 2 0)
          canvas (bp/histogram (bm/new-canvas 3 4) bins)]
      (snap! (str "\n" (bm/canvas->string canvas)) "
⠀⣿⠀
⠀⣿⠀
⠀⣿⠀
⠀⣿⠀"))))

(deftest histogram-empty-bins-test
  (testing "Empty bins returns canvas unchanged"
    (let [canvas (bm/new-canvas 4 3)]
      (is (= canvas (bp/histogram canvas {}))))))
