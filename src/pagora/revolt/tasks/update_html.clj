(ns pagora.revolt.tasks.update-html
  (:require
   [revolt.utils :as utils]
   [environ.core :as env]
   [clojure.string :as str]
   [jansi-clj.core :as jansi :refer :all :exclude [reset]]
   [clojure.pprint :refer [pprint]]))

(defn bugsnag-link-tag [api-key environment version]
  (letfn [(wrap-with-quotes [s] (str "\"" s "\""))]
    (str "<script type=text/javascript src=/app/" "bugsnag.min.js data-apikey="
         (wrap-with-quotes api-key)  " data-releasestage="
         (wrap-with-quotes environment) " data-appversion="
         (wrap-with-quotes version) "></script>" )))

(defn update-html-string [html-string {{:keys [sha tag branch timestamp version env]} :ctx
                                       :as options}]
  (let [{:keys [bugsnag-api-key-frontend
                include-build-info-in-html
                app-name path]} (get options (or (keyword env) :dev))
        bugsnag-ph "<!--bugsnag-->"
        app-js-ph "<!--app-js-ph-->"
        build-json-ph "<!--build.json-->"
        app-name-ph "<!--app-name-->"
        html-string
        (cond-> html-string
          app-name (str/replace app-name-ph app-name)
          bugsnag-api-key-frontend (str/replace bugsnag-ph
                                                (bugsnag-link-tag bugsnag-api-key-frontend :prod version))
          include-build-info-in-html (str/replace build-json-ph (str "<script>window.build={version:\"" version
                                                                     "\",branch:\"" branch
                                                                     "\",tag:\"" tag
                                                                     "\",timestamp:\"" timestamp
                                                                     "\",sha:\"" sha
                                                                     "\",env:\"" env
                                                                     "\"}</script>"))
          true (str/replace app-js-ph
                            (str "<script type=\"text/javascript\" src=\"/" path "/app.js\"></script>")))]
    (str/replace html-string (re-pattern (str/join "|" [bugsnag-ph build-json-ph])) "")))



(defn invoke [ctx {:keys [src-path dest-path] :as options} target]
  (utils/timed
   "UPDATE-HTML"
   (let [html-string (slurp src-path)
         updated-html-string (update-html-string html-string (assoc options
                                                                    :ctx ctx))]
     (println (str "Writing " dest-path " from " src-path " for " (:env ctx) " environment"))
     (spit dest-path updated-html-string)
     ctx)))

;; (invoke {:env "prod"} {:src-path "resources/admin-template.html"
;;                        :dest-path "resources/admin.html"
;;                        :dev {:include-build-info-in-html false}
;;                        :prod {:include-build-info-in-html true
;;                               :bugsnag-api-key-frontend "123"}
;;                        }
;;         nil)
