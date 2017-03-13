/******************************************************************************
* DEPENDENCIES
******************************************************************************/
var express    = require('express');
var argv       = require('yargs').argv;
var path       = require('path');
var winston    = require('winston');
var bodyParser = require('body-parser');
var fs         = require('fs-extra');
var jsonFile   = require('jsonfile');
var neo4j      = require('neo4j-driver').v1;
var exec       = require('child_process').exec;
var opn        = require('opn');


/******************************************************************************
* APP SETUP
******************************************************************************/

var app = express();
app.use('/node_modules', express.static(path.join(__dirname, 'node_modules')));
app.use('/graphics', express.static(path.join(__dirname, 'graphics')));
app.use(bodyParser.json());

/******************************************************************************
* OPTIONS
******************************************************************************/

const port = argv.port || 3000;
const flinkPort = argv.flink_port || 8081;
const neo4jPort = argv.neo4j_http_port || 7474;
const neo4jBoltPort = argv.neo4j_bolt_port || 7687;
const neo4jUsername = argv.neo4j_username || "neo4j";
const neo4jPassword = argv.neo4j_password || "password";

/******************************************************************************
* HANDLERS
******************************************************************************/
function fnCrimegraphPage(req, res) {
  res.sendFile(path.join(__dirname + '/index.html'));
}

function fnFlinkDashboard(req,res) {
  res.redirect('http://crimegraph.braineering.it:'+ flinkPort);
}

function fnNeo4JDashboard(req,res) {
  res.redirect('http://crimegraph.braineering.it:'+ neo4jPort);
}

function fnStart(req, res) {
  winston.info('fnStart');
  var COMMAND = req.body;
  winston.info('New command submitted: ', JSON.stringify(COMMAND));

  var dataset;
  switch(COMMAND.dataset) { // S, M, L
    case 'S':
      dataset = '/vagrant/data/small.data';
      break;
    case 'M':
      dataset = '/vagrant/data/medium.data';
      break;
    case 'L':
      dataset = '/vagrant/data/large.data';
      break;
    default:
      dataset = '/vagrant/data/small.data';
      break;
  }

  var hiddenMetric = COMMAND.hiddenMetric | 'LOCAL'; // type: LOCAL, QUASI_LOCAL, WEIGHTED_QUASI_LOCAL
  var hiddenThreshold = COMMAND.hiddenThreshold | 0.5;
  var hiddenLocality = COMMAND.hiddenLocality | 1;
  var hiddenWeights = COMMAND.hiddenWeights | [1.0];

  var potentialMetric = COMMAND.potentialMetric | 'LOCAL'; // type: LOCAL, QUASI_LOCAL, WEIGHTED_QUASI_LOCAL
  var potentialThreshold = COMMAND.potentialThreshold | 0.5;
  var potentialLocality = COMMAND.potentialLocality | 1;
  var potentialWeights = COMMAND.potentialWeights | [1.0];

  var resetcommand = 'service restart crimegraph ' +
      '--dataset ${dataset} ' +
      '--hiddenMetric ${hiddenMetric} ' +
      '--hiddenLocality ${hiddenLocality} ' +
      '--hiddenWeights ${hiddenWeights} ' +
      '--hiddenThreshold ${hiddenThreshold} ' +
      '--potentialMetric ${potentialMetric}  ' +
      '--potentialLocality ${potentialLocality} ' +
      '--potentialWeights ${potentialWeights} ' +
      '--potentialThreshold ${potentialThreshold} ' +
      '--neo4jHostname ${neo4jHostname} ' +
      '--neo4jHostname ${neo4jUsername} ' +
      '--neo4jPassword ${neo4jPassword}';

  winston.info('Executing command ${resetCommand}');

  var restart = exec(resetcommand, function (error, stdout, stderr) {
    console.log(stdout);
    if (error !== null) {
      console.log('exec error: ' + error);
    }
  });

  // wait command execution
  restart.on('close',function(){
    console.log('command executed');
    // open flink and neo4j dashboards
    opn("http://crimegraph.braineering.it:"+flinkPort, "_blank");
    opn("http://crimegraph.braineering.it:"+neo4jPort, "_blank");
  });

  var driver = neo4j.driver("bolt://crimegraph.braineering.it:"+neo4jBoltPort, neo4j.auth.basic(neo4jUsername, neo4jPassword));

  // emptying neo4j
  var session = driver.session();
  session
  .run("MATCH (n:Person) DETACH DELETE n")
  .then(function(result){
    console.log(result);
    session.close();
    driver.close();
  });

  res.send('Command submitted');
}

function callback(err, results) {
      if (err) throw err;
      var result = results[0];
      if (!result) {
          console.log('No user found.');
      } else {
          var user = result['user'];
          console.log(user);
      }
  };

/******************************************************************************
* REST API
******************************************************************************/
app.get('/', fnCrimegraphPage);

app.get('/flink', fnFlinkDashboard);

app.get('/neo4j', fnNeo4JDashboard);

app.post('/start', fnStart);


/******************************************************************************
* START
******************************************************************************/
app.listen(port, function () {
  winston.info('Crimegraph controller ready on port %d', port);
  winston.info('Press Ctrl-C to shutdown');
});
