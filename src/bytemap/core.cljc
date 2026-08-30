(ns bytemap.core
  "Text-based canvas using braille or block octant characters.

  Bytemap creates bitmaps using Unicode characters that divide a character cell
  into a 2x4 grid of pixels: braille patterns by default, or the Unicode 16
  block octants. This enables reasonably high-resolution text-based graphics in
  terminal output."
  (:require [bytemap.schema :as schema]
            [bytemap.util :as util]
            [clojure.string :as s]
            [still.core :refer [snap!]]))

;; Malli Schemas

;; Constants
(def ^:private braille-offset 0x2800)

(def ^:private block-offset 0x1CD00)

(def ^:private legacy-blocks
  "Octant patterns Unicode encodes outside the Block Octants range: the space,
  the quadrants, and the half and quarter blocks that predate it.

  Keys are row-major octant bitmasks, bit 0 top-left through bit 7 bottom-right."
  {2r00000000 0x0020  ; space
   2r00000001 0x1CEA8 ; left half upper one quarter
   2r00000010 0x1CEAB ; right half upper one quarter
   2r00000011 0x1FB82 ; upper one quarter
   2r00000101 0x2598  ; quadrant upper left
   2r00001010 0x259D  ; quadrant upper right
   2r00001111 0x2580  ; upper half
   2r00010100 0x1FBE6 ; middle left one quarter
   2r00101000 0x1FBE7 ; middle right one quarter
   2r00111111 0x1FB85 ; upper three quarters
   2r01000000 0x1CEA3 ; left half lower one quarter
   2r01010000 0x2596  ; quadrant lower left
   2r01010101 0x258C  ; left half
   2r01011010 0x259E  ; quadrant upper right and lower left
   2r01011111 0x259B  ; quadrant upper left, upper right and lower left
   2r10000000 0x1CEA0 ; right half lower one quarter
   2r10100000 0x2597  ; quadrant lower right
   2r10100101 0x259A  ; quadrant upper left and lower right
   2r10101010 0x2590  ; right half
   2r10101111 0x259C  ; quadrant upper left, upper right and lower right
   2r11000000 0x2582  ; lower one quarter
   2r11110000 0x2584  ; lower half
   2r11110101 0x2599  ; quadrant upper left, lower left and lower right
   2r11111010 0x259F  ; quadrant upper right, lower left and lower right
   2r11111100 0x2586  ; lower three quarters
   2r11111111 0x2588})  ; full block

;; Core Functions

(defn braille
  "Converts a byte (0-255) to a braille Unicode character.

  Each bit in the byte corresponds to one of the 8 dots in a braille character.
  The byte is added to the braille Unicode offset (0x2800) to get the final character."
  {:malli/schema [:function [:=> [:cat schema/ByteValue] :string]]}
  [byte-val]
  (str (char (+ braille-offset byte-val))))

(snap! (braille 0) "⠀")
(snap! (braille 64) "⡀")
(snap! (braille 255) "⣿")

(defn bit-of-subpixel
  "Maps a subpixel coordinate [x y] to its corresponding bit position (0-7).

  The mapping follows the braille standard layout:
  - x is 0-1 (left or right column)
  - y is 0-3 (top to bottom)

  The bit layout is:
    [0,0]=0  [1,0]=3
    [0,1]=1  [1,1]=4
    [0,2]=2  [1,2]=5
    [0,3]=6  [1,3]=7"
  {:malli/schema [:function [:=> [:cat schema/Subpixel] schema/Bit]]}
  [[x y]]
  (if (= y 3) (+ 6 x) (+ (* 3 x) y)))

(snap! (bit-of-subpixel [0 0]) 0)
(snap! (bit-of-subpixel [1 3]) 7)

