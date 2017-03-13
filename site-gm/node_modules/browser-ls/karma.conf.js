'use strict';

module.exports = function(config) {
  config.set({

    frameworks: ['mocha', 'chai', 'sinon'],

    files: [
      './dist/ls.js',
      './tests/*.js'
    ],

    reporters: ['mocha'],
    port: 9876,
    colors: true,
    logLevel: 'INFO',
    captureTimeout: 60000,

    autoWatch: false,

    browsers: ['PhantomJS'],

    singleRun: true
  });
};
