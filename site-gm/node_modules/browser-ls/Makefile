karma 		= ./node_modules/karma/bin/karma
jshint		= ./node_modules/.bin/jshint
uglifyjs	= ./node_modules/.bin/uglifyjs
linelint 	= ./node_modules/.bin/linelint
browserify 	= ./node_modules/.bin/browserify
lintspaces 	= ./node_modules/.bin/lintspaces

.PHONY : test

default: format

build:format
	$(browserify) -s ls -e ./index.js -o ./dist/ls.js
	$(uglifyjs) -m -c -o ./dist/ls.min.js ./dist/ls.js
	@echo "Build succeeded!\n"

test:build
	$(karma) start

format:
	$(linelint) ./index.js
	@echo "linelint pass!\n"
	$(lintspaces) -nt -i js-comments -d spaces -s 2 ./index.js
	@echo "lintspaces pass!\n"
	$(jshint) ./index.js
	@echo "JSHint pass!\n"