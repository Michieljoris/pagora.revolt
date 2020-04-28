(ns pagora.revolt.tasks.gzip-assets
  (:require
   [clojure.java.io :as io]
   [cheshire.core :as json]
   [revolt.utils :as utils]
   [jansi-clj.core :as jansi :refer :all :exclude [reset]]
   [clojure.pprint :refer [pprint]])
  (:import java.util.zip.GZIPInputStream
           java.util.zip.GZIPOutputStream))

;;Copied from org.martinklepsch.boot-gzip


;; (defn invoke [{:keys [public-path assets] :as ctx} options target]
;;   (utils/timed
;;    "GZIP-ASSETS"
;;    (let [assets-path (str (utils/ensure-relative-path target "assets") "/" public-path "/")]
;;      (loop [assets assets ctx ctx]
;;        (if-let [[asset fingerprintend-asset] (first assets)]
;;          (let [in (io/file (str assets-path fingerprintend-asset))
;;                target (str fingerprintend-asset ".gz")
;;                out (io/file assets-path target)]
;;            (gzip-file in out)
;;            (let [origin-size (.length in)
;;                  new-size    (.length out)]
;;              (println (format "Gzipped %s (%s) to %s (%s), saving %s%%\n"
;;                               fingerprintend-asset (bytes->human-readable origin-size)
;;                               target (bytes->human-readable new-size)
;;                               (percent-saved origin-size new-size))))
;;            (recur (rest assets) (update-in ctx [:assets asset] #(str % ".gz"))))
;;          ctx)))))

;; (pprint (invoke {:public-path "public/"
;;           :assets {"admin_new/app.css"
;;                    "admin_new/922A19C4BAC5CA0B96A1B435D1A518F4E8953A7F2ADDDB66F355A31D83344A83-app.css",
;;                    "admin_new/app.js"
;;                    "admin_new/C8FC4C10850DD12FC7F57C3D5A4579E278077592F8E5A573F9013065A76CC44D-app.js"}},
;;          {}
;;          "target"))
