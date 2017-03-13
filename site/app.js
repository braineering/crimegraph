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
const neo4jPort = argv.neo4j_port || 7474;
const neo4jBoltPort = argv.neo4jbolt_port || 7687;
const neo4jUser = argv.neo4j_user || "neo4j";
const neo4jPass = argv.neo4j_pass || "password";

/******************************************************************************
* HANDLERS
******************************************************************************/
function fnCrimegraphPage(req, res) {
  res.sendFile(path.join(__dirname + '/index.html'));
}

function fnFlinkDashboard(req,res) {
  res.redirect('http://localhost:'+ flinkPort);
}

function fnNeo4JDashboard(req,res) {
  res.redirect('http://localhost:'+ neo4jPort);
}

function fnStart(req, res) {
  winston.info('fnStart');
  var COMMAND = req.body;
  winston.info('New command submitted: ', JSON.stringify(COMMAND));

  var dataset = COMMAND.dataset;     // type: S, M, L
  var potential = COMMAND.potential; // type: Local, QuasiLocal, WeightedQuasiLocal
  var hidden = COMMAND.hidden; // type: Local
  var steps = 1;
  var weights = new Array();

  if(COMMAND.hasOwnProperty('steps'))
    steps = COMMAND.steps;
  
  if(COMMAND.hasOwnProperty('weights'))
    weights = COMMAND.weights;

  //manca la gestione dei jar per le metriche desiderate
  
  var resetcommand = "neo4j stop; neo4j start; cd $FLINK_HOME/bin; ./stop-local.sh; ./start-local.sh;"
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
    opn("http://localhost:"+flinkPort, "_blank");
    opn("http://localhost:"+neo4jPort, "_blank");
  });

  var driver = neo4j.driver("bolt://localhost:"+neo4jBoltPort, neo4j.auth.basic(neo4jUser, neo4jPass));

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
