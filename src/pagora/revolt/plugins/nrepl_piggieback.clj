(ns ^{:clojure.tools.namespace.repl/load false} pagora.revolt.plugins.nrepl-piggieback
  (:require [cider.nrepl]
            [io.aviso.ansi]
            [nrepl.server :as server]
            [clojure.tools.logging :as log]
            [refactor-nrepl.middleware :as refactor.nrepl]
            [cider.piggieback :as pback]
            [revolt.plugin :refer [Plugin create-plugin]]))

(defn init-plugin
  "Initializes nREPL plugin."

  [config]
  (reify Plugin
    (activate [this ctx]
      (println "Starting nREPL server (with piggieback, cider and refactor middleware)..")

      (let [handler (apply server/default-handler
                           (conj (map #'cider.nrepl/resolve-or-fail cider.nrepl/cider-middleware)
                                 #'refactor.nrepl/wrap-refactor
                                 #'pback/wrap-cljs-repl))
            server (server/start-server
                    :port (:port config)
                    :handler handler)]

        (spit ".nrepl-port" (:port server))
        (println (io.aviso.ansi/yellow (str "nREPL client can be connected to port " (:port server))))
        server))

    (deactivate [this ret]
      (when ret
        (log/debug "closing nrepl")
        (nrepl.server/stop-server ret)))))
