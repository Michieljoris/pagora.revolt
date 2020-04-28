(ns pagora.revolt.task
  (:require
   [revolt.task :refer [Task create-task]]
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

(defn make-description
  "Composes a task information based on title, description and list of parameters."

  [title description & params]
  (let [pstr (for [[k d] (partition 2 params)] (format "  %-20s | %s", k d))]
    (str title "\n\n" description "\n\n" (str/join "\n" pstr) "\n")))


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
      "CSS preprocessor.

Takes Sass/Scss files and turns them into CSS ones.

Options:
--------

  :source-path - relative directory with sass/scss files to transform
  :output-dir - directory where to store generated CSS files
  :sass-options - sass compiler options
")))

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
        "Static assets fingerprinter.

Fingerprints static assets like images, scripts or styles.

Options:
--------

  :assets-paths - collection of paths with assets to fingerprint
  :exclude-paths - collection of paths to exclude from fingerprinting
  :update-with-exts - extensions of files to update with new references to fingerprinted assets

By default all javascripts, stylesheets and HTML files are scanned for references to
fingerprinted assets. Any recognized reference is being replaced with fingerprinted version.
"))))

(defmethod create-task ::write-info [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (write-info/invoke ctx (merge opts input) target))
    (describe [this]
      "Write info to json file"
      ;; (make-description "Project info generator" "Generates map of project-specific information used by other tasks."
      ;;                   :name "project name, eg. \"edge\""
      ;;                   :package "symbol describing project package, eg defunkt.edge"
      ;;                   :version "project version"
      ;;                   :description "project description to be shown")
      )))

(defmethod create-task ::copy-newrelic-jar [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (copy-newrelic-jar/invoke ctx (merge opts input) target))
    (describe [this]
      "Copies new relic jar as pulled in as a dependency from the $HOME/.m2 repository to the root dir of the project")))

(defmethod create-task ::aot [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (aot/invoke ctx (merge opts input) classpaths target))
    (describe [this]
      (make-description "Ahead-Of-Time compilation" "Compiles project namespaces."
                        :exclude-namespaces "vector of strings of (start of) namespaces to exclude"
                        :extra-namespaces "collection of additional namespaces to compile")
      )))

(defmethod create-task ::update-html [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (update-html/invoke ctx (merge opts input) target))
    (describe [this]
      "Edits/sets/adds/removes script links, bugsnag info, build-info etc"
      )))


(defmethod create-task ::capsule [_ opts classpaths target]
  (reify Task
    (invoke [this input ctx]
      (capsule/invoke ctx (merge opts input) target))
    (describe [this]
      "Capsule packager.

Generates an uberjar-like capsule (http://www.capsule.io).

Options:
--------

  :exclude-paths - collection of project paths to exclude from capsule
  :output-jar - project-related path of output jar, eg. dist/foo.jar
  :capsule-type - type of capsule, one of :empty, :thin or :fat (defaults to :fat)
  :main - main class to be run

Capsule options (http://www.capsule.io/reference):

  :min-java-version
  :min-update-version
  :java-version
  :jdk-required?
  :jvm-args
  :environment-variables
  :system-properties
  :security-manager
  :security-policy
  :security-policy-appended
  :java-agents
  :native-agents
  :native-dependencies
  :capsule-log-level
")))
