# Bytemap (Clojure)

Bytemap is a library for creating text-based graphics using Unicode braille characters. Each braille character contains 8 "pixels" arranged in a 2x4 grid, allowing for reasonably high-resolution terminal output.

This is an idiomatic Clojure/ClojureScript port of the [Janet implementation](../janet).

## Installation

Add to your `deps.edn`:

```clojure
{:deps {bytemap/bytemap {:local/root "path/to/bytemap/clojure"}}}
```

## Usage

### Basic Drawing

```clojure
(require '[bytemap.core :as bm])

;; Create a canvas and draw points
(-> (bm/new-canvas 10 5)
    (bm/draw-point [10 10])
    (bm/print-canvas!))

;; Draw lines
(-> (bm/new-canvas 10 5)
    (bm/draw-line [0 0] [20 20])
    (bm/draw-line [0 20] [20 0])
    (bm/print-canvas!))

;; ⠑⢄⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠑⢄⠀⠀⠀⢀⠔⠁
;; ⠀⠀⠀⠀⠑⢄⢀⠔⠁⠀
;; ⠀⠀⠀⢀⠔⠁⠑⢄⠀⠀
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

### Plotting Functions

```clojure
;; Plot a sine wave
(bm/plot #(Math/sin %) [40 10] Math/PI 1)

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

;; Plot without axes
(bm/plot #(Math/cos %) [20 10] Math/PI 1 :axis false)
```

### Working with Canvas as Data

```clojure
;; Canvas is an immutable data structure
(let [canvas (bm/new-canvas 10 5)
      canvas-with-line (bm/draw-line canvas [0 0] [20 20])]
  ;; Original canvas is unchanged
  (bm/canvas->string canvas)           ;; => blank canvas
  (bm/canvas->string canvas-with-line)) ;; => canvas with line
```

## API

### Canvas Creation and Rendering

- `(new-canvas width height)` - Creates a new canvas. Dimensions are in "pixels" (braille characters), where each pixel is 2x4 subpixels.
- `(bounds canvas)` - Returns `[width height]` in subpixels.
- `(canvas->string canvas)` - Converts canvas to a string.
- `(print-canvas! canvas)` - Prints canvas to stdout (side-effecting).

### Drawing Functions

- `(draw-point canvas [x y])` - Draws a point at subpixel coordinates. Returns new canvas.
- `(draw-point canvas [x y] false)` - Clears a point. Returns new canvas.
- `(draw-line canvas [x1 y1] [x2 y2])` - Draws a line using Bresenham's algorithm. Returns new canvas.
- `(plot f [w h] x-scale y-scale & {:keys [axis]})` - Plots a function. Prints output and returns nil.

### Low-level Functions

- `(braille byte-val)` - Converts a byte (0-255) to a braille character.
- `(bit-of-subpixel [x y])` - Maps subpixel coordinates to bit position.
- `(set-subpixel num [x y] value)` - Sets or clears a specific subpixel bit.

## Testing

Run tests with:

```bash
clojure -M:test -e '(require (quote bytemap.drawing-test)) (clojure.test/run-tests (quote bytemap.drawing-test))'
```

## Implementation Notes

### Coordinate System

Bytemap uses a 2x4 pixel grid (not 3x6). This deliberate design choice avoids information loss from phantom dots that cannot be rendered in braille characters. While this causes slight distortion in straight lines, it preserves more visual information.

- Canvas dimensions are specified in "pixels" (braille characters)
- Each pixel contains 2x4 subpixels
- Drawing coordinates are in subpixels
- Coordinates are automatically rounded to nearest integer
- Out-of-bounds coordinates are silently clipped

### Immutability

The Clojure implementation uses an immutable API:

- All drawing functions return a new canvas
- Original canvas is never modified
- Use threading macros (`->`) for chaining operations
- Internally uses transients for performance during canvas building

### Malli Schemas

All public functions include Malli schemas for validation:

```clojure
(require '[malli.core :as m])
(require '[bytemap.core :as bm])

(m/validate [:=> [:cat bm/Canvas bm/Point] bm/Canvas]
            bm/draw-point)  ;; => true
```

### ClojureScript Support

All code is in `.cljc` files and works in both Clojure and ClojureScript with minimal reader conditionals.

## Differences from Janet Implementation

1. **Immutability** - Clojure version returns new canvases; Janet version mutates in place
2. **Function plotting** - Minor floating-point rounding differences in output
3. **Schemas** - Clojure version includes Malli schemas for validation
4. **Unicode handling** - Clojure handles Unicode natively; no manual UTF-8 encoding needed

## License

Same license as the Janet implementation (see parent directory).
