(ns bytemap.drawing-test
  "Tests for bytemap drawing functionality."
  (:require [bytemap.core :as bm]
            [clojure.string :as s]
            [clojure.test :refer [deftest is testing]]))

(defn ^:private union-jack
  "Draws the union jack pattern on a canvas."
  [canvas]
  (let [canvas (reduce (fn [c x]
                         (-> c
                             (bm/draw-point [x x])
                             (bm/draw-point [x (- 20 x)])))
                       canvas
                       (range 21))]
    (reduce (fn [c x]
              (-> c
                  (bm/draw-point [10 x])
                  (bm/draw-point [x 10])))
            canvas
            (range 21))))

(deftest union-jack-test
  (testing "Drawing the union jack pattern"
    (let [canvas (union-jack (bm/new-canvas 10 5))]
      (is (= (str "\n" (bm/canvas->string canvas))
             "
⠑⢄⠀⠀⠀⡇⠀⠀⢀⠔
⠀⠀⠑⢄⠀⡇⢀⠔⠁⠀
⠤⠤⠤⠤⢵⣷⠥⠤⠤⠤
⠀⠀⢀⠔⠁⡇⠑⢄⠀⠀
⢀⠔⠁⠀⠀⡇⠀⠀⠑⢄")))))

(deftest union-jack-blocks-test
  (testing "Drawing the union jack pattern with block octants"
    ;; Blank cells are spaces, so the expected lines live in a vector to
    ;; keep their trailing whitespace out of the source.
    (let [canvas (union-jack (bm/new-canvas 10 5 :style :blocks))]
      (is (= (bm/canvas->string canvas)
             (s/join "\n"
                     ["𜴄𜶀   ▌  𜺠𜴐"
                      "  𜴄𜶀 ▌𜺠𜴐𜺨 "
                      "𜴧𜴧𜴧𜴧𜶥𜷤𜴨𜴧𜴧𜴧"
                      "  𜺠𜴐𜺨▌𜴄𜶀  "
                      "𜺠𜴐𜺨  ▌  𜴄𜶀"]))))))

(deftest fill-test
  (testing "Filling entire canvas"
    (let [canvas (bm/new-canvas 10 5)
          canvas (reduce (fn [c [x y]] (bm/draw-point c [x y]))
                         canvas
                         (for [x (range 30)
                               y (range 30)]
                           [x y]))]
      (is (= (str "\n" (bm/canvas->string canvas))
             "
⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿")))))

(deftest line-test
  (testing "Drawing multiple lines"
    (let [canvas (-> (bm/new-canvas 10 5)
                     (bm/draw-line [0 0] [20 20])
                     (bm/draw-line [0 5] [20 10])
                     (bm/draw-line [5 15] [20 5]))]
      (is (= (str "\n" (bm/canvas->string canvas))
             "
⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀
⠒⠢⠵⢄⣀⡀⠀⠀⢀⠤
⠀⠀⠀⠀⠑⢌⠭⠛⠓⠢
⠀⠀⢀⠤⠊⠁⠑⢄⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠑⢄")))))

(deftest radial-lines-test
  (testing "Drawing lines in all directions (radial pattern)"
    (let [tau    (* 2 Math/PI)
          c      30
          canvas (bm/new-canvas c (/ c 2))
          r      (- c 1)
          points 20
          canvas (reduce (fn [canvas i]
                           (let [angle (+ 0.1 (* i (/ tau points)))]
                             (bm/draw-line canvas
                                           [c c]
                                           [(+ c (* r (Math/cos angle)))
                                            (+ c (* r (Math/sin angle)))])))
                         canvas
                         (range points))]
      (is
       (=
        (str "\n" (bm/canvas->string canvas))
        "
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡄⠀⠀⠀⢰⠀⠀⠀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠐⡄⠀⠀⠀⢱⠀⠀⠀⡜⠀⠀⠀⡰⠁⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⡀⠀⠀⠀⠘⢄⠀⠀⠈⡆⠀⠀⡇⠀⠀⡰⠁⠀⠀⡠⠊⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠈⠢⣀⠀⠀⠈⢆⠀⠀⢣⠀⠀⡇⠀⢠⠃⠀⢀⠜⠀⠀⠀⠀⢀⠀⠀
⠀⠠⢄⡀⠀⠀⠀⠑⢄⠀⠈⢢⠀⠸⡀⢸⠀⢠⠃⢀⠔⠁⠀⠀⣀⠤⠒⠁⠀⠀
⠀⠀⠀⠈⠑⠢⠤⣀⠀⠑⢄⡀⠣⡀⢇⢸⢀⠎⡰⠁⠀⡠⠔⠊⠀⠀⠀⠀⠀⠀
⢀⣀⣀⠀⠀⠀⠀⠀⠉⠒⠢⢌⡢⡱⣸⣜⡮⢊⠤⠒⠉⣀⣀⡠⠤⠔⠒⠒⠉⠁
⠀⠀⠀⠉⠉⠉⠉⠉⠒⠒⠒⢒⣚⣽⢷⣿⣾⢗⣊⣉⣉⣀⡀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⢀⣀⡠⠤⠤⠒⠒⠊⠉⡡⠔⡞⣝⢿⢗⠭⡒⠤⢄⡀⠈⠉⠉⠉⠉⠑⠒⠂
⠀⠉⠁⠀⠀⠀⠀⢀⡠⠒⠉⡠⠊⡜⢸⠈⡆⢣⠈⠢⡀⠈⠑⠒⠤⣀⠀⠀⠀⠀
⠀⠀⠀⠀⣀⠔⠊⠁⠀⡠⠊⠀⡰⠁⢸⠀⢣⠀⠣⡀⠈⠢⢄⠀⠀⠀⠉⠒⠂⠀
⠀⠀⠐⠉⠀⠀⠀⢀⠔⠁⠀⢰⠁⠀⡎⠀⠸⡀⠀⠱⡀⠀⠀⠑⢄⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢀⠔⠁⠀⠀⢠⠃⠀⠀⡇⠀⠀⢇⠀⠀⠘⡄⠀⠀⠀⠑⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠃⠀⠀⢀⠇⠀⠀⢸⠀⠀⠀⠘⢄⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⠀⠀⠸⠀⠀⠀⠀⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀")))))
