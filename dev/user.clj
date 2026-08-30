(ns user
  #_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
  (:require [build :as b]
            [bytemap.core :as bm]
            [bytemap.plot :as bp]
            [bytemap.util :as bu]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [doc source]]
            [cognitect.test-runner.api :as test-runner]
            [malli.core :as m]
            [malli.dev :as md]
            [malli.dev.pretty :as mdp]
            [malli.instrument :as mi]
            [still.core :refer [snap!]])
  (:import (java.util Random)))

(comment
  (set! *warn-on-reflection* true)
  (set! *warn-on-reflection* false))

(md/start! {:report (mdp/reporter (mdp/-printer {:colors       false
                                                 :print-length 30
                                                 :print-level  2
                                                 :print-meta   false
                                                 :width        80}))})

(comment
  (mi/collect!)
  (m/function-schemas))

(comment
  (do (in-ns 'user) (time (test-runner/test nil))))

(comment
  (do (in-ns 'user) (time (b/jar-all nil)))
  (do (in-ns 'user) (time (b/uber-all nil))))

(snap!
 (str "\n"
      (bp/plot->string #(Math/sqrt (abs (- 1 (Math/pow % 2))))
                       [20 5]
                       1 Math/PI
                       :axis true))
 "
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢀⣀⣀⣀⣀⣀⣇⣀⣀⣀⣀⣀⠀⠀⠀⠀
⠴⠾⠭⠭⠭⠤⠤⠤⠤⠤⡧⠤⠤⠤⠤⠬⠭⠭⠽⠦
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀")

(snap!
 (str "\n"
      (let [x-scale 1
            y-scale Math/PI]
        (-> (bm/new-canvas 20 5)
            (bp/plot #(Math/sqrt (abs (- 1 (Math/pow % 2))))
                     :axis true
                     :x-scale x-scale
                     :y-scale y-scale)
            bm/canvas->string)))
 "
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢀⣀⣀⣀⣀⣀⣇⣀⣀⣀⣀⣀⠀⠀⠀⠀
⠴⠾⠭⠭⠭⠤⠤⠤⠤⠤⡧⠤⠤⠤⠤⠬⠭⠭⠽⠦
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀")

(comment
  (let [random (Random. 1234)
        nums   (stream-seq! (.ints random 10000000 0 11))
        m      (into (sorted-map) (frequencies nums))]
    (pprint m)
    (pprint (bu/downsample-histogram m 5))))

(snap! (str "\n"
            (bm/canvas->string (loop [canvas (bm/new-canvas 5 3)
                                      x      0
                                      y      0]
                                 (if (and (= x 10) (= y 12))
                                   canvas
                                   (recur (bm/draw-point canvas [x y])
                                          (if (= 10 x) 0 (+ 2 x))
                                          (if (= 10 x) (+ 2 y) y))))))
       "
⠅⠅⠅⠅⠅
⠅⠅⠅⠅⠅
⠅⠅⠅⠅⠅")

(let [bins   (sorted-map 0 1 1 3 2 5 3 2)
      canvas (bp/histogram (bm/new-canvas 20 30) bins :orientation :vertical)]
  (bm/canvas->string canvas))


(defn golden-spiral
  "Draw the golden spiral on a canvas."
  [canvas {:keys [center scale max-θ start-angle]}]
  (let [φ       1.618033988749895
        [cx cy] center
        steps   1000]
    (loop [i          0
           prev-point nil
           canvas     canvas]
      (if (>= i steps)
        canvas
        (let [θ      (+ start-angle (* max-θ (/ i (dec steps))))
              r      (* scale (Math/pow φ (/ θ (/ Math/PI 2))))
              x      (+ cx (* r (Math/cos (- θ))))
              y      (+ cy (* r (Math/sin (- θ))))
              point  [(int x) (int y)]
              canvas (if prev-point
                       (bm/draw-line canvas prev-point point)
                       canvas)]
          (recur (inc i) point canvas))))))

(-> (bm/new-canvas 60 20 :style :blocks)
    (golden-spiral {:center      [60 45]
                    :max-θ       (* 6 Math/PI)
                    :scale       0.5
                    :start-angle (* 0.75 Math/PI)})
    (bm/print-canvas!))

(-> (bm/new-canvas 60 20 :style :braille)
    (golden-spiral {:center      [60 45]
                    :max-θ       (* 6 Math/PI)
                    :scale       0.5
                    :start-angle (* 0.75 Math/PI)})
    (bm/print-canvas!))

(let [τ      (* 2 Math/PI)
      c      30
      canvas (bm/new-canvas c (/ c 2) :style :blocks)
      r      (- c 1)
      points 20
      canvas (reduce (fn [canvas i]
                       (let [angle (+ 0.1 (* i (/ τ points)))]
                         (bm/draw-line canvas
                                       [c c]
                                       [(+ c (* r (Math/cos angle)))
                                        (+ c (* r (Math/sin angle)))])))
                     canvas
                     (range points))]
  (bm/print-canvas! canvas))
