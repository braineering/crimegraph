var assert = require('assert')
var bs = require('../')
var fixtures = require('./fixtures')

describe('browser-storage', function() {
  describe('setItem / getItem', function() {
    fixtures.forEach(function(f) {
      it('should set ' + JSON.stringify(f.value) + ' and get ' + f.expected, function() {
        bs.setItem(f.key, f.value)

        assert.equal(bs.getItem(f.key), f.expected)
      })
    })
  })
})
