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

### Modifying the styles

Note: you'll need node.js to install the tools needed to compile stylus
files to css.  Instructions for installing node can be found on [its
website](http://nodejs.org/).

To change the style of the site, edit the files in
`src/getclojure/stylus/*.styl`.  Then run `watch make` in your terminal
(note: this may take a moment and will require an internet connection
the first time it's run).  After that, refresh your browser to see your
changes!

For more information on stylus, see http://learnboost.github.io/stylus/.

### Note to python 3 users

If the default python on your system is python 3, you'll need to edit
pygments so that it uses python 2. To do so, run the following command
after running `./script/bootstrap.sh`:

    sed --in-place 's$#!/usr/bin/env python$#!/usr/bin/env python2$' resources/pygments/pygmentize

You'll also need a `python2` symlink, which should exist as of python
2.7.3. If `command -v python2` returns nothing, run the following
command to create the symlink:

    ln -s "$(command -v python)" /usr/local/bin/python2

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
