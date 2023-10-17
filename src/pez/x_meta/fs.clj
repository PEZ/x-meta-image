(ns pez.x-meta.fs
  (:require [babashka.fs :as fs]))

(defn tmp-path!
  [slug file-name]
  (let [path (fs/path (fs/temp-dir) "x-meta" slug file-name)]
    (fs/create-dirs (fs/parent path))
    path))

(defn replace-ext [path new-ext]
  (str (first (fs/split-ext path)) new-ext))