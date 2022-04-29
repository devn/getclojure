# GetClojure

Searchable Clojure examples programatically compiled from lots of scraping.
GetClojure extracts valid s-expressions from various locations, runs them in a
sandbox, and captures the value and output of every s-expression. The result is
then made searchable.

## Usage

Go to [GetClojure](http://getclojure.org) and start searching.

## Developers

In order to run locally in development mode you'll need to do the
following:

* `docker-compose up -d`
* `pip install pygments`
* Capture the expressions: `lein run -m getclojure.sexp`
* Start the server: `lein ring server`
* Visit [localhost:8080](http://localhost:8080) and search.

## Thanks

* To Jean Niklas L'Orange (hyPiRion) for helping me understand what
  this string does in `cl-format`, and for fixing a related bug in the
  Clojure pretty printer: ~<#(~;~@{~w~^ ~_~}~;)~:>
* To Chris Houser for giving me a treasure trove of logs to harvest.

## YourKit

YourKit is kindly supporting open source projects with its full-featured Java
Profiler.

YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

* <a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
* <a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.

## License

Copyright © 2013 Devin Walters

Distributed under the Eclipse Public License, the same as Clojure.
