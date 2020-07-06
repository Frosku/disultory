(ns disultory.spec)

(defn dice
  "Helper function for specifying dice, takes sides, number, and fn."
  ([fn number] (dice fn number 1))
  ([fn number sides]
   {:number number :sides sides :function fn}))

(defn with
  "Returns the parent specification with a new attribute as specified by
   attr. This will usually be used in conjunction with the *-attribute
   functions below."
  [parent attr]
  (merge-with into parent {:attributes [attr]}))

(defn with-conditional
  "Like (with), but takes a condition and an attribute, which will be
   generated only when the condition resolves true."
  [parent condition attr]
  (merge-with into parent {:conditional [{:condition condition
                                          :attribute attr}]}))

(defn random-attribute
  "Random attributes resolve their values by rolling dice. These can
   have any number of sides, and be additive or subtractive. Each dice
   should be specified in the following format:

   {:sides - The number of sides of each dice (optional, defaults to 1)
    :number - The number of dice to roll
    :function - Either + or -}

  So, 3d6 can be written as [{:sides 6 :number 3 :function +}] whilst
  2d6 + 1d4 - 1 would be [{:sides 6 :number 2 :function +}, {:sides 4
  :number 1, :function +}, {:number 1 :function -}]"
  [id args]
  {:id id :type :random :dice (mapv (fn [a] a) args)})

(defn dice-attribute
  "Dice attributes work much like random attributes, except that they
   are returned in a nested structure i.e. {:stats {:strength ...}}."
  [id args]
  (let [attributes (mapv (fn [a] {:id (first a) :dice (second a)}) args)]
    {:id id :type :dice :options attributes}))

(defn boolean-attribute
  "Boolean attributes are either true or false, and are specified with
   a probability which is <= 1. A prob of 0.4 means the attribute is 40%
   likely to be true, and 60% likely to be false."
  [id prob]
  {:id id :type :boolean :prob prob})

(defn distinct-attribute
  "Distinct attributes are provided as a list of mutually exclusive
   options: (with-distinct-attribute :species [[:human 3] [:cat 5]]).
   Probability is treated as the numerator over a denominator which
   is the sum of all probabilities, so in this example the spec would
   have a 3/8 probability of generating a human, and a 5/8 probability
   of generating a cat."
  [id args]
  (let [attributes (mapv (fn [a] {:id (first a) :prob (second a)}) args)]
    {:id id :type :distinct :options attributes}))

(defn multiple-choice-attribute
  "Multiple choice attributes are like a combination between boolean
   attributes and distinct attributes, and are returned as a vector of
   selected attributes. Probabilities, like with the boolean attributes,
   are specified as a number <= 1."
  [id args]
  (let [attributes (mapv (fn [a] {:id (first a) :prob (second a)}) args)]
    {:id id :type :multiple-choice :options attributes}))

(defn n-choice-attribute
  "n-Choice attributes are like multiple choice attributes, but only a
   maximum of n choices can be selected."
  [id choices args]
  (let [attributes (mapv (fn [a] {:id (first a) :prob (second a)}) args)]
    {:id id :type :n-choice :choices choices :options attributes}))

(defn fn-attribute
  "Function attributes are useful when none of the other attribute types
   is expressive enough to generate the required information. The fn
   takes a single argument, the specification as processed so far."
  [id fun]
  {:id id :type :fn :function fun})

(defn fixed-attribute
  "Fixed attributes are values which are set in stone. This may not seem
   useful when procedurally generating datasets, but it's surprisingly
   helpful, especially with conditional attributes. For example, you
   might want to express a conditional fixed attribute with a number of
   legs derived from species."
  [id value]
  {:id id :type :fixed :value value})