(def ^:private block-chars
  "Byte value (in braille bit order) to block octant character.

  Unicode encodes 230 of the 256 patterns in the Block Octants range, in
  ascending order of a row-major bitmask, skipping the 26 patterns already
  encoded elsewhere. Bytes are stored in braille bit order, so the table is
  built pre-permuted and indexes exactly like `braille`."
  (let [codepoints (:chars (reduce (fn [{:keys [chars cp]} pattern]
                                     (if-let [legacy (legacy-blocks pattern)]
                                       {:chars (conj chars legacy)
                                        :cp    cp}
                                       {:chars (conj chars cp)
                                        :cp    (inc cp)}))
                                   {:chars []
                                    :cp    block-offset}
                                   (range 256)))
        octant-bit (into {}
                         (for [x (range 2)
                               y (range 4)]
                           [(bit-of-subpixel [x y]) (+ (* 2 y) x)]))]
    (mapv (fn [byte-val]
            (let [pattern (reduce (fn [pattern bit]
                                    (cond-> pattern
                                      (bit-test byte-val bit)
                                      (bit-set (octant-bit bit))))
                                  0
                                  (range 8))
                  cp      (nth codepoints pattern)]
              ;; Most octants sit above the BMP, so a single char will not
              ;; do
              #?(:clj (String. (Character/toChars cp))
                 :cljs (js/String.fromCodePoint cp))))
          (range 256))))

(defn block
  "Converts a byte (0-255) to a Unicode block octant character.

  Each bit in the byte corresponds to one of the 8 cells of the 2x4 grid, using
  the same layout as `braille`."
  {:malli/schema [:function [:=> [:cat schema/ByteValue] :string]]}
  [byte-val]
  (nth block-chars byte-val))

(snap! (block 0) " ")
(snap! (block 3) "▘")
(snap! (block 255) "█")

(defn ^:private style->glyph
  "Returns the byte to character function for a canvas style."
  [style]
  (case style
    :braille braille
    :blocks block
    #?(:clj (throw (RuntimeException. (str "Unknown style " style)))
       :cljs (throw (str "Unknown style " style)))))

(defn set-subpixel
  "Sets or clears a specific subpixel in a byte value.

  Returns a new byte with the specified subpixel bit set or cleared."
  {:malli/schema [:function
                  [:=> [:cat schema/ByteValue schema/Subpixel :any]
                   schema/ByteValue]]}
  [num subpixel value]
  (util/set-bit num (bit-of-subpixel subpixel) value))

(snap! (set-subpixel 0 [0 0] true) 1)
(snap! (set-subpixel 255 [0 0] false) 254)

(defn new-canvas
  "Creates a new canvas with the specified width and height in 'pixels'.

  Each pixel is a character representing a 2x4 grid of subpixels. So, a 10x5
  canvas has dimensions of 20x20 in subpixel coordinates.

  Options:
  - :style - :braille (default) or :blocks, the characters the canvas renders
             with. :blocks needs a font with Unicode 16 block octants."
  {:malli/schema [:function [:=> [:cat :int :int] schema/Canvas]
                  [:=> [:cat :int :int [:* :any]] schema/Canvas]]}
  [width height &
   {:keys [style]
    :or   {style :braille}}]
  {:height height
   :pixels (vec (repeat (* width height) 0))
   :style  style
   :width  width})

