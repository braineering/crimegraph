/******************************************************************************
* DEPENDENCIES
******************************************************************************/
var express    = require('express');
var argv       = require('yargs').argv;
var path       = require('path');
var winston    = require('winston');

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
* HANDLERS
******************************************************************************/
function fnLandingPage(req, res) {
  res.sendFile(path.join(__dirname + '/views/index.html'));
}

function fnFlinkDashboard(req, res) {
  var ip = req.header('x-forwarded-for') || req.connection.remoteAddress;
  res.redirect('http://127.0.0.1:' + flinkPort);
}

function fnNeo4JDashboard(req, res) {
  var ip = req.header('x-forwarded-for') || req.connection.remoteAddress;
  res.redirect('http://127.0.0.1:' + neo4jPort);
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
