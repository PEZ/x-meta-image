(ns pez.x-meta.compose
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [membrane.skia :as skia]
            [membrane.skia.paragraph :as para]
            [membrane.ui :as ui]
            [pez.x-meta.fs :as meta-fs])
  (:import [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))

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

;; Only does shrinking
(defn aspect-fill [[w h] elem]
  (let [[ew eh] (ui/bounds elem)
        sx (/ w ew)
        sy (/ h eh)
        s (min sx sy)]
    (ui/scale s s
              elem)))

(defn aspect-scale-to-width [w elem]
  (aspect-fill [w (second (ui/bounds elem))] elem))

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

(defn add-texts! [texts elem]
  (let [overlay (create-text-overlay (ui/bounds elem) texts)]
    [elem overlay]))

(defn load-image [image-path]
  (ui/image image-path))

;; The Skia backend does not support converting to an image blob (I think)
;; So we go via saving to a temp PNG file and reading it back up again
(defn ->imageIO-image [elem]
  (let [temp-path (str (meta-fs/tmp-path! "membrane-to-imageIO" (str (gensym "image-") ".png")))
        _image? (skia/save-image temp-path elem nil nil 0 false) ;; TODO: Handle error?
        file (io/as-file temp-path)]
    (ImageIO/read file)))

;; ImageIO/write does not want to produce JPEGs fromimages with alpha layers
(defn imageIO-rgba->rgb [image]
  (let [width (.getWidth image)
        height (.getHeight image)
        rgb-image (BufferedImage. width height BufferedImage/TYPE_INT_RGB)
        g (.createGraphics rgb-image)]
    (.drawImage g image 0 0 nil)
    (.dispose g)
    rgb-image))

(defn imageIO-save-as! [format new-path image]
  (let [new-image (if (= format "jpg")
                    (imageIO-rgba->rgb image)
                    image)
        new-image-file (io/as-file new-path)]
    (if (ImageIO/write new-image format new-image-file)
      new-image-file
      (throw (ex-info "Failed to write image" {:format format :path new-path})))))

;; Membrane does not support saving JPEG (I think)
(defn save-as! [format path elem]
  (imageIO-save-as! format path (->imageIO-image elem)))

;; Facebook, LinkedIn, Slack, etcetera do not hide article info
;; so we don't overlay the info on the image
(defn create-share-image! [image-path new-path]
  (->> (load-image image-path)
       (crop-to-height 675 false)
       (save-as! "jpg" new-path)))

;; For X/Twitter we do the overlay thing
(defn create-twitter-image! [image-path new-path {:keys [title description author]}]
  (let [description-max 100
        shortened-description (if (> (count description) description-max)
                                (str (subs description 0 description-max) "…")
                                description)]
    (->> (load-image image-path)
         (crop-to-height 675 true)
         (add-texts! {:title title
                      :author author
                      :description shortened-description})
         (save-as! "jpg" new-path))))

;; The Playground is this Rich Comment Form
(comment
  (def original-path "assets/kitten.png")
  (def article-meta {:title "Adding Back Article Information to Links on X"
                     :author "Peter Strömberg a.k.a. PEZ"
                     :description "Placing article header, author, and description as an image overlay on X Link Share Images."})

  ;; Writing to an image
  (->> (ui/image original-path)
       (aspect-scale-to-width 1200)
       (crop-to-height 675 true)
       (add-texts! article-meta)
       (save-as! "jpg" (meta-fs/replace-ext original-path "-twitter.jpg")))

  ;; Use Membrane UI to work more interactively with the composition

  ;; Show errors in the UI
  (defmacro wrap-errors [body]
    `(try
       (ui/try-draw
        ~body
        (fn [draw# e#]
          (draw# (ui/label e#))))
       (catch Exception e#
         (ui/label e#))))

  ;; Evaluate `debug-img` and open the file in your editor
  ;; (Handy if you have only one screen.)
  (defonce debug-img (str (fs/create-temp-file {:prefix "debug-img-" :suffix ".png"})))

  (defn debug [window-info]
    (wrap-errors
     (let [[cw ch] (:container-size window-info)]
       (aspect-fill
        [cw ch]
        (let [elem
              (->> (ui/image original-path)
                   (aspect-scale-to-width 1200)
                   (crop-to-height 675 true)
                   (add-texts! article-meta))]
          (skia/save-image debug-img elem) ; This writes the debug image
          elem)))))

  ;; Start the UI app
  (skia/run
   #'debug
   {:include-container-info true})



  ;; Membrane Skia backend Hello World
  (skia/run (constantly
             (ui/label "hello World")))

  :rcf)
