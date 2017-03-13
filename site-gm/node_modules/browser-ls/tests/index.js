'use strict';

/*jshint expr: true*/

// ls module will be global as it binds to window
var expect = chai.expect
  , sinon = window.sinon
  , ls = window.ls
  , TEST_STR_KEY = 'TEST_STR_KEY'
  , TEST_STR = 'TEST_STR'
  , TEST_OBJ_KEY = 'TEST_OBJ_KEY'
  , TEST_OBJ = {
    hello: 'world'
  };

// Remove everything from localStorage
Object.keys(window.localStorage).forEach(function (k) {
  window.localStorage.removeItem(k);
});


describe('ls Module', function () {

  afterEach(function () {
    localStorage.removeItem(TEST_STR_KEY);
    localStorage.removeItem(TEST_OBJ_KEY);
  });

  beforeEach(function () {
    window.localStorage.clear();
  });

  describe('ls Object', function () {
    it('Should be defined on window', function () {
      expect(window.ls).to.be.defined;
    });
  });


  describe('Events', function () {

    describe('Success Events', function () {
      var adapter = ls.getAdapter('Events');

      function evtCheck (evt, fn, args) {
        var cb = sinon.spy();
        adapter.on(evt, cb);
        fn.apply(adapter, args);

        expect(cb.called).to.equal(true);
      }

      it ('Should call the SET callback', function () {
        evtCheck(adapter.EVENTS.SET, adapter.set, [TEST_STR_KEY, TEST_STR]);
      });

      it ('Should call the GET callback', function () {
        evtCheck(adapter.EVENTS.GET, adapter.get, [TEST_STR_KEY]);
      });

      it ('Should call the REMOVE callback', function () {
        evtCheck(adapter.EVENTS.REMOVE, adapter.remove, [TEST_STR_KEY]);
      });
    });

    describe('Error Events', function () {
      var adapter = ls.getAdapter('ErrorEvents');

      function errEvtCheck (evt, fn, args) {
        var cb = sinon.spy();
        adapter.on(evt, cb);
        fn.apply(adapter, args);

        expect(cb.called).to.equal(true);
      }

      it ('Should call the GET_FAILED callback', function () {
        var getStub = sinon.stub(window.localStorage, 'getItem').throws();

        errEvtCheck(adapter.EVENTS.GET_FAILED, adapter.get, [TEST_STR_KEY]);
        getStub.restore();
      });

      it ('Should call the SET_FAILED callback', function () {
        var setStub = sinon.stub(window.localStorage, 'setItem').throws();

        errEvtCheck(adapter.EVENTS.SET_FAILED, adapter.set, [TEST_STR_KEY, TEST_STR]);
        setStub.restore();
      });

      it ('Should call the REMOVE_FAILED callback', function () {
        var rmStub = sinon.stub(window.localStorage, 'removeItem').throws();

        errEvtCheck(adapter.EVENTS.REMOVE_FAILED, adapter.remove, [TEST_STR_KEY]);
        rmStub.restore();
      });
    });

  });


  describe('ls Adapters & Reuse', function () {
    describe('#getAdapter', function () {
      it ('Should return an instance of a LocalStorage adapter', function () {
        var instance = ls.getAdapter('Tester');
        expect(instance.get).to.be.a('function');
        expect(instance.getNs()).to.be.a('string');
        expect(instance.getNs()).to.equal('Tester');
      });
    });
  });


  describe('#getKeys', function () {
    beforeEach(function () {
      window.localStorage.clear();
    });

    it('Should return an empty array', function () {
      var keys = ls.getAdapter('ABC').getKeys();
      expect(keys).to.be.an('Array');
      expect(keys).to.have.length(0);
    });

    it('Should return a single key', function (done) {
      ls.getAdapter('ABC').set(TEST_STR, TEST_STR, function (err) {
        expect(err).to.be.null;

        var keys = ls.getAdapter('ABC').getKeys();
        expect(keys).to.be.an('Array');
        expect(keys).to.have.length(1);
        done();
      });
    });
  });

  describe('#get', function () {
    it('Should try get an item not in localStorage', function () {
      ls.get(TEST_STR_KEY, function (err, res) {
        expect(err).to.be.null;
        expect(res).to.be.null;
      });
    });

    it('Should get an item from localStorage', function () {
      localStorage.setItem(TEST_STR_KEY, TEST_STR);

      ls.get(TEST_STR_KEY, function (err, res) {
        expect(err).to.be.null;
        expect(res).to.be.a('string');
        expect(res).to.equal(TEST_STR);
      });
    });

    it('Should modify the value before being set', function () {
      var adp = window.ls.getAdapter({
        preSave: function (val, callback) {
          callback(null, val.replace('evan', ''));
        }
      });

      adp.set(TEST_STR_KEY, 'evanshortiss', function (err) {
        expect(err).to.be.null;
        adp.get(TEST_STR_KEY, function (e, res) {
          expect(e).to.be.null;
          expect(res).to.equal('shortiss');
        });
      });
    });
  });


  describe('#getJson', function () {
    it('Should try get an item not in localStorage', function () {
      ls.getJson(TEST_OBJ_KEY, function (err, res) {
        expect(err).to.be.null;
        expect(res).to.be.null;
      });
    });

    it('Should get an item from localStorage', function () {
      localStorage.setItem(TEST_OBJ_KEY, JSON.stringify(TEST_OBJ));

      ls.getJson(TEST_OBJ_KEY, function (err, res) {
        expect(err).to.be.null;
        expect(res).to.be.an('object');
        expect(JSON.stringify(res)).to.equal(JSON.stringify(TEST_OBJ));
      });
    });
  });


  describe('#set', function () {
    it ('Should set an item in localStorage', function () {
      ls.set(TEST_STR_KEY, TEST_STR, function (err) {
        expect(err).to.be.null;

        ls.get(TEST_STR_KEY, function (err, res) {
          expect(err).to.be.null;
          expect(res).to.be.a('string');
          expect(res).to.equal(TEST_STR);
        });
      });
    });

    it ('Should modify an item during retreival', function () {
      var adp = window.ls.getAdapter({
        postLoad: function (val, callback) {
          callback(null, val.replace('evan', ''));
        }
      });

      adp.set(TEST_STR_KEY, 'evanshortiss', function (err) {
        expect(err).to.be.null;
        adp.get(TEST_STR_KEY, function (e, res) {
          expect(e).to.be.null;
          expect(res).to.equal('shortiss');
        });
      });
    });
  });

  describe('#clear', function () {
    it('Should clear keys that have been added', function (done) {
      var adapter = ls.getAdapter('Clear Test');

      adapter.set(TEST_STR, TEST_STR, function (err) {
        expect(err).to.be.null;
        expect(adapter.getKeys()).to.have.length(1);

        adapter.clear(function (clearErr) {
          expect(clearErr).to.be.null;
          expect(adapter.getKeys()).to.have.length(0);
          done();
        });
      });
    });
  });

  describe('#setJson', function () {
    it ('Should set a JSON object and retreive it', function () {
      ls.setJson(TEST_OBJ_KEY, TEST_OBJ, function (err) {
        expect(err).to.be.null;

        ls.getJson(TEST_OBJ_KEY, function (err, res) {
          expect(err).to.be.null;
          expect(res).to.be.an('object');
          expect(JSON.stringify(res)).to.equal(JSON.stringify(TEST_OBJ));
        });
      });
    });
  });


  describe('#remove', function () {
    it ('Should set an item, remove it, verify it was removed', function () {
      ls.set(TEST_STR_KEY, TEST_STR, function (err) {
        expect(err).to.be.null;

        ls.remove(TEST_STR_KEY, function (err) {
          expect(err).to.be.null;

          ls.get(TEST_STR_KEY, function (err, res) {
            expect(err).to.be.null;
            expect(res).to.be.null;
          });
        });
      });
    });
  });

});
