'use strict';

var ls = window.localStorage
  , safejson = require('safejson')
  , events = require('events')
  , async = require('async')
  , util = require('util');


/**
 * Object represnting possible events that istances will emit.
 * Each instance has this attached to it for accessibility.
 * @api public
 * @type {Object}
 */
var EVENTS = {
  SET: 'SET',
  SET_FAILED: 'SET_FAILED',
  GET: 'GET',
  GET_FAILED: 'GET_FAILED',
  REMOVE: 'REMOVE',
  REMOVE_FAILED: 'REMOVE_FAILED'
};


/**
 * Wrap an optional callback for safe execution.
 * Returns an empty callback if none is provided.
 * @param  {Function} [callback]
 * @return {Function}
 * @api private
 */
function wrapCallback (callback) {
  return callback || function () {};
}


/**
 * Generate a period separated storage key
 * @param  {String} ns  Namespace being used
 * @param  {String} key Actual key of the data
 * @return {String} The generated namespaced key
 * @api private
 */
function genStorageKey (ns, key) {
  return (ns === '') ? ns.concat(key) : ns.concat('.').concat(key);
}

/**
 * Default drop in for processor hook for pre-save and post-load
 * @param  {String}   v
 * @param  {Function} callback
 */
function defProcessor (v, callback) {
  callback(null, v);
}


/**
 * Class used to provide a wrapper over localStorage
 * @param {String} [ns] Optional namespace for the adpater.
 */
function LocalStorage (params) {
  events.EventEmitter.call(this);

  if (!params || typeof params === 'string') {
    params = {
      ns: params || ''
    };
  }

  this.ns = params.ns || '';

  if (typeof this.ns !== 'string') {
    throw new Error('Namespace must be a string.');
  }

  this.preSave = params.preSave || defProcessor;
  this.postLoad = params.postLoad || defProcessor;

  if (this.preSave && typeof this.preSave !== 'function') {
    throw new Error('preSave option must be a function');
  }

  if (this.postLoad && typeof this.postLoad !== 'function') {
    throw new Error('postLoad option must be a function');
  }

  this.EVENTS = EVENTS;
}
util.inherits(LocalStorage, events.EventEmitter);
module.exports = LocalStorage;


/**
 * Get the namespace of the this LocalStorage object
 * @return {String} The namespace used to prefix all keys for this instance
 */
LocalStorage.prototype.getNs = function () {
  return this.ns;
};


/**
 * Get all keys in this adapter
 * @return {Array}
 */
LocalStorage.prototype.getKeys = function () {
  var keys = Object.keys(ls)
    , ns = this.getNs().concat('.');

  return keys
    .filter(function (k) {
      return (k.indexOf(ns) === 0);
    })
    .map(function (k) {
      return k.replace(ns, '');
    });
};


/**
 * Set a value in localStorage.
 * @param {String}    key The under which data should be stored
 * @param {String}    value The value to store
 * @param {Function}  [callback] Optional callback for getting operation results
 */
LocalStorage.prototype.set = function (key, value, callback) {
  callback = wrapCallback(callback);
  var self = this;

  this.preSave(value, function (err, newVal) {
    if (err) {
      callback(err, null);
    } else {
      try {
        ls.setItem(genStorageKey(self.getNs(), key), newVal);
        callback(null, null);
        self.emit(EVENTS.SET, key, value, newVal);
      } catch (e) {
        callback(e, null);
        self.emit(EVENTS.SET_FAILED, key, value, newVal);
      }
    }
  });
};


/**
 * Useful method for saving JSON data without needing to stringify.
 * @param {String} key The key to store data under
 * @param {Object} json The JSON object to store
 * @param {Function} [callback] Optional callback for getting operation results
 */
LocalStorage.prototype.setJson = function (key, json, callback) {
  var self = this;
  callback = wrapCallback(callback);

  safejson.stringify(json, function (err, str) {
    if (err) {
      callback(err, null);

      self.emit(EVENTS.SET_FAILED, key, json);
    } else {
      self.set(key, str, callback);
    }
  });
};


/**
 * Get a value from localStorage.
 * @param {String} key Key of the value to get
 * @param {Function} [callback] Optional callback for getting operation results
 */
LocalStorage.prototype.get = function (key, callback, silent) {
  callback = wrapCallback(callback);

  var self = this;

  // Pretty sure this doesn't throw, but let's be careful...
  try {
    var item = ls.getItem(genStorageKey(this.getNs(), key));

    this.postLoad(item, function (err, newVal) {
      callback(err, newVal);

      if (!silent) {
        self.emit(EVENTS.GET, key, item, newVal);
      }
    });
  } catch (e) {
    if (!silent) {
      this.emit(EVENTS.GET_FAILED, key);
    }
    callback(e, null);
  }
};


/**
 * Clear all stored keys for this instances namespace.
 * @param  {Function} callback
 */
LocalStorage.prototype.clear = function (callback) {
  var self = this;

  async.eachSeries(this.getKeys(), function (k, cb) {
    self.remove(k, cb);
  }, function (err) {
    // Ensure error passed is null to keep with standards
    callback(err || null, null);
  });
};


/**
 * Useful method for getting JSON data directly as an object.
 * @param {String} key Key of Object to retrieve
 * @param {Function} [callback] Optional callback used to get results.
 */
LocalStorage.prototype.getJson = function (key, callback) {
  callback = wrapCallback(callback);

  var self = this;

  this.get(key, function (err, res) {
    if (err) {
      callback(err, null);
      self.emit(EVENTS.GET_FAILED, key);
    } else {
      safejson.parse(res, function (err, res) {
        if (err) {
          callback(err, null);

          self.emit(EVENTS.GET_FAILED, key);
        } else {
          callback(null, res);

          self.emit(EVENTS.GET, key, res);
        }
      });
    }
  }, true);
};


/**
 * Remove an item from localStorage.
 * @param {String} key Key of the object to remove
 * @param {Function} [callback] Optional callback used to get results.
 */
LocalStorage.prototype.remove = function (key, callback) {
  callback = wrapCallback(callback);

  // Pretty sure this doesn't throw, but let's be careful...
  try {
    ls.removeItem(genStorageKey(this.getNs(), key));
    callback(null, null);
    this.emit(EVENTS.REMOVE, key);
  } catch (e) {
    callback(e, null);
    this.emit(EVENTS.REMOVE_FAILED, key);
  }
};
