(ns pagora.revolt.tasks.aot
  (:require [clojure.tools.namespace.find :as tnfind]
            [revolt.utils :as utils]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defn compile-namespaces
  [namespaces exclude-namespaces]
  (doseq [namespace namespaces]
    (let [nstr (str namespace)]
      (when-not (some #(.startsWith nstr %) exclude-namespaces)
        (log/info "compiling" nstr)
        (compile namespace)))))

(defn invoke
  [ctx {:keys [extra-namespaces exclude-namespaces]} classpaths target]
  (let [classes (utils/ensure-relative-path target "classes")]
    (.mkdirs (io/file classes))

    (utils/timed
     "AOT"
     (binding [*compile-path* classes]
       (doseq [cp classpaths
               :when (.isDirectory cp)
               :let  [namespaces (tnfind/find-namespaces-in-dir cp)]]
         (compile-namespaces namespaces exclude-namespaces))
       (compile-namespaces extra-namespaces exclude-namespaces)))
(assoc ctx :aot? true)))

