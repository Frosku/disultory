(ns disultory.decision)

(defn roll-dice
  "Simple dice roller function, takes a single input in the form
   {:number n :sides z} and rolls ndz (dice notation) dice, returning
   only the cumulative total."
  [dice]
  (apply + (repeatedly (:number dice)
                       #(+ 1 (rand-int (if (nil? (:sides dice))
                                         1
                                         (:sides dice)))))))

(defn decide-dice-option
  "This function totals up the additive dice rolls and the subtractive
   dice rolls, then subtracts the latter from the former."
  [option]
  (let [additive-dice (filterv #(= + (:function %)) (:dice option))
        subtractive-dice (filterv #(= - (:function %)) (:dice option))
        additive-rolls (mapv #(roll-dice %) additive-dice)
        subtractive-rolls (mapv #(roll-dice %) subtractive-dice)]
    {(:id option)
     (- (apply + additive-rolls) (apply + subtractive-rolls))}))

(defn decide-random-attribute
  "This function dispatches random attributes to be calculated."
  [attr]
  (decide-dice-option attr))

(defn decide-dice-attribute
  "This function dispatches dice attributes to be calculated."
  [attr]
  {(:id attr) (apply merge (mapv #(decide-dice-option %)
                                 (:options attr)))})

(defn roll-boolean
  "This function takes a probability, specified as a number between 0.0
   and 1.0, and returns a decision for that probability."
  [prob]
  (< (rand) prob))

(defn decide-boolean-attribute
  "This function structures the data for boolean attributes."
  [attr]
  {(:id attr) (roll-boolean (:prob attr))})

(defn get-weighted-distribution
  "Takes any number of options with probabilities expressed as numerators
   and returns a weighted distribution suitable for use with rand-nth.

   (get-weighted-distribution [[:male 1] [:female 4]])
     => [:male :female :female :female :female]"
  [opts]
  (->> opts
       (mapv (fn [[n p]] (repeat p n)))
       flatten))

(defn roll-distinct
  "Compositional function which takes options for a distinct attribute
   and randomly chooses one from a weighted distribution."
  [& opts]
  (-> opts
     get-weighted-distribution
     seq
     rand-nth))

(defn decide-distinct-attribute
  "This function structures the data for distinct attributes."
  [attr]
  {(:id attr)
   (->> attr
        (:options)
        (map (fn [o] [(:id o) (:prob o)]))
        (apply roll-distinct))})

(defn decide-multiple-choice-attribute
  "This function structures the data for multiple choice attributes."
  [attr]
  {(:id attr)
   (->> attr
        (:options)
        (mapv #(decide-boolean-attribute %))
        (filterv #(= true (val (first %))))
        (mapv #(key (first %))))})

(defn decide-n-choice-attribute
  "This function structures the data for n-choice attributes."
  [attr]
  (let [try (-> attr
               (decide-multiple-choice-attribute))]
        (if (<= (count (get try (:id attr))) (:choices attr))
          try
          {(:id attr)
           (->> (get try (:id attr))
                shuffle
                (take (:choices attr)))})))

(defn decide-fixed-attribute
  "Structures the data for fixed attributes.

   (decide-fixed-attribute {:id :name :type :fixed :value \"Tom\"})
     => {:name \"Tom\"}"
  [attr]
  {(:id attr) (:value attr)})

(defn decide-fn-attribute
  "Structures the data for function attributes."
  [attr spec]
  {(:id attr) ((:function attr) spec)})
