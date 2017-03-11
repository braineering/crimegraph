/******************************************************************************
* DEPENDENCIES
******************************************************************************/
var express    = require('express');
var argv       = require('yargs').argv;
var path       = require('path');
var winston    = require('winston');
//var ls         = require('browser-ls');
//var LStorage   = require('node-localstorage').LocalStorage;

/******************************************************************************
* APP SETUP
******************************************************************************/
var app = express();
app.use('/bower_components', express.static(path.join(__dirname, 'bower_components')));
app.use('/assets', express.static(path.join(__dirname, 'assets')));

/******************************************************************************
* OPTIONS
******************************************************************************/
const port = argv.port || 3000;
const flinkPort = argv.flink_port || 8081
const neo4jPort = argv.neo4j_port || 7474

/******************************************************************************
* LOCAL STORAGE
******************************************************************************/
/*
var localStorage = require('localStorage');
var myValue = { foo: 'bar', baz: 'quux' };
localStorage.setItem('myKey', JSON.stringify(myValue));
myValue = localStorage.getItem('myKey');
*/

/*
var store = require('store');
store.set('user', { name:'Marcus' });
*/

/*
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
*/
/*
var localStorage = new LStorage('./scratch');
localStorage.setItem('myFirstKey', 'myFirstValue');
*/
/******************************************************************************
* HANDLERS
******************************************************************************/
function fnLandingPage(req, res) {
  res.sendFile(path.join(__dirname + '/views/index.html'));
}

function fnFlinkDashboard(req, res) {
  res.redirect('http://localhost:' + flinkPort);
}

function fnNeo4JDashboard(req, res) {
  res.redirect('http://localhost:' + neo4jPort);
}

/******************************************************************************
* REST API
******************************************************************************/
app.get('/', fnLandingPage);

app.get('/flink', fnFlinkDashboard);

app.get('/neo4j', fnNeo4JDashboard);

/******************************************************************************
* START
******************************************************************************/
app.listen(port, function () {
  winston.info('Crimegraph ready on port %d', port);
  winston.info('Press Ctrl-C to shutdown');
});
