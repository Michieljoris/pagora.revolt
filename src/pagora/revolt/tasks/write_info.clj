(ns pagora.revolt.tasks.write-info
  (:require
   [clojure.java.io :as io]
   [environ.core :as env]
   [cheshire.core :as json]
   [revolt.utils :as utils]
   [jansi-clj.core :as jansi :refer :all :exclude [reset]]
   [clojure.pprint :refer [pprint]]))

(defn write-build-info
  [path kv]
  (io/make-parents path)
  (with-open [out (io/writer path)]
    (json/generate-stream kv out)))

(defn invoke [ctx {:keys [path]} target]
  (utils/timed
   "WRITE-INFO"
   (let [version (clojure.string/trim (slurp "version"))
         ctx (assoc ctx
                    :version version
                    :env (get env/env :clj-env))]
     (println (jansi/white (with-out-str (pprint ctx))))

     (write-build-info path (dissoc ctx :package :description))
     ctx)
   ))

