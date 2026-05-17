(ns bytemap.plot
  (:require [bytemap.core :refer
             [bounds canvas->string draw-line draw-point new-canvas
              print-canvas!]]
            [bytemap.schema :as schema]
            [bytemap.util :as util]
            [clojure.core.match :refer [match]]
            [clojure.string :as s]))

(defn plot
  "Plots a mathematical function on a canvas.

  Arguments:
  - canvas: A canvas
  - f: Function to plot (takes a number, returns a number)

  Options:
  - :axis - Whether to draw x and y axes (default: true)
  - :x-scale: The range of x values (from -x-scale to +x-scale) (default: 1)
  - :y-scale: The range of y values (from -y-scale to +y-scale) (default: 1)

  The function is sampled at regular intervals across the canvas width,
  and consecutive points are connected with lines.

  Returns the new canvas."
  {:malli/schema [:function [:=> [:cat schema/Canvas fn?] schema/Canvas]
                  [:=> [:cat schema/Canvas fn? [:* :any]] schema/Canvas]]}
  [canvas f &
   {:keys [axis x-scale y-scale]
    :or   {axis    true
           x-scale 1
           y-scale 1}}]
  (let [[w h]   (bounds canvas)
        canvas  (if axis
                  ;; Draw axes
                  (let [canvas (reduce (fn [c i] (draw-point c [(/ w 2) i]))
                                       canvas
                                       (range h))]
                    (reduce (fn [c i] (draw-point c [i (/ h 2)]))
                            canvas
                            (range w)))
                  canvas)
        ;; Narrow y range slightly to avoid clipping extremes
        y-scale (* y-scale (/ (inc h) h))]
    ;; Sample function and draw lines between consecutive points
    (loop [i          0
           prev-point nil
           canvas     canvas]
      (if (>= i w)
        canvas
        (let [;; x spans -0.5 to 0.5 (inclusive)
              x      (- (/ i (dec w)) 0.5)
              y      (/ (f (* x 2 x-scale)) y-scale -2)
              p      [(* (+ x 0.5) (dec w)) (* (+ y 0.5) h)]
              canvas (if prev-point (draw-line canvas prev-point p) canvas)]
          (recur (inc i) p canvas))))))

(defn histogram
  "Plots a map as a histogram on a canvas.

  Arguments:
  - canvas: A canvas
  - bins: A map where the values are numbers (e.g. the output of clojure.core/frequencies).
          Iteration order of `bins` determines bar order (left-to-right for
          :vertical, top-to-bottom for :horizontal) — pass a sorted-map for a
          meaningful ordering.

  Options:
  - :orientation - Whether to draw the histogram with :vertical or :horizontal bars (default: :vertical)

  Bar heights (or widths, for :horizontal) are scaled relative to the largest
  value in `bins`. If `bins` has more entries than there are subpixel columns
  (:vertical) or rows (:horizontal) available on the canvas, it is downsampled
  via `bytemap.util/downsample-histogram` into exactly that many bars, each a
  weighted average over the corresponding fractional span of input bins — the
  original bin keys are not retained in that case.

  Returns the new canvas."
  {:malli/schema
   [:function [:=> [:cat schema/Canvas [:map-of :any number?]] schema/Canvas]
    [:=> [:cat schema/Canvas [:map-of :any number?] [:* :any]]
     schema/Canvas]]}
  [canvas bins &
   {:keys [orientation]
    :or   {orientation :vertical}}]
  (if (empty? bins)
    canvas
    (let [[w h]     (bounds canvas)
          bin-count (count bins)
          max-bin   (max 1 (apply max (vals bins)))]
      (match [orientation]
        [:vertical]
        (let [values    (if (> bin-count w)
                          (util/downsample-histogram bins w)
                          (vec (vals bins)))
              bar-count (count values)
              bin-width (max 1 (long (Math/floor (/ w bar-count))))]
          (reduce
           (fn [canvas [i v]]
             (let [bar-height (long (Math/round (double (* h (/ v max-bin)))))]
               (if (pos? bar-height)
                 (let [x0     (* i bin-width)
                       top    (- h bar-height)
                       bottom (dec h)]
                   (reduce
                    (fn [canvas dx]
                      (draw-line canvas [(+ x0 dx) bottom] [(+ x0 dx) top]))
                    canvas
                    (range bin-width)))
                 canvas)))
           canvas
           (map-indexed vector values)))
        [:horizontal]
        (let [values     (if (> bin-count h)
                           (util/downsample-histogram bins h)
                           (vec (vals bins)))
              bar-count  (count values)
              bin-height (max 1 (long (Math/floor (/ h bar-count))))]
          (reduce
           (fn [canvas [i v]]
             (let [bar-width (long (Math/round (double (* w (/ v max-bin)))))]
               (if (pos? bar-width)
                 (let [y0    (* i bin-height)
                       right (dec bar-width)]
                   (reduce (fn [canvas dy]
                             (draw-line canvas [0 (+ y0 dy)] [right (+ y0 dy)]))
                           canvas
                           (range bin-height)))
                 canvas)))
           canvas
           (map-indexed vector values)))
        :else #?(:clj (throw (RuntimeException. (str "Unknown orientation "
                                                     orientation)))
                 :cljs (throw (str "Unknown orientation " orientation)))))))

