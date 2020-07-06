(ns disultory.core
  (:require [disultory.spec :as s]
            [disultory.decision :as d]))

(defn blank-specification
  "Returns an empty map, this is simply an (in?)convenience function which
   allows for more expressive chaining."
  []
  {})

(defn with-attribute
  "Helper function for adding attributes to the parent specification. See
   documentation in disultory.spec for more information."
  [parent id type & args]
  (case type
    :random (s/with parent (s/random-attribute id args))
    :dice (s/with parent (s/dice-attribute id args))
    :boolean (s/with parent (s/boolean-attribute id (first args)))
    :distinct (s/with parent (s/distinct-attribute id args))
    :multiple-choice (s/with parent (s/multiple-choice-attribute id args))
    :n-choice (s/with parent
                      (s/n-choice-attribute id (first args) (rest args)))
    :fn (s/with parent (s/fn-attribute id (first args)))
    :fixed (s/with parent (s/fixed-attribute id (first args)))))

(defn decide-attribute
  "Dispatch function for making decisions from attributes on a spec. See
   documentation in disultory.decision for more information."
  ([attr] (decide-attribute attr {}))
  ([attr specification]
   (case (:type attr)
     :random (d/decide-random-attribute attr)
     :dice (d/decide-dice-attribute attr)
     :boolean (d/decide-boolean-attribute attr)
     :distinct (d/decide-distinct-attribute attr)
     :multiple-choice (d/decide-multiple-choice-attribute attr)
     :n-choice (d/decide-n-choice-attribute attr)
     :fixed (d/decide-fixed-attribute attr)
     :fn (d/decide-fn-attribute attr specification))))

(defn conditions-met
  "Returns a vector of conditional attributes whose conditions have been
   met. These conditions are expressed as Clojure functions, so they
   allow for limitless complexity."
  [specification]
  (if (empty? (:conditional specification))
    []
    (->> (:conditional specification)
         (filterv #(= true ((:condition %) specification)))
         (mapv #(:attribute %)))))

(defn generate
  "Recursively generate data from a specification. First, the function
   decides a value for each attribute, then it calls itself with a list
   of conditional attributes whose conditions have been met until either
   all conditional attributes have been resolved or all conditions are
   false."
  [specification]
  (if (empty? (:attributes specification))
    (dissoc specification :attributes :conditional)
    (let [next-specification (apply merge-with
                                    into
                                    specification
                                    (map #(decide-attribute % specification)
                                         (:attributes specification)))
          conditions-met (conditions-met next-specification)
          new-conditions (remove (fn [c] (some
                                         #(= (:attribute c) %)
                                         conditions-met))
                                 (:conditional specification))]
      (recur (assoc next-specification
                    :attributes conditions-met
                    :conditional new-conditions)))))
