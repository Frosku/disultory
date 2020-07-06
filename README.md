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
         
(def spec (-> (blank-specification)
             (with-attribute :name :fixed "Tom")   ; Set name to Tom
             (with-attribute :gender :fixed :male) ; Set gender to male
             (with-attribute 
               :age 
               :random
               {:number 5 :sides 6 :function +})   ; Roll 5d6 for age
             (with-attribute :bald :boolean 0.7))) ; 70% chance of baldness
  => {:attributes [{:id :name, :type :fixed, :value "Tom"} {:id :gender, :type :fixed, :value :male} {:id :age, :type :random, :dice [{:number 5, :sides 6, :function #function[clojure.core/+]}]} {:id :bald, :type :boolean, :prob 0.7}]}
  
(generate spec)
=> {:name "Tom", :gender :male, :age 15, :bald true}
```

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
