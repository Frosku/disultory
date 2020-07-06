(ns disultory.core-test
  (:require [disultory.core :refer :all]
            [disultory.spec :refer :all]
            [disultory.decision :refer :all]
            [clojure.test :refer :all]))

(deftest test-expansion-of-attributes
  (testing "We can correctly expand :dice attributes"
    (is (= {:attributes [{:id :stats
                          :type :dice
                          :options [{:id :strength :dice [{:number 2
                                                           :sides 6
                                                           :function +}
                                                          {:number 1
                                                           :function -}]}
                                    {:id :intelligence :dice [{:number 2
                                                               :sides 4
                                                               :function +}]}]}]}
           (-> (blank-specification)
              (with-attribute :stats :dice
                [:strength [{:number 2 :sides 6 :function +}
                            {:number 1 :function -}]]
                [:intelligence [{:number 2
                                 :sides 4
                                 :function +}]]))))
    (is (= {:attributes [{:id :age :type :random :dice [{:number 5
                                                        :sides 6
                                                        :function +}]}]}
           (-> (blank-specification)
              (with-attribute :age :random {:number 5
                                            :sides 6
                                            :function +})))))
  (testing "We can correctly expand :boolean attributes"
    (is (= {:attributes [{:id :shiny
                          :type :boolean
                          :prob 0.6}]}
           (-> (blank-specification)
              (with-attribute :shiny :boolean 0.6)))))
  (testing "We can correctly expand :distinct attributes"
    (is (= {:attributes [{:id :species
                          :type :distinct
                          :options [{:id :unicorn :prob 5}
                                    {:id :pegasus :prob 5}
                                    {:id :pony :prob 8}
                                    {:id :pegacorn :prob 1}]}]}
           (-> (blank-specification)
              (with-attribute :species :distinct
                [:unicorn 5]
                [:pegasus 5]
                [:pony 8]
                [:pegacorn 1])))))
  (testing "We can correctly expand :multiple-choice attributes"
    (is (= {:attributes [{:id :likes
                          :type :multiple-choice
                          :options [{:id :candy :prob 0.5}
                                    {:id :burgers :prob 0.2}]}]}
           (-> (blank-specification)
              (with-attribute :likes :multiple-choice
                [:candy 0.5] [:burgers 0.2])))))
  (testing "We can correctly expand :n-choice attributes"
    (is (= {:attributes [{:id :favorite-colors
                          :type :n-choice
                          :choices 3
                          :options [{:id :blue :prob 0.1}
                                    {:id :red :prob 0.6}
                                    {:id :green :prob 0.3}
                                    {:id :brown :prob 0.7}]}]}
           (-> (blank-specification)
              (with-attribute :favorite-colors :n-choice 3
                [:blue 0.1] [:red 0.6] [:green 0.3] [:brown 0.7])))))
  (testing "We can correctly expand :fn attributes"
    (let [fun (fn [] "Foo")]
      (is (= {:attributes [{:id :name
                            :type :fn
                            :function fun}]}
             (-> (blank-specification)
                (with-attribute :name :fn fun))))))
  (testing "We can correctly expand :fixed attributes"
    (is (= (-> (blank-specification)
              (with-attribute :legs :fixed 4))
           {:attributes [{:id :legs
                          :type :fixed
                          :value 4}]})))
  (testing "We can combine attributes into a spec"
    (is (= {:attributes [{:id :name :type :fixed :value "Tom"}
                         {:id :species
                          :type :distinct
                          :options [{:id :cat :prob 3}
                                    {:id :dog :prob 2}]}
                         {:id :stats
                          :type :dice
                          :options [{:id :strength :dice "3d6"}
                                    {:id :speed :dice "2d6"}]}
                         {:id :gender
                          :type :distinct
                          :options [{:id :male :prob 1}
                                    {:id :female :prob 1}]}]}
           (-> (blank-specification)
              (with-attribute :name :fixed "Tom")
              (with-attribute :species :distinct [:cat 3] [:dog 2])
              (with-attribute :stats :dice [:strength "3d6"] [:speed "2d6"])
              (with-attribute :gender :distinct [:male 1] [:female 1]))))))

(deftest dice-decision-test
  (testing "We can roll dice"
    (with-redefs [clojure.core/rand-int (fn [n] (- n 1))]
      (is (= 12 (roll-dice {:number 2 :sides 6})))
      (is (= 1 (roll-dice {:number 1})))
      (is (= 256 (roll-dice {:number 16 :sides 16})))))
  (testing "We can define attributes from dice"
    (with-redefs [clojure.core/rand-int (fn [n] (- n 1))]
      (is (=
           {:stats {:strength 12 :intelligence 6 :speed 8}}
           (decide-attribute
            {:id :stats
             :type :dice
             :options [{:id :strength :dice [{:number 2 :sides 6 :function +}]}
                       {:id :intelligence :dice [{:number 1 :sides 8 :function +}
                                                 {:number 2 :function -}]}
                       {:id :speed :dice [{:number 2 :sides 4 :function +}]}]}))))))

