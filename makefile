SHELL = /bin/sh
PATH += :./node_modules/.bin

all: node_modules resources/public/css/screen.css

node_modules:
	npm install

resources/public/css/%.css: node_modules src/getclojure/stylus/%.styl
	stylus --compress src/getclojure/stylus/ --out resources/public/css/
	sqwish "$@" # makes 'filename.min.css'
	mv "$(basename $@).min.css" "$@"
