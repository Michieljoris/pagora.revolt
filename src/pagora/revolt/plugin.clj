(ns pagora.revolt.plugin
  (:require
   [revolt.plugin :refer [create-plugin resolve-from-symbol]]
   ))

(defmethod create-plugin ::nrepl-piggieback [_ config]
  (resolve-from-symbol 'pagora.revolt.plugins.nrepl-piggieback config))
