# GetClojure

Searchable Clojure examples programatically compiled from lots of scraping.
GetClojure extracts valid s-expressions from various locations, runs them in a
sandbox, and captures the value and output of every s-expression. The result is
then made searchable.

## Usage

Go to [GetClojure](http://getclojure.org) and start searching.

## Project Overview

| namespace | purpose |
|--|--|
| getclojure.config | Where any globally-necessary project configuration and functions live |
| getclojure.elastic | Houses configuration, query, and seeding from files for elasticsearch |
| getclojure.extract | Contains logic related to parsing clojure IRC logfiles and spitting out files for later consumption |
| getclojure.format | Logic for formatting s-expressions |
| getclojure.routes | Routes for the application |
| getclojure.server | The entrypoint for the server |
| getclojure.sexp | Logic related to running s-expressions: sandboxing via SCI, formatting and printing, and production of files for later consumption|
| getclojure.util | Everyone has a junk drawer. This is ours. |
| getclojure.views.layout | Static HTML generation via Hiccup |

## Developers

In order to run locally in development mode you'll need to do the following:

* `pip install pygments`
* `docker-compose up -d`
* Download the [logs](https://www.dropbox.com/s/19yy3zn5nh8a1gr/clojure-irc-logs.tar.gz?dl=0) and extract them into the `resources/logs` directory.
* Capture the working expressions: `lein extract-sexp-input-file`. Grab a cup of coffee. This takes about 10min on my machine.
* Set the appropriate env vars in your `.envrc`. If you don't use [direnv](https://direnv.net/), you'll need to export `APP_ENV=development` and `INDEX_NAME=getclojure.`
* Run `lein gen-working-sexps`, `lein gen-formatted-sexps`, and finally `lein seed-elastic`.
* Start the server: `lein ring server-headless`
* Visit [localhost:8080](http://localhost:8080) and search.

## Thanks

* To borkdude for providing so much great open source software to the Clojure community.
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