(defn plot-histogram
  "Plots and prints a histogram on a new canvas."
  {:malli/schema [:function [:=> [:cat [:seqable number?]] :nil]
                  [:=> [:cat [:seqable number?] [:* :any]] :nil]]}
  [xs &
   {:keys [w h stats orientation]
    :or   {orientation :vertical
           stats       true}}]
  (let [bins      (into (sorted-map) (frequencies xs))
        bin-count (count (keys bins))
        max-bin   (apply max (vals bins))
        max-bar   (if (> max-bin 128) 128 max-bin)
        width     (if (= orientation :vertical)
                    (or w bin-count)
                    (or h bin-count))
        height    (if (= orientation :vertical) (or h max-bar) (or w max-bar))
        hist      (histogram (new-canvas width height)
                             bins
                             :orientation
                             orientation)]
    (print-canvas! hist)
    (when stats
      (let [mean    (util/calculate-mean xs)
            std-dev (util/calculate-std-dev xs mean)]
        (println (str "  μ = " (util/format-float mean)))
        (println (str "  σ = " (util/format-float std-dev)))))))

(defn plot->string
  "Convenience function that plots a mathematical function and returns the string representation.

  Arguments:
  - f: Function to plot (takes a number, returns a number)
  - [w h]: schema/Canvas dimensions in pixels
  - x-scale: The range of x values (from -x-scale to +x-scale)
  - y-scale: The range of y values (from -y-scale to +y-scale)

  Options:
  - :axis - Whether to draw x and y axes (default: true)"
  {:malli/schema
   [:function [:=> [:cat fn? [:tuple :int :int] number? number?] :string]
    [:=> [:cat fn? [:tuple :int :int] number? number? [:* :any]] :string]]}
  [f [w h] x-scale y-scale &
   {:keys [axis]
    :or   {axis true}}]
  (-> (new-canvas w h)
      (plot f :x-scale x-scale :y-scale y-scale :axis axis)
      canvas->string))

(defn print-plot!
  "Convenience function that plots a mathematical function on a new canvas and prints it.

  Arguments:
  - f: Function to plot (takes a number, returns a number)
  - [w h]: schema/Canvas dimensions in pixels
  - x-scale: The range of x values (from -x-scale to +x-scale)
  - y-scale: The range of y values (from -y-scale to +y-scale)

  Options:
  - :axis - Whether to draw x and y axes (default: true)"
  {:malli/schema
   [:function [:=> [:cat fn? [:tuple :int :int] number? number?] :nil]
    [:=> [:cat fn? [:tuple :int :int] number? number? [:* :any]] :nil]]}
  [f [w h] x-scale y-scale &
   {:keys [axis]
    :or   {axis true}}]
  (let [s     (plot->string f [w h] x-scale y-scale :axis axis)
        lines (s/split s #"\n")]
    (doseq [line lines]
      (println line))
    nil))
