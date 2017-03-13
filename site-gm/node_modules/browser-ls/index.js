'use strict';

var LS = require('./lib/LocalStorage');

module.exports = new LS();

module.exports.getAdapter = function getAdapter (params) {
  var adapter = new LS(params);

  return adapter;
};
