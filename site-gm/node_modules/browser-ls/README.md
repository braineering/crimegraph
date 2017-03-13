browser-ls
=======

[![build-status](https://travis-ci.org/evanshortiss/browser-local-storage.svg?branch=master)
](https://travis-ci.org/evanshortiss/browser-local-storage.svg?branch=master)[![version](https://badge.fury.io/js/browser-ls.svg)
](https://badge.fury.io/js/browser-ls.svg)

browser-ls provides a nice node-like wrapper on the localStorage API in 
browsers. It handles potential localStorage exceptions for you, plus 
it's only 3KB minified!

It handles exceptions internally via try catch blocks and will return errors in 
the Node.js fashion of using the first callback parameter as an error argument.
This means it'll play nicely with other Node.js modules that are supported in 
the browser like Async.js.

## Install 

Via NPM: 

```
npm i browser-ls
```

Via Bower

```
bower i browser-ls
```

You can also just drop a file from the _dist/_ folder here into your project.


## Example


```javascript
var ls = require('browser-ls');

// If you're not Browserify-ing you can use:
// var ls = window.ls ;
// instead of "require"

ls.setJson('somejson', {
	name: 'Bruce Wayne',
	aka: 'Batman'
}, function (err) {
	if (err) {
		alert('Failed to write to localStorage');
	} else {
		ls.getJson('somejson', function(err, json) {
			if (err) {
				alert('Failed to get item from localStorage')
			} else {
				alert('We stored and retrieved JSON from localStorage!');
				alert('Result: ' + JSON.stringify(json));
			}
		});
	}
});

```

Because this module uses a standard Node callback pattern if you like neater 
code the above could be written like so:

```javascript
var async = require('async'),
	ls = require('browser-ls');

var STORAGE_KEY = 'somejson'

async.series({
	writeToLs: function (cb) {
		ls.setJson(STORAGE_KEY, {
			name: 'Bruce Wayne',
			aka: 'Batman'
		}, cb);
	}, 
	readFromLs: function (cb) {
		ls.getJson(STORAGE_KEY, cb);
	}
}, function (err, res) {
	if (err) {
		alert('Something went wrong...');
	} else {
		alert('We stored and retrieved JSON from localStorage!');
		alert('Result: ' + JSON.stringify(res.readFromLs));
	}
})
```

## Browser Support
I've tested this on the following browsers but it should work on pretty much 
any browser with _JSON_ and _localStorage_ support. 

* Safari 7.0.5
* Chrome 37.0.2062
* Firefox 29.0.0

If the awesome
[ci.testling](https://ci.testling.com/) service has the timeout issues it's 
recently experiencing resolved a more complete browser support matrix can be 
constructed then.


## API
All callbacks receive an error as the first parameter which will be null if no 
error occured. All methods that retreive an item take a second parameter that 
is the result.

#### get(key, callback)
Get a string value from localStorage.

```javascript

var ls = require('browser-ls');

ls.get('SOME_KEY', function (err, result) {
	if (err) {
		// DARN!
	} else {
		// WOO-HOO!
	}
});

```

#### getJson(key, callback)
Get an Object from localStorage.

#### set(key, string[, callback])
Set a string value in localStorage.

#### setJson(key, object[, callback])
Write a JSON object to localStorage.

#### remove(key[, callback])
Remove an object from localStorage.

#### getAdapter (name)
This will get a localStorage interface that stores data under the given key 
name to avoid clashes. It has all the standard API methods. 

For example:

```javascript

var ls = require('browser-ls');

var Users = ls.getAdapter('Users');

Users.set('KEY', 'some value');
ls.set('KEY', 'another value');

Users.get('KEY', function (err, res) {
	// res will equal 'some value'
});
ls.get('KEY' function (err, res) {
	// res will equal 'another value'
});

```

Using a single instance to do this would have overwritten the Users value with 
that of the plain ls interface.

_getAdapter_ also has more advanced usage and supports pre-save and post-load 
transforms. These are useful if you need to store encrypted data or performs 
modification of data prior to saving. Here's an example"

```javascript
var ls = require('browser-ls');
var encryption = require('./encryption');
var userId = 'user-1234';

var Users = ls.getAdapter({
	ns: 'Users', // Store data under this namespace
	preSave: doEncrypt,
	postLoad: doDecrypt
});

function doEncrypt (val, callback) {
	// val is the value passed to the .set method below
	encryption.encrypt(val, function (err, encryptedVal) {
		if (err) {
			callback(err, null);
		} else {
			// Send the updated value to the callback to be saved
			callback(null, encryptedVal)
		}
	});
}

function doEncrypt (val, callback) {
	// val is the value that the module got from localStorage 
	// in this case it is the encrypted value of:
	// '{"name":"evan","location":"USA"}''
	encryption.decrypt(val, function (err, decryptedVal) {
		if (err) {
			callback(err, null);
		} else {
			// Send the updated value to the callback to be loaded
			callback(null, decryptedVal)
		}
	});
}

// This JSON string being saved will be encrypted by preSave
Users.set(userId, JSON.stringify({
	name: 'evan',
	location: 'USA'
}));


// The value we get will automatically be decrpyted
Users.get(userId, function (err, user) {
	// user object will be equal to the originally saved data
});
```

In the above examples you could replace _set_ and _get_ with _setJson_ and 
_getJson_ and the encryption would still work since the pre and post functions 
are called directly before the safe after the JSON is stringified (set) and 
before it's parsed (get).

## Changelog

#### 1.4.0
Added support for pre-save and post-load transforms.

#### pre 1.4.0
Not documented...