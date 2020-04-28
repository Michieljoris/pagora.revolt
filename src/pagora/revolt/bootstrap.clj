(ns pagora.revolt.bootstrap
  (:require
   [clojure.tools.cli :as cli]
   [revolt.bootstrap :as revolt]
   [pagora.revolt.plugin]
   [pagora.revolt.task]
   [taoensso.timbre :as timbre]
   [jansi-clj.core :as jansi :refer :all :exclude [reset]]
   [clojure.pprint :refer [pprint]]))

(comment
  (def path "admin_new/")

  (gzip  :files {(str path "prod.js") (str path "prod.js.gz")
                 (str path "app.css")  (str path "app.css.gz")
                 (str path "garden.css")  (str path  "garden.css.gz")
                 ;; "bla.txt" "bla2.gz.txt"
                 }
         ;; Also you can compress files matching regular expressions:
         ;; :regex [#"main.js"]
         )

  (asset-fingerprint)                   ;


  ;; :dependencies (fn [_] (apply conj mvn-deps

  ;;                              ;;For copying to root dir.
  ;;                              ['com.newrelic.agent.java/newrelic-agent new-relic-version]

  ;;                              ;;We can just conj this
  ;;                              (seq git-deps)))


  ;; (def nrepl-port (if-let [s (env :nrepl-port)]
  ;;                   (read-string s)
  ;;                   38400))
  ;; (def reload-port (if-let [s (env :reload-port)]
  ;;                   (read-string s)
  ;;                   46500))

  ;; (println "NREPL_PORT: " nrepl-port)
  ;; (println "RELOAD_PORT: " reload-port)

)


(defn -main
  [& args]
  (timbre/info (jansi/white  "---------revolt-bootstrap------------"))
  ;; (println "Path:" path)
  ;; (pprint args)

  (let [params (:options (cli/parse-opts args revolt/cli-options))]
    (timbre/info "REVOLT params")
    (timbre/info :#pp params)
    ;; (timbre/info (jansi/white (str "ENVIRONMENT: " environment)))

    (apply revolt/-main args))

  )
