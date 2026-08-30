[![Clojars Project](https://img.shields.io/clojars/v/dev.tomwaddington/bytemap.svg)](https://clojars.org/dev.tomwaddington/bytemap)

# Bytemap (Clojure)

`bytemap` is a library for creating text-based graphics using Unicode braille or block octant characters.

Each character contains 8 “pixels” arranged in a 2x4 grid, allowing for reasonably high-resolution terminal output.

This is a Clojure(Script) port of [Ian Henry’s Janet library](https://github.com/ianthehenry/bytemap).

## Installation

Add to your `deps.edn`:

```clojure
{:deps {dev.tomwaddington/bytemap.clj {:mvn/version "RELEASE"}}
```

## Usage

### Basic Drawing

```clojure
(require '[bytemap.core :as bm]
         '[bytemap.plot :as bp])

;; Create a canvas and draw points
(-> (bm/new-canvas 10 5)
    (bm/draw-point [10 10])
    (bm/print-canvas!))
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠄⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

;; Draw lines
(-> (bm/new-canvas 10 5)
    (bm/draw-line [0 0] [20 20])
    (bm/draw-line [0 20] [20 0])
    (bm/print-canvas!))
;; ⠑⢄⠀⠀⠀⠀⠀⠀⢀⠔
;; ⠀⠀⠑⢄⠀⠀⢀⠔⠁⠀
;; ⠀⠀⠀⠀⢑⢔⠁⠀⠀⠀
;; ⠀⠀⢀⠔⠁⠀⠑⢄⠀⠀
;; ⢀⠔⠁⠀⠀⠀⠀⠀⠑⢄
```

### Drawing the Union Jack

```clojure
(let [canvas (bm/new-canvas 10 5)
      ;; Diagonal lines
      canvas (reduce (fn [c x]
                       (-> c
                           (bm/draw-point [x x])
                           (bm/draw-point [x (- 20 x)])))
                     canvas
                     (range 21))
      ;; Cross lines
      canvas (reduce (fn [c x]
                       (-> c
                           (bm/draw-point [10 x])
                           (bm/draw-point [x 10])))
                     canvas
                     (range 21))]
  (bm/print-canvas! canvas))
;; ⠑⢄⠀⠀⠀⡇⠀⠀⢀⠔
;; ⠀⠀⠑⢄⠀⡇⢀⠔⠁⠀
;; ⠤⠤⠤⠤⢵⣷⠥⠤⠤⠤
;; ⠀⠀⢀⠔⠁⡇⠑⢄⠀⠀
;; ⢀⠔⠁⠀⠀⡇⠀⠀⠑⢄
```

### Block Octants

Canvases render with braille by default. Pass `:style :blocks` to use the
Unicode 16 block octants instead. The previous example drawn with blocks:

```clojure
(-> (bm/new-canvas 10 5 :style :blocks)
    (bm/draw-line [0 0] [20 20])
    (bm/draw-line [0 20] [20 0])
    (bm/print-canvas!))
;; 𜴄𜶀      𜺠𜴐
;;   𜴄𜶀  𜺠𜴐𜺨
;;     𜵹𜶈𜺨
;;   𜺠𜴐𜺨 𜴄𜶀
;; 𜺠𜴐𜺨     𜴄𜶀
```

Note that block octants were added in Unicode 16.0 (September 2024) and font
coverage is still thin.

Empty cells render as spaces rather than the blank braille pattern, so block
output has trailing whitespace where braille output does not.

### Plotting Functions

```clojure
;; Plot a sine wave (prints to stdout)
(bp/print-plot! #(Math/sin %) [40 10] Math/PI 1)
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⢀⠤⠖⠚⠒⠒⢤⡀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠈⠢⡀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⢠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠱⡀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡷⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢄
;; ⠹⡉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⢉⠝⡏⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉
;; ⠀⠘⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠊⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠈⠢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⠁⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀⡠⠊⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠑⢤⣀⣀⢀⣀⡤⠊⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

;; Get plot as a string (no side effects)
(def plot-str (bp/plot->string #(Math/cos %) [40 10] Math/PI 1))

;; Plot without axes
(bp/print-plot! #(Math/sin %) [40 10] Math/PI 1 :axis false)
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠤⠖⠚⠒⠒⢤⡀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠈⠢⡀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠔⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢆⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠱⡀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢄
;; ⠱⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠜⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠘⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠈⠢⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡰⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀⡠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠑⢤⣀⣀⢀⣀⡤⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
```

### Plotting a Histogram

```clojure
;; Plot a histogram of frequencies, with stats (prints to stdout)
(bp/plot-histogram [1 1 2 2 2 3 3 3 3 3 4 4])
;; ⠀⠀⣿⠀
;; ⠀⠀⣿⠀
;; ⠀⣿⣿⠀
;; ⣿⣿⣿⣿
;; ⣿⣿⣿⣿
;;   μ = 2.583333
;;   σ = 1.048445

;; Horizontal orientation
(bp/plot-histogram [1 1 2 2 2 3 3 3 3 3 4 4] :orientation :horizontal)
;; ⣿⡇⠀⠀
;; ⣿⣷⡆⠀
;; ⣿⣿⣧⣤
;; ⣿⡿⠿⠿
;; ⣿⡇⠀⠀
;;   μ = 2.583333
;;   σ = 1.048445

;; Build a histogram canvas directly, without the frequencies/stats wrapper
(-> (bm/new-canvas 10 5)
    (bp/histogram (sorted-map 0 1, 1 3, 2 5, 3 2))
    (bm/print-canvas!))
;; ⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀
;; ⠀⠀⠀⠀⠀⣿⣿⡇⠀⠀
;; ⠀⠀⢸⣿⣿⣿⣿⡇⠀⠀
;; ⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿
;; ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
```

### Working with Canvas as Data

```clojure
;; Canvas is an immutable data structure
(let [canvas (bm/new-canvas 10 5)
      canvas-with-line (bm/draw-line canvas [0 0] [20 20])]
  ;; Original canvas is unchanged
  (bm/canvas->string canvas)            ;; => blank canvas
  (bm/canvas->string canvas-with-line)) ;; => canvas with line
```

## API

### Canvas Creation and Rendering (bytemap.core)

- `(new-canvas width height & {:keys [style]})` - Creates a new canvas. Dimensions are in “pixels” (characters), where each pixel is 2x4 sub-pixels. `:style` is `:braille` (default) or `:blocks`.
- `(bounds canvas)` - Returns `[width height]` in sub-pixels.
- `(canvas->string canvas)` - Converts canvas to a string.
- `(print-canvas! canvas)` - Prints canvas to standard output (side-effecting).

### Drawing Functions (bytemap.core)

- `(draw-point canvas [x y])` - Draws a point at sub-pixel coordinates. Returns new canvas.
- `(draw-point canvas [x y] false)` - Clears a point. Returns new canvas.
- `(draw-line canvas [x1 y1] [x2 y2])` - Draws a line using Bresenham’s algorithm. Returns new canvas.

### Plotting Functions (bytemap.plot)

- `(plot canvas f & {:keys [axis x-scale y-scale]})` - Plots a function.
- `(plot->string f [w h] x-scale y-scale & {:keys [axis style]})` - Plots a function and returns the string representation.
- `(print-plot! f [w h] x-scale y-scale & {:keys [axis style]})` - Plots a function, prints to standard output, and returns nil.
- `(histogram canvas bins & {:keys [orientation]})` - Draws a histogram of `bins` (a map of value → count, e.g. `clojure.core/frequencies` output) on a canvas, with `:vertical` (default) or `:horizontal` bars. Downsamples via `bytemap.util/downsample-histogram` if `bins` has more entries than available columns/rows. Returns new canvas.
- `(plot-histogram xs & {:keys [w h stats orientation style]})` - Convenience function: computes `(frequencies xs)`, builds and prints a histogram sized to fit (or to explicit `w`/`h` pixels), and prints `μ`/`σ` statistics by default (`:stats false` to suppress). Returns nil.

### Low-Level Functions (bytemap.core)

- `(braille byte-val)` - Converts a byte (0–255) to a braille character.
- `(block byte-val)` - Converts a byte (0–255) to a block octant character.
- `(bit-of-subpixel [x y])` - Maps sub-pixel coordinates to bit position.
- `(set-subpixel num [x y] value)` - Sets or clears a specific sub-pixel bit.

## License

Copyright © 2025–2026 Tom Waddington

Distributed under the MIT License. See LICENSE file for details.
