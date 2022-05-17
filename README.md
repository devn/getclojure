# GetClojure

Searchable Clojure examples programatically compiled from lots of scraping.
GetClojure extracts valid s-expressions from a set of logfiles, runs them in a
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
| getclojure.format | Logic for formatting and syntax highlighting s-expressions |
| getclojure.layout | Static HTML generation via hiccup |
| getclojure.routes | Routes for the application |
| getclojure.seed | Runs the full ETL pipeline from logs to seeding elasticsearch |
| getclojure.server | The entrypoint for the server |
| getclojure.sexp | Logic related to running s-expressions: sandboxing via SCI |
| getclojure.util | Everyone has a junk drawer. This is ours. |

## Developers

In order to run locally in development mode you'll need to do the following:

* OpenJDK 17. No guarantees it works with older JVMs.
* `pip install pygments`: There are a variety of options for keeping your python envs separate. virtualenv, pyenv, etc. Use whatever works for you. Note that you'll need python 3.X or later.
* `docker-compose up -d`: Runs elasticsearch.
* Download the [logs](https://www.dropbox.com/s/19yy3zn5nh8a1gr/clojure-irc-logs.tar.gz?dl=0) and extract them into the `resources/logs` directory.
* Set the appropriate env vars in your `.envrc`. If you don't use [direnv](https://direnv.net/), you'll need to export `APP_ENV=development` and `INDEX_NAME=getclojure.`
* Run `lein seed 25` to get the s-expressions found in the first 25 logfiles, or `lein seed :all` to run the entire pipeline across all local logfiles.
* Start the server: `lein ring server-headless`
* Visit [localhost:8080](http://localhost:8080) and search.

## Contributions

Contributions welcome. This project uses `lein test-refresh` for rerunning
tests. Run `lein test-refresh :all` to run all tests, including the integration
tests. If you have questions about contributing, please reach out to me on
Clojurians Slack (@devn) or Libera IRC (devn).

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
