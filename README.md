# Ants Simulation in Clojure

This project uses [lein](https://leiningen.org/). Run it with:

```bash
$ lein run
```

![demo-sample](https://cloud.githubusercontent.com/assets/46027/22576690/23b64650-e9a4-11e6-9bfd-529a9ff7f848.gif)

## Motivation

After watching Rich's great [talk about concurrency in Clojure](https://www.youtube.com/watch?v=dGVqrGmwOAw),
I wanted to experiment with his original Ants Simulation and learn more about
concurrency, agents, refs, state management, etc.

I ended up refactoring the original solution, because:

- State is used in far too many places. `deref` is the norm. It's hard to spot pure functions.
- Naming is generally bad, with lots of abbreviations and bindings with one char.
- It has one big namespace, which makes things more confusing when you're trying to follow along and make changes.
- It doesn't read well, i.e. you need to read and re-read every function source to grasp what's going on.
- It lacks the functional punch we all love! Example: most functions are using `let`s, sometimes as much as 6 bindings. It feels imperative code in many ways and lacks composability.

Having said all of that, I had tons of fun and learned a lot playing around with this simulation.

## Resources

- [Rich's talk](https://www.youtube.com/watch?v=dGVqrGmwOAw)
- [Talk's slides](https://github.com/dimhold/clojure-concurrency-rich-hickey/blob/master/ClojureConcurrencyTalk.pdf?raw=true)
- [Original source (not really from Rich)](https://github.com/juliangamble/clojure-ants-simulation)
- [Clojure Applied](https://www.amazon.com/Clojure-Applied-Practitioner-Ben-Vandgrift-ebook/dp/B016CJGHFE/ref=mt_kindle?_encoding=UTF8&me=)

## Original Notice

Ants is based on the Clojure Ants Simulation by Rich Hickey.

Copyright (c) Rich Hickey. All rights reserved.
The use and distribution terms for this software are covered by the
Common Public License 1.0 ([http://opensource.org/licenses/cpl1.0.php][cpl])
which can be found in the file cpl.txt at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.
