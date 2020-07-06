(defproject com.frosku/disultory "0.1.0"
  :author "Frosku <frosku@frosku.com>"
  :signing {:gpg-key "frosku@frosku.com"}
  :description "Library for the procedural generation of data
                from specification."
  :url "https://github.com:Frosku/disultory.git"
  :license {:name "The Unlicense"
            :url "https://unlicense.org"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.fversnel/dnddice "3.0.3"]]
  :repl-options {:init-ns disultory.core}
  :source-paths ["src"]
  :test-paths ["t"]
  :target-path "target/%s/"
  :compile-path "%s/classes"
  :clean-targets ^{:protect false} [:target-path])