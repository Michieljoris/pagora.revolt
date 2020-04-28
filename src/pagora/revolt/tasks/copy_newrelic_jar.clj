(ns pagora.revolt.tasks.copy-newrelic-jar
  (:require
   [clojure.java.shell :as shell]
   [revolt.utils :as utils]
   [jansi-clj.core :as jansi :refer :all :exclude [reset]]))

(defn invoke [ctx {:keys [version]} target]
  (utils/timed
   "COPY-NEWRELIC-JAR"
   (let [home (System/getenv "HOME")
         source (str home "/.m2/repository/com/newrelic/agent/java/newrelic-agent/" version "/newrelic-agent-" version ".jar")]
    (println "Copying new relic jar from " source" to cwd")
    (println (shell/sh
              "cp" source "./newrelic-agent.jar"))
     ctx)))
