# GetClojure

Searchable, clojure examples programatically compiled from *lots* and
*lots* of scraping. GetClojure extracts valid s-expressions from
various locations, runs them in a sandbox, and captures the value
and/or output of every s-expression. That information is saved and is
made searchable via ElasticSearch.

## Usage

Go to [GetClojure](http://getclojure.org) and start searching.

## Developers

In order to run locally in development mode you'll need to do the
following:

* Run ElasticSearch locally.
* Run MongoDB locally. Find it via your package manager.
* Run `./script/bootstrap.sh`
* Seed the Database: `lein run -m getclojure.seed`
* Start the server: `lein ring server`
* Compute.

### Note to python 2 users

If after running the commands above you get a list of pages (numbers at
the bottom) but no actual results, the database might not be populating
correctly. This could be because pygments requires python 2. To ensure
you have a symlink to python2 in your path (you should as of python
2.7.3), run the following command:

    command -v python2

If there's no output, you'll need to make a `python2` symlink, to do so,
run the following command:

    ln -s "$(command -v python)" /usr/local/bin/python2

Try running `lein run -m getclojure.seed` again and you should be in
business!

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
