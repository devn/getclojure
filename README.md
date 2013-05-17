# GetClojure

Searchable, clojure examples programatically compiled from *lots* and
*lots* of scraping. GetClojure extracts valid s-expressions from
various locations, runs them in a sandbox, and captures the value
and/or output of every s-expression. That information is saved and is
made searchable via ElasticSearch.

## Usage

Go to [GetClojure](http://getclojure.com) and start searching.

## Developers

In order to run locally in development mode you'll need to do the
following:

* Run ElasticSearch locally. Find it via your package manager.
* Run MongoDB locally. Find it via your package manager.
* Run `./script/bootstrap.sh`
* Seed the Database: `lein run -m getclojure.seed`
* Start the server: `lein ring server`
* Compute.

## Contributors

* Anthony Grimes
* Joe Nelson
* Joshua Hoff

## Thanks

* To Bendyworks for 20% time.
* To ClojureWerkz for providing great libraries.
* To Anthony Grimes (Raynes) for letting me steal some of his code
  from RefHeap to speed things along.
* To Phil Hagelberg (technomancy) for his help with Heroku-related
  issues.
* To Jean Niklas L'Orange (hyPiRion) for helping me understand what
  this string does in cl-format, and for fixing a related bug in the
  Clojure pretty printer: ~<#(~;~@{~w~^ ~_~}~;)~:>
* To Chris Houser for giving me a treasure trove of logs to harvest.

## License

Copyright © 2013 Devin Walters

Distributed under the Eclipse Public License, the same as Clojure.
