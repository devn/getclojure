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

* `pip install pygments`
* `docker-compose up -d`
* Capture the working expressions: `lein run -m getclojure.sexp`. Note: This writes to a file named `output.json` which is intended to be uploaded to Algolia.
* Set the appropriate env vars in your `.envrc`.
* Start the server: `lein ring server-headless`
* Visit [localhost:8080](http://localhost:8080) and search.

## Thanks

* To Chris Houser for giving me a treasure trove of logs to harvest.

## YourKit

YourKit is kindly supporting open source projects with its full-featured Java
Profiler.

YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:

* <a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
* <a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.

## License

Copyright © 2022 Devin Walters

Distributed under the Eclipse Public License, the same as Clojure.
