(ns bytemap.drawing-test
  "Tests for bytemap drawing functionality, ported from Janet implementation."
  (:require [clojure.test :refer [deftest is testing]]
            [bytemap.core :as bm]))

(defmacro test-canvas-output
  "Tests that the canvas output matches expected braille art.
  Similar to Janet's test-stdout macro."
  [canvas expected]
  `(is (= ~expected (bm/canvas->string ~canvas))))

(deftest union-jack-test
  (testing "Drawing the union jack pattern"
    (let [canvas (bm/new-canvas 10 5)
          ;; Draw diagonal lines
          canvas (reduce (fn [c x]
                          (-> c
                              (bm/draw-point [x x])
                              (bm/draw-point [x (- 20 x)])))
                        canvas
                        (range 21))
          ;; Draw cross
          canvas (reduce (fn [c x]
                          (-> c
                              (bm/draw-point [10 x])
                              (bm/draw-point [x 10])))
                        canvas
                        (range 21))]
      (test-canvas-output canvas
                         "⠑⢄⠀⠀⠀⡇⠀⠀⢀⠔\n⠀⠀⠑⢄⠀⡇⢀⠔⠁⠀\n⠤⠤⠤⠤⢵⣷⠥⠤⠤⠤\n⠀⠀⢀⠔⠁⡇⠑⢄⠀⠀\n⢀⠔⠁⠀⠀⡇⠀⠀⠑⢄"))))

(deftest fill-test
  (testing "Filling entire canvas"
    (let [canvas (bm/new-canvas 10 5)
          canvas (reduce (fn [c [x y]]
                          (bm/draw-point c [x y]))
                        canvas
                        (for [x (range 30)
                              y (range 30)]
                          [x y]))]
      (test-canvas-output canvas
                         "⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿"))))

(deftest sine-wave-test
  (testing "Plotting a sine wave"
    ;; Note: Output differs slightly from Janet due to floating-point rounding differences
    (let [expected "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⢀⠤⠖⠚⠒⠒⢤⡀⠀⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠈⠢⡀⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⢠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠱⡀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡷⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢄\n⠹⡉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⢉⠝⡏⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉\n⠀⠘⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠊⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⠈⠢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⠁⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀⡠⠊⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠑⢤⣀⣀⢀⣀⡤⠊⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀"]
      ;; Capture output from plot function
      (is (= expected
             (with-out-str (bm/plot #(Math/sin %) [40 10] Math/PI 1)))))))

(deftest plot-draws-lines-test
  (testing "Plot draws lines rather than individual points"
    (let [expected "⠀⠀⠀⠀⠀⠀⢠⠲⡀⠀\n⠀⠀⠀⠀⠀⠀⡇⠀⢇⠀\n⠀⠀⠀⠀⠀⢰⠁⠀⠸⡀\n⠀⠀⠀⠀⠀⡸⠀⠀⠀⡇\n⠀⠀⠀⠀⠀⡇⠀⠀⠀⢣\n⡇⠀⠀⠀⢰⠁⠀⠀⠀⠈\n⢸⠀⠀⠀⡸⠀⠀⠀⠀⠀\n⠈⡆⠀⠀⡇⠀⠀⠀⠀⠀\n⠀⢇⠀⢸⠀⠀⠀⠀⠀⠀\n⠀⠘⣄⠇⠀⠀⠀⠀⠀⠀"]
      (is (= expected
             (with-out-str (bm/plot #(Math/sin %) [10 10] Math/PI 1 :axis false)))))))

(deftest line-test
  (testing "Drawing multiple lines"
    (let [canvas (-> (bm/new-canvas 10 5)
                     (bm/draw-line [0 0] [20 20])
                     (bm/draw-line [0 5] [20 10])
                     (bm/draw-line [5 15] [20 5]))]
      (test-canvas-output canvas
                         "⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀\n⠒⠢⠵⢄⣀⡀⠀⠀⢀⠤\n⠀⠀⠀⠀⠑⢌⠭⠛⠓⠢\n⠀⠀⢀⠤⠊⠁⠑⢄⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠑⢄"))))

(deftest radial-lines-test
  (testing "Drawing lines in all directions (radial pattern)"
    (let [tau (* 2 Math/PI)
          c 30
          canvas (bm/new-canvas c (/ c 2))
          r (- c 1)
          points 20
          canvas (reduce (fn [canvas i]
                          (let [angle (+ 0.1 (* i (/ tau points)))]
                            (bm/draw-line canvas [c c]
                                         [(+ c (* r (Math/cos angle)))
                                          (+ c (* r (Math/sin angle)))])))
                        canvas
                        (range points))]
      (test-canvas-output canvas
                         "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡄⠀⠀⠀⢰⠀⠀⠀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠐⡄⠀⠀⠀⢱⠀⠀⠀⡜⠀⠀⠀⡰⠁⠀⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⡀⠀⠀⠀⠘⢄⠀⠀⠈⡆⠀⠀⡇⠀⠀⡰⠁⠀⠀⡠⠊⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠈⠢⣀⠀⠀⠈⢆⠀⠀⢣⠀⠀⡇⠀⢠⠃⠀⢀⠜⠀⠀⠀⠀⢀⠀⠀\n⠀⠠⢄⡀⠀⠀⠀⠑⢄⠀⠈⢢⠀⠸⡀⢸⠀⢠⠃⢀⠔⠁⠀⠀⣀⠤⠒⠁⠀⠀\n⠀⠀⠀⠈⠑⠢⠤⣀⠀⠑⢄⡀⠣⡀⢇⢸⢀⠎⡰⠁⠀⡠⠔⠊⠀⠀⠀⠀⠀⠀\n⢀⣀⣀⠀⠀⠀⠀⠀⠉⠒⠢⢌⡢⡱⣸⣜⡮⢊⠤⠒⠉⣀⣀⡠⠤⠔⠒⠒⠉⠁\n⠀⠀⠀⠉⠉⠉⠉⠉⠒⠒⠒⢒⣚⣽⢷⣿⣾⢗⣊⣉⣉⣀⡀⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⢀⣀⡠⠤⠤⠒⠒⠊⠉⡡⠔⡞⣝⢿⢗⠭⡒⠤⢄⡀⠈⠉⠉⠉⠉⠑⠒⠂\n⠀⠉⠁⠀⠀⠀⠀⢀⡠⠒⠉⡠⠊⡜⢸⠈⡆⢣⠈⠢⡀⠈⠑⠒⠤⣀⠀⠀⠀⠀\n⠀⠀⠀⠀⣀⠔⠊⠁⠀⡠⠊⠀⡰⠁⢸⠀⢣⠀⠣⡀⠈⠢⢄⠀⠀⠀⠉⠒⠂⠀\n⠀⠀⠐⠉⠀⠀⠀⢀⠔⠁⠀⢰⠁⠀⡎⠀⠸⡀⠀⠱⡀⠀⠀⠑⢄⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⢀⠔⠁⠀⠀⢠⠃⠀⠀⡇⠀⠀⢇⠀⠀⠘⡄⠀⠀⠀⠑⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠃⠀⠀⢀⠇⠀⠀⢸⠀⠀⠀⠘⢄⠀⠀⠀⠀⠀⠀⠀\n⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⠀⠀⠸⠀⠀⠀⠀⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀"))))
