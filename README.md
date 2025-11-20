# bytemap (Clojure)

`bytemap` is a library for creating text-based graphics using Unicode braille characters. Each braille character contains 8 “pixels” arranged in a 2x4 grid, allowing for reasonably high-resolution terminal output.

This is a Clojure(Script) port of [Ian Henry’s Janet library](https://github.com/ianthehenry/bytemap).

## Installation

Add to your `deps.edn`:

```clojure
{:deps {io.github.waddie/bytemap.clj {:git/sha "…"}}
```

## Usage

### Basic drawing

```clojure
(require '[bytemap.core :as bm])

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

### Plotting functions

```clojure
;; Plot a sine wave (prints to stdout)
(bm/print-plot! #(Math/sin %) [40 10] Math/PI 1)
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
(def plot-str (bm/plot->string #(Math/cos %) [40 10] Math/PI 1))

;; Plot without axes
(bm/print-plot! #(Math/sin %) [40 10] Math/PI 1 :axis false)
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

### Working with canvas as data

```clojure
;; Canvas is an immutable data structure
(let [canvas (bm/new-canvas 10 5)
      canvas-with-line (bm/draw-line canvas [0 0] [20 20])]
  ;; Original canvas is unchanged
  (bm/canvas->string canvas)            ;; => blank canvas
  (bm/canvas->string canvas-with-line)) ;; => canvas with line
```

## API

### Canvas creation and rendering

- `(new-canvas width height)` - Creates a new canvas. Dimensions are in “pixels” (braille characters), where each pixel is 2x4 sub-pixels.
- `(bounds canvas)` - Returns `[width height]` in sub-pixels.
- `(canvas->string canvas)` - Converts canvas to a string.
- `(print-canvas! canvas)` - Prints canvas to standard output (side-effecting).

### Drawing functions

- `(draw-point canvas [x y])` - Draws a point at sub-pixel coordinates. Returns new canvas.
- `(draw-point canvas [x y] false)` - Clears a point. Returns new canvas.
- `(draw-line canvas [x1 y1] [x2 y2])` - Draws a line using Bresenham’s algorithm. Returns new canvas.

### Plotting functions

- `(plot->string f [w h] x-scale y-scale & {:keys [axis]})` - Plots a function and returns the string representation.
- `(print-plot! f [w h] x-scale y-scale & {:keys [axis]})` - Plots a function, prints to standard output, and returns nil.

### Low-level functions

- `(braille byte-val)` - Converts a byte (0-255) to a braille character.
- `(bit-of-sub-pixel [x y])` - Maps sub-pixel coordinates to bit position.
- `(set-sub-pixel num [x y] value)` - Sets or clears a specific sub-pixel bit.

## Differences from Janet implementation

1. **Immutability** - Clojure version returns new canvases; Janet version mutates in place
2. **Function plotting** - Minor floating-point rounding differences in output
3. **Schemas** - Clojure version includes Malli schemas for validation
4. **Unicode handling** - Clojure handles Unicode natively; no manual UTF-8 encoding needed
5. **Chunks output for nREPL compatibility** - Braille characters are 3-bytes. nREPL has a 1024-byte output buffer, which will split them and break the output

## License

Copyright © 2025 Tom Waddington

Distributed under the MIT License. See LICENSE file for details.
