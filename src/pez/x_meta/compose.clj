(ns pez.x-meta.compose
  (:require [babashka.fs :as fs]
            [membrane.skia :as skia]
            [membrane.skia.paragraph :as para]
            [membrane.ui :as ui]
            [pez.x-meta.fs :as meta-fs]))

;; Load this file in the REPL
;; Then use the ”Playground” Rich Comment FORM
;; at the bottom of the file

;; The Membrane Skia backend has sweet text layout
(defn create-text [{:keys [width title author description]}]
  (let [style #:text-style
               {:color [1 1 1]
                :font-families ["Nimbus Sans" "Arial"] ;; Use whatever you fancy
                :font-size 42}] ;; Needs to be pretty large, because phones
    (ui/vertical-layout
     (para/paragraph
      {:text title
       :style (merge style #:text-style {:font-style #:font-style {:weight :bold}})}
      width)
     (ui/spacer 0 6)
     (para/paragraph
      {:text author
       :style (merge style #:text-style {:font-style #:font-style {:slant :italic}})}
      width)
     (ui/spacer 0 6)
     (para/paragraph
      {:text description
       :style style}
      width))))

(defn create-text-overlay [[image-width image-height] texts]
  (let [text-padding-w 30
        text-padding-h 20
        margin-bottom 88 ;; X overlays the site domain at the bottom of the image
        text-width (- image-width (* text-padding-w 2))
        text (create-text (assoc texts :width text-width))
        rect-height (+ (ui/height text) (* text-padding-h 2))
        rect-y (- image-height rect-height margin-bottom)
        text-y (+ rect-y text-padding-h)]
    [(ui/translate
      0 rect-y
      (ui/filled-rectangle [0 0 0 0.5] ;; Consider 0.65 transparency
                           image-width
                           rect-height))
     (ui/translate text-padding-w text-y
                   text)]))

;; Crop vertically, keeping the the middle of the image
(defn crop-to-height [target-height pad? elem]
  (let [[w h] (ui/bounds elem)]
    (if (> h target-height)
      (let [y-offset (quot (- h target-height) 2)]
        (ui/scissor-view [0 0] [w target-height]
                         (ui/translate 0 (- y-offset) elem)))
      (if pad?
        (ui/padding 0 0  (- target-height h) 0 elem)
        elem))))

(defn add-texts [texts elem]
  (let [overlay (create-text-overlay (ui/bounds elem) texts)]
    [elem overlay]))

(def ext->skia-format
  {"jpg" ::skia/image-format-jpeg
   "jpeg" ::skia/image-format-jpeg
   "png" ::skia/image-format-png
   "webp" ::skia/image-format-webp})

(defn save! [path format quality elem]
  (skia/save-image path elem nil (ext->skia-format format) quality true))

;; The Playground is this Rich Comment Form
;; Evaluate the top level forms in the `comment` form, one by one
(comment
  (def original-path "assets/kitten.png")
  (def article-meta {:title "Adding Back Article Information to Links on X"
                     :author "Peter Strömberg a.k.a. PEZ"
                     :description "Placing article header, author, and description as an image overlay on X Link Share Images."})

  ;; Writing to an image
  (->> (ui/image original-path [1200 nil]) ;; aspect scales to 1200 in width
       (crop-to-height 675 true)
       (add-texts article-meta)
       (save! (meta-fs/replace-ext original-path "-twitter.jpg")
              "jpg" ; use "png" for lossless compression regardless of quality
              80    ; for "jpg", it's a trade-off file size <-> fidelity
              ))

  ;; Use Membrane UI to work more interactively with the composition.
  ;; Either evaluate the whole `(do ...)` or do it form, by form.
  ;; WHen you have started the UI app (that's the`(skia run ...)` below)
  ;; you can try editing things in the program, evaluate that, and
  ;; see what happens in the UI app.
  ;; Use the `debug-img` to get live updates happening in a file you
  ;; (e.g. in your editor).
  (do
    ;; Show errors in the UI
    (defmacro wrap-errors [body]
      `(try
         (ui/try-draw
          ~body
          (fn [draw# e#]
            (draw# (ui/label e#))))
         (catch Exception e#
           (ui/label e#))))

    ;; We use this to make our composition view fill the full width
    ;; of the `dev-app` window
    (defn aspect-fill [[w h] elem]
      (let [[ew eh] (ui/bounds elem)
            sx (/ w ew)
            sy (/ h eh)
            s (min sx sy)]
        (ui/scale s s
                  elem)))

    ;; Evaluate `debug-img` and open the file in your editor
    ;; (Handy if you have only one screen.)
    (defonce debug-img (str (fs/create-temp-file {:prefix "debug-img-" :suffix ".png"})))

    (defn dev-app [window-info]
      (wrap-errors
       (let [[cw ch] (:container-size window-info)]
         (aspect-fill
          [cw ch]
          (let [elem
                (->> (ui/image original-path [1200 nil])
                     (crop-to-height 675 true)
                     (add-texts article-meta))]
            #_(skia/save-image debug-img elem) ; This writes the debug image
                                               ; Note: Don't do this in prodcution
                                               ; (It also slows down the app A LOT!)
            elem)))))

    ;; Start the UI app
    (skia/run
     #'dev-app
     {:include-container-info true}))



  ;; This is the Membrane Skia backend Hello World
  (skia/run (constantly
             (ui/label "hello World")))

  :rcf)
