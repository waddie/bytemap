(ns bytemap.schema)

(def Bit
  "Schema for bit positions (0-7)"
  [:int
   {:max 7
    :min 0}])

(def ByteValue
  "Schema for byte values (0-255)"
  [:int
   {:max 255
    :min 0}])

(def Point "Schema for a 2D point [x y]" [:tuple number? number?])

(def Subpixel
  "Schema for a subpixel coordinate [x y] where x is 0-1, y is 0-3"
  [:tuple
   [:int
    {:max 1
     :min 0}]
   [:int
    {:max 3
     :min 0}]])

(def Style
  "Schema for the characters a canvas renders with"
  [:enum :blocks :braille])

(def Canvas
  "Schema for a canvas data structure"
  [:map
   [:width :int]
   [:height :int]
   [:pixels [:vector ByteValue]]
   [:style {:optional true} Style]])
