test: node-test browser-test

node-test:
	@./node_modules/.bin/mocha -R spec

browser-test:
	@echo "starting local dev web server..."
	@node test/dev-server/server.js & echo "$$!" > /tmp/browser-storage-test.pid
	@./node_modules/.bin/mochify --wd -R spec
	@cat /tmp/browser-storage-test.pid | xargs kill
	@rm -f /tmp/browser-storage-test.pid

kill-browser-test:
	@cat /tmp/browser-storage-test.pid | xargs kill
	@rm -f /tmp/browser-storage-test.pid

.PHONY: node-test browser-test