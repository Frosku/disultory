(defproject com.frosku/disultory "0.1.4"
  :author "Frosku <frosku@frosku.com>"
  :signing {:gpg-key "frosku@frosku.com"}
  :description "Library for the procedural generation of data
                from specification."
  :url "https://github.com:Frosku/disultory.git"
  :license {:name "The Unlicense"
            :url "https://unlicense.org"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :repl-options {:init-ns disultory.core}
  :source-paths ["src"]
  :test-paths ["t"]
  :target-path "target/%s/"
  :compile-path "%s/classes"
  :plugins [[lein-bump-version "0.1.6"]]
  :clean-targets ^{:protect false} [:target-path])