(deftest boolean-decision-test
  (testing "We can get a boolean value"
    (is (= true (roll-boolean 1.0)))
    (is (= false (roll-boolean 0.0)))
    (with-redefs [clojure.core/rand (fn [] 0.5)]
      (is (= true (roll-boolean 0.6)))
      (is (= false (roll-boolean 0.5)))
      (is (= false (roll-boolean 0.4)))))
  (testing "We can determine boolean attributes"
    (is (= {:shiny true}
           (decide-attribute {:id :shiny :type :boolean :prob 1.0})))
    (is (= {:shiny false}
           (decide-attribute {:id :shiny :type :boolean :prob 0.0})))))

(deftest distinct-decision-test
  (testing "We can get a weighted distribution"
    (is (= [:male :male :male :female]
           (get-weighted-distribution [[:male 3] [:female 1]]))))
  (testing "We can get a distinct value"
    (is (= :male
           (roll-distinct [:male 10])))
    (is (= :female
           (roll-distinct [:female 10]))))
  (testing "We can determine distinct attributes"
      (is (= {:gender :male}
             (decide-attribute {:id :gender
                                :type :distinct
                                :options [{:id :male :prob 6}]})))))

(deftest multiple-and-n-choice-decision-test
  (testing "We can determine multiple choice attributes"
    (with-redefs [clojure.core/rand (fn [] 0.5)
                  clojure.core/shuffle (fn [coll] coll)]
      (is (= {:skills [:cooking :magic]}
             (decide-attribute {:id :skills
                                :type :multiple-choice
                                :options [{:id :cooking :prob 0.6}
                                          {:id :cleaning :prob 0.3}
                                          {:id :magic :prob 0.9}
                                          {:id :combat :prob 0.1}]})))
      (is (= {:skills [:cooking]}
             (decide-attribute {:id :skills
                                :type :n-choice
                                :choices 1
                                :options [{:id :cooking :prob 0.6}
                                          {:id :cleaning :prob 0.3}
                                          {:id :magic :prob 0.9}
                                          {:id :combat :prob 0.1}]}))))))

(deftest fixed-decision-test
  (testing "We can determine fixed attributes"
    (is (= {:has-horn true}
           (decide-attribute {:id :has-horn :type :fixed :value true})))))

(deftest function-test
  (testing "We can determine function attributes"
    (is (= {:name "Tom"}
           (decide-attribute {:id :name
                              :type :fn
                              :function (fn [] "Tom")})))))

(deftest generation-test
  (testing "We can generate a character with no conditional attributes"
    (def character-spec (-> (blank-specification)
                           (with-attribute :name :fixed "Lindsey")
                           (with-attribute :gender :distinct
                             [:male 1] [:female 1])
                           (with-attribute :age :random
                             {:sides 6 :number 2 :function +}
                             {:number 13 :function +})))
    (def character (-> character-spec generate))
    (is (= true (contains? character :name)))
    (is (= true (contains? character :age)))
    (is (= true (contains? character :gender)))
    (is (= true (= (:name character) "Lindsey")))
    (is (= true (>= (:age character) 15)))
    (is (= true (<= (:age character) 25)))
    (is (= true (or (= (:gender character) :male)
                    (= (:gender character) :female))))))

(deftest conditional-attributes-test
  (testing "We can add conditional attributes to a spec"
    (def is-tom? (fn [spec] (= "Tom" (:name spec))))
    (is (= {:attributes [{:id :name :type :fixed :value "Tom"}]
            :conditional [{:condition is-tom?
                           :attribute {:id :gender :type :fixed :value :male}}]}
           (-> (blank-specification)
              (with-attribute :name :fixed "Tom")
              (with-conditional is-tom? (fixed-attribute :gender :male))))))
  (testing "We can check if a condition is met"
    (is (= [{:id :gender :type :fixed :value :male}]
           (-> {:name "Tom"}
              (with-conditional is-tom? (fixed-attribute :gender :male))
              (conditions-met)))))
  (testing "We can generate with conditional attributes"
    (is (= {:name "Tom" :gender :male}
           (-> (blank-specification)
              (with-attribute :name :fixed "Tom")
              (with-conditional is-tom? (fixed-attribute :gender :male))
              generate)))))
