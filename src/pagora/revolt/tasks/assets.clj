(ns pagora.revolt.tasks.assets
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [revolt.utils :as utils])
    (:import java.util.zip.GZIPInputStream
             java.util.zip.GZIPOutputStream
             (java.io File)
             (java.nio.file StandardCopyOption)
             (java.nio.file Files)))

(def ^:const buffer-len 2048)

(def copy-options
  (into-array StandardCopyOption [StandardCopyOption/REPLACE_EXISTING]))

(defn consume-input-stream
  [input-stream]
  (let [buffer (byte-array buffer-len)]
    (while (> (.read input-stream buffer 0 buffer-len) 0))))

(defn mk-sha256
  [file]
  (.getMessageDigest
    (doto
      (java.security.DigestInputStream.
        (io/input-stream file)
        (java.security.MessageDigest/getInstance "SHA-256"))
      consume-input-stream)))

(defn hashed-name
  [file]
  (let [digest (mk-sha256 file)]
    (str (javax.xml.bind.DatatypeConverter/printHexBinary (.digest digest)) "-" (.getName file))))

(defn excluded?
  [path exclude-paths]
  (some #(.startsWith path %) exclude-paths))

(defn copy-assets
  [path output-path exclude-paths]
  (let [assets-path (.toPath path)]
    (doseq [file (file-seq path)]
      (let [file-path (.toPath file)]
        (when (and (.isFile file)
                   (not (excluded? file-path exclude-paths)))
          (let [relative-output (.relativize assets-path file-path)
                destination (io/file output-path (.toString relative-output))]
            (io/make-parents destination)
            (Files/copy file-path (.toPath destination) copy-options)))))))

(defn fingerprint
  "Fingerprinting resources"
  [path public-path]
  (let [assets-file (io/file path public-path)
        assets-path (.toPath assets-file)]
    (filter
     (complement nil?)
     (for [file (file-seq assets-file)
           :when (and (.isFile file) (not (.endsWith (.getName file) ".DS_Store")))
           :let  [destination (io/file (.getParent file) (hashed-name file))]]
       (when (.renameTo file destination)
         (log/infof "%s => %s" file destination)
         [(str (.relativize assets-path (.toPath file)))
          (str (.relativize assets-path (.toPath destination)))])))))

(defn replace-all
  [text patterns]
  (loop [input text, pat patterns]
    (let [[pattern replacement] (first pat)]
      (if-not pattern
        input
        (recur (str/replace input pattern replacement)
               (rest pat))))))

(defn morph-resource
  [assets-kv extensions patterns file entry-name]
  (when (and file (not (assets-kv entry-name)))
    (if (some #(.endsWith (.toLowerCase entry-name) %) extensions)
      (let [tmp-file (File/createTempFile entry-name ".tmp")]
        (log/info "looking for assets in:" entry-name)
        (.deleteOnExit tmp-file)
        (spit tmp-file (replace-all (slurp file) patterns))
        tmp-file)
      file)))

(defn ^:private bytes->human-readable [bytes & [si?]]
  (let [unit (if si? 1000 1024)]
    (if (< bytes unit) (str bytes " B")
        (let [exp (int  (/ (java.lang.Math/log bytes)
                           (java.lang.Math/log unit)))
              pre (str (nth (if si? "kMGTPE" "KMGTPE") (dec exp)) (if-not si? "i" ))]
          (format "%.1f %sB" (/ bytes (Math/pow unit exp)) pre)))))

(defn ^:private percent-saved [old new]
  (if (not= old 0)
    (-> (* 100 (/ (- old new) old))
        float
        java.lang.Math/round)
    0))

(defn ^:private gzip-file
  "Writes the contents of input to output, compressed.
  input: something which can be copied from by io/copy.
  output: something which can be opend by io/output-stream.
      The bytes written to the resulting stream will be gzip compressed."
  [input output & opts]
  (with-open [output (-> output io/output-stream GZIPOutputStream.)]
    (apply io/copy input output opts)))

(defn invoke
  [ctx {:keys [assets-paths exclude-paths update-with-exts options public-path gzip]} classpaths target]
  (let [assets-path (utils/ensure-relative-path target "assets")
        extensions (map #(str "." (.toLowerCase %)) update-with-exts)]

    (utils/timed
     "COPYING ASSETS"
     (doseq [path (map io/file assets-paths)]
       (copy-assets path assets-path exclude-paths)))

    (let [assets-kv (utils/timed
                     "FINGERPRINTING"
                     (into {} (fingerprint assets-path public-path)))
          assets-kv (if gzip
                      (utils/timed
                       "GZIPPING"
                       (let [assets-path (str assets-path "/" public-path "/")]
                         (loop [assets-kv assets-kv new-assets-kv {}]
                           (if-let [[asset fingerprintend-asset] (first assets-kv)]
                             (let [in (io/file (str assets-path fingerprintend-asset))
                                   target (str fingerprintend-asset ".gz")
                                   out (io/file assets-path target)]
                               (gzip-file in out)
                               (io/delete-file in)
                               (let [origin-size (.length in)
                                     new-size    (.length out)]
                                 (log/info (format "Gzipped %s (%s) to %s (%s), saving %s%%\n"
                                                   fingerprintend-asset (bytes->human-readable origin-size)
                                                   target (bytes->human-readable new-size)
                                                   (percent-saved origin-size new-size))))
                               (recur (rest assets-kv) (assoc new-assets-kv asset target)))
                             new-assets-kv))))
                      assets-kv)]

      (let [patterns (map #(vector (re-pattern (first %)) (second %)) assets-kv)]
        (-> ctx
            (assoc  :assets assets-kv)
            (update :before-pack-fns conj (partial morph-resource assets-kv extensions patterns)))))))

      ;; (utils/timed
      ;;  "FINGERPRINTING"
      ;;  (let [assets-kv (into {} (fingerprint assets-path public-path))
      ;;        patterns  (map #(vector (re-pattern (first %)) (second %)) assets-kv)]
      ;;    (-> ctx
      ;;        (assoc  :assets assets-kv)
      ;;        (update :before-pack-fns conj (partial morph-resource assets-kv extensions patterns)))))
