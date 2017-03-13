safejson
===

Concise library to parse and stringify JSON without the need for try catch. 
Simply use the standard Node.js pattern of providing parameters and a 
callback that takes an error as the first parameter and result as the second.

This library is nice for chaining operations using async as demonstrated below.

## Browser Support
Pretty much any browser with JSON support.
[![browser support](https://ci.testling.com/evanshortiss/safejson.png)
](https://ci.testling.com/evanshortiss/safejson)

## Install

```
npm install safejson --save
```

```
bower install safejson --save
```

## Example with Async
```javascript

var safejson = require('safejson')
  , async = require('async')
  , fs = require('fs');

exports.updateFile = function (callback) {
  async.waterfall([
    readFile,
    safejson.parse,
    doUpdate,
    safejson.stringify,
    writeFile
  ], callback); 
}

```

## Example (Parse)

```javascript
// Valid JSON object that will stringify
var VALID_OBJECT = {
  name: 'evan',
  age: 23
};

var VALID_JSON_STRING = JSON.stringify(VALID_OBJECT);


safejson.parse(VALID_JSON_STRING, function(err, json) {
  // err is null as no error would have occured due to valid input
  // json is a valid JSON object
});

```

## Example (Stringify)
```javascript
// Valid JSON object that will stringify
var VALID_OBJECT = {
  name: 'evan',
  age: 23
};

// Invalid JSON object, has a circular reference added below
var CIRCULAR_OBJECT = {
  name: 'evan',
  age: 23
};
CIRCULAR_OBJECT.cref = CIRCULAR_OBJECT;

safejson.stringify(VALID_OBJECT, function(err, json) {
  // err would be null as the object is valid json
  // json is a valid json string
});

safejson.stringify(CIRCULAR_OBJECT, function(err, str) {
  // err would be defined as the object contained a circular reference
  // str would equal null
});

```


## Configs
#### safejson.defer = {Boolean}
If true the parsing of JSON will be briefly deffered. This uses 
process.nextTick in Node.js and the appropriate browser shim 
(setTimeout for example) where necessary.

## Methods
#### safejson.stringify(value[, replacer [, space]], callback)
Does the job of JSON.stringify but handles exceptions for you. Supports all the 
usual JSON.stringify parameters, including the optional *replacer* and 
*spaces*. The last parameter must always be a callback function and is not 
optional.

#### safejson.parse(str[, reviver], callback)
Does the job of JSON.parse but handles exceptions for you. Supports all the 
usual JSON.parse parameters. The last parameter must always be a callback 
function and is not optional.