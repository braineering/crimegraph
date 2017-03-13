module.exports = (function() {
  if (typeof localStorage === "undefined" || localStorage === null) {
    var nodeStorage = require('node-localstorage').LocalStorage

    return new nodeStorage('localStorage')
  } else 
    return localStorage
})()