(snap! (new-canvas 10 5)
       {:height 5
        :pixels [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
                 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
        :style  :braille
        :width  10})

(defn bounds
  "Returns the canvas dimensions in subpixels [width height], where each pixel is 2x4 subpixels."
  {:malli/schema [:function [:=> [:cat schema/Canvas] [:tuple :int :int]]]}
  [{:keys [width height]}]
  [(* 2 width) (* 4 height)])

(snap! (bounds (new-canvas 10 5)) [20 20])

(defn draw-point
  "Draws a point at subpixel coordinates [x y] on the canvas.

  Returns a new canvas with the point drawn. Coordinates are rounded to
  nearest integer. Points outside canvas bounds are silently ignored.

  The optional value parameter determines whether to set (true) or clear (false)
  the point. Defaults to true."
  {:malli/schema [:function
                  [:=> [:cat schema/Canvas schema/Point] schema/Canvas]
                  [:=> [:cat schema/Canvas schema/Point :any] schema/Canvas]]}
  ([canvas point] (draw-point canvas point true))
  ([canvas [x y] value]
   (let [{:keys [width height _]} canvas
         x       (Math/round (double x))
         y       (Math/round (double y))
         pixel-x (util/idiv x 2)
         pixel-y (util/idiv y 4)]
     (if (or (< pixel-x 0) (< pixel-y 0) (>= pixel-x width) (>= pixel-y height))
       canvas ; out of bounds, return unchanged
       (let [pixel-ix (+ (* pixel-y width) pixel-x)
             subpixel [(mod x 2) (mod y 4)]]
         (update
          canvas
          :pixels
          (fn [pixels]
            (update pixels pixel-ix #(set-subpixel % subpixel value)))))))))

(snap! (-> (new-canvas 4 2)
           (draw-point [4 4]))
       {:height 2
        :pixels [0 0 0 0 0 0 1 0]
        :style  :braille
        :width  4})

(defn canvas->string
  "Converts a canvas to a string representation, using the canvas style.

  Returns a multi-line string where each line represents one row of the canvas."
  {:malli/schema [:function [:=> [:cat schema/Canvas] :string]]}
  [{:keys [width height pixels style]
    :or   {style :braille}}]
  (let [glyph (style->glyph style)]
    (apply str
           (for [y (range height)]
             (str (apply str
                         (for [x    (range width)
                               :let [i (+ (* y width) x)]]
                           (glyph (nth pixels i))))
                  (when (< y (dec height)) "\n"))))))

(snap! (str "\n"
            (-> (new-canvas 5 3)
                (draw-point [5 6])
                (canvas->string)))
       "
⠀⠀⠀⠀⠀
⠀⠀⠠⠀⠀
⠀⠀⠀⠀⠀")

(defn print-canvas!
  "Prints a canvas to stdout using the canvas style.

  Outputs line-by-line to avoid buffer boundary issues with multibyte characters."
  {:malli/schema [:function [:=> [:cat schema/Canvas] :nil]]}
  [canvas]
  (let [s     (canvas->string canvas)
        lines (s/split s #"\n")]
    (doseq [line lines]
      (println line)))
  nil)

;; Vector operations

(defn ^:private span
  "Calculates the span between two points along an axis (0=x, 1=y)."
  [axis p0 p1]
  (- (nth p1 axis) (nth p0 axis)))

(defn ^:private sign
  "Returns the sign of a number: -1, 0, or 1."
  [x]
  (cond (< x 0) -1
        (> x 0) 1
        :else 0))

(defn ^:private make-vec2
  "Constructs a 2D vector from major/minor axis values.

  major-axis is 0 for x, 1 for y."
  [major-axis major minor]
  (case major-axis
    0 [major minor]
    1 [minor major]))

(defn draw-line
  "Draws a line from start point to end point using Bresenham’s algorithm.

  Returns a new canvas with the line drawn. Both start and end are subpixel
  coordinates."
  {:malli/schema [:function
                  [:=> [:cat schema/Canvas schema/Point schema/Point]
                   schema/Canvas]]}
  [canvas start end]
  (let [x-axis      0
        y-axis      1
        x-span      (span x-axis start end)
        y-span      (span y-axis start end)
        ;; Determine major and minor axes
        [major-axis minor-axis] (if (< (Math/abs y-span) (Math/abs x-span))
                                  [x-axis y-axis]
                                  [y-axis x-axis])
        ;; Ensure we draw from lower to higher major coordinate
        [start end] (if (< (nth start major-axis) (nth end major-axis))
                      [start end]
                      [end start])
        minor-step  (sign (- (nth end minor-axis) (nth start minor-axis)))
        run         (- (nth end major-axis) (nth start major-axis))
        rise        (Math/abs (- (nth end minor-axis) (nth start minor-axis)))]
    (loop [canvas canvas
           major  (nth start major-axis)
           minor  (nth start minor-axis)
           err    (- (* 2 rise) run)]
      (if (> major (nth end major-axis))
        canvas
        (let [canvas      (draw-point canvas (make-vec2 major-axis major minor))
              [minor err] (if (> err 0)
                            [(+ minor minor-step) (- err (* 2 run))]
                            [minor err])]
          (recur canvas (inc major) minor (+ err (* 2 rise))))))))

(snap! (-> (new-canvas 10 5)
           (draw-line [0 0] [20 20])
           (draw-line [0 20] [20 0]))
       {:height 5
        :pixels [17 132 0 0 0 0 0 0 128 20 0 0 17 132 0 0 128 20 1 0 0 0 0 0 145
                 148 1 0 0 0 0 0 128 20 1 0 17 132 0 0 128 20 1 0 0 0 0 0 17
                 132]
        :style  :braille
        :width  10})
