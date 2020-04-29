(ns pagora.revolt.task
  (:require
   [revolt.task :refer [Task create-task make-description]]
   [pagora.revolt.tasks.sass :as sass]
   [pagora.revolt.tasks.assets :as assets]
   [pagora.revolt.tasks.aot :as aot]
   [pagora.revolt.tasks.write-info :as write-info]
   [pagora.revolt.tasks.copy-newrelic-jar :as copy-newrelic-jar]
   [pagora.revolt.tasks.update-html :as update-html]
   [pagora.revolt.tasks.capsule :as capsule]
   [clojure.tools.logging :as log]
   [clojure.string :as str]
   ))

(defmethod create-task ::sass [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (let [in (if (map? input)
                 (merge opts input)
                 (assoc opts :file input))]
        (sass/invoke ctx in classpaths target)))
    (notify [this path ctx]
      (.invoke this path ctx))
    (describe [this]
      (make-description "CSS preprocessor" "Takes Sass/Scss files and turns them into CSS ones."
                        :source-path "relative directory with sass/scss resources to transform"
                        :output-path "relative directory where to store generated CSSes"
                        :sass-options "sass compiler options"))))

(defmethod create-task ::assets [_ opts classpaths target]
  (let [default-opts {:update-with-exts ["js" "css" "html"]}
        options (merge default-opts opts)]
    (reify Task
      (invoke [this input ctx]
        (assets/invoke ctx (merge options input) classpaths target))
      (notify [this path ctx]
        (log/warn "Notification is not handled by \"assets\" task.")
        ctx)
      (describe [this]
        (make-description "Static assets fingerprinter" "Fingerprints static assets like images, scripts or styles"
                          :assets-paths "collection of paths with assets to fingerprint"
                          :exclude-paths "collection of paths to exclude from fingerprinting"
                          :update-with-exts "extensions of files to update with new references to fingerprinted assets")))))


(defmethod create-task ::write-info [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (write-info/invoke ctx (merge opts input) target))
    (describe [this]
      (make-description "Write project info to json" ""
                        :path "Something like resources/build-info.json"))))

(defmethod create-task ::copy-newrelic-jar [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (copy-newrelic-jar/invoke ctx (merge opts input) target))
    (describe [this]
      (make-description "Copies new relic jar as pulled in as a dependency from the $HOME/.m2 repository to the root dir of the project"
                        :version "Something like \"4.2.0\",  this should match the version in deps.edn!!!!! "))))


(defmethod create-task ::aot [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (aot/invoke ctx (merge opts input) classpaths target))
    (describe [this]
      (make-description "Ahead-Of-Time compilation" "Compiles project namespaces."
                        :extra-namespaces "collection of additional namespaces to compile"))))



(defmethod create-task ::update-html [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (update-html/invoke ctx (merge opts input) target))
    (describe [this]
      (make-description "Edits/sets/adds/removes script links, bugsnag info, build-info etc" ""
                        :src-path "resources/app-template.html"
                        :dest-path "resources/app.html"
                        :dev {:include-build-info-in-html false
                              :path "app"
                              :app-name "Pagora"}
                        :prod {:include-build-info-in-html true
                               :path "app"
                               :app-name "pagora"
                               ;;TODO load from env!!
                               :bugsnag-api-key-frontend "3464720f5367914849487b96cb428fc8" ;anything but nil adds the bugsnag script tag in app.html
                               :bugsnag-api-key-server "d87f64fd8593faed9e04f531efc6a082"}

                        )

      )))


(defmethod create-task ::capsule [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (capsule/invoke ctx (merge opts input) target))
    (describe [this]
      (make-description "Capsule packager" "Generates an uberjar-like capsule (http://www.capsule.io)."
                        :capsule-type "type of capsule, one of :empty, :thin or :fat (defaults to :fat)"
                        :exclude-paths "collection of project paths to exclude from capsule"
                        :output-jar "project related path of output jar, eg. dist/foo.jar"
                        :main "main class to be run"
                        :min-java-version "http://www.capsule.io/reference"
                        :min-update-version "http://www.capsule.io/reference"
                        :java-version "http://www.capsule.io/reference"
                        :jdk-required? "http://www.capsule.io/reference"
                        :jvm-args "http://www.capsule.io/reference"
                        :environment-variables "http://www.capsule.io/reference"
                        :system-properties "http://www.capsule.io/reference"
                        :security-manager "http://www.capsule.io/reference"
                        :security-policy "http://www.capsule.io/reference"
                        :security-policy-appended "http://www.capsule.io/reference"
                        :java-agents "http://www.capsule.io/reference"
                        :native-agents "http://www.capsule.io/reference"
                        :native-dependencies "http://www.capsule.io/reference"
                        :capsule-log-level "http://www.capsule.io/reference"))))
