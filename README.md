# Disultory

[![Clojars Project](https://img.shields.io/clojars/v/discord.clj.svg)](https://clojars.org/discord.clj)

Disultory was created as a component of one of my other projects, but I
thought it might be useful to someone, so I've decided to release it
standalone.

Disultory features a composable specification format and decision
engine which allows for the procedural generation of structured data
according to specification. I use it for generating random characters
but there are limitless ways in which it could be applied.

## Usage

### Example

```clojure
(require [disultory.core :as d]
         [disultory.spec :as ds]
         [disultory.decision :as dd])
         
(def spec (-> (d/blank-specification)
             (d/with-attribute :name :fixed "Tom")    ; Set name to Tom
             (d/with-attribute :gender :fixed :male)  ; Set gender to male
             (d/with-attribute :age :random 
                               (ds/dice + 5 6))         ; Roll 5d6 for age
             (d/with-attribute :bald :boolean 0.7)))  ; 70% chance of baldness
  => {:attributes 
       [{:id :name, :type :fixed, :value "Tom"}
        {:id :gender, :type :fixed, :value :male}
        {:id :age, :type :random, :dice 
          [{:number 5, :sides 6, :function #function[clojure.core/+]}]} 
        {:id :bald, :type :boolean, :prob 0.7}]}
  
(d/generate spec)
  => {:name "Tom", :gender :male, :age 15, :bald true}
```

If you think that it might not make sense for Tom to be bald at 15,
or, at the very least you think it might be unfortunate for Tom, then
you'll need a conditional attribute:

```clojure
(-> (d/blank-specification)
   ...
   (ds/with-conditional (fn [tom] (> (:age tom) 20)) 
                        {:bald :boolean 0.2}))
```

Conditional attributes run after standard attributes, and therefore
we can check the outcome of our age attribute, and only give Tom a
chance of going bald if he's over 20 -- aren't we generous?

### Explanation

First, you will need to generate a specification. Specifications are
composed of attributes -- those fields which should always appear in
the output data -- and conditional attributes -- those fields which
should only appear based on existing attributes.

For more information, I'd suggest looking at the tests & docstrings,
which contain more information on all the available features.

## Unlicense

This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to [https://unlicense.org](https://unlicense.org/).
