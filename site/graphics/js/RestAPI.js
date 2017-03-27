'use strict';


function RestAPI(type) 
{

  if(type == "start") {
    
    var datasetchoice = document.getElementById('dataset_combobox').value;
    var potentialchoice = document.getElementById('potential_combobox').value;
    var hiddenchoice = document.getElementById('hidden_combobox').value;
    var hiddenThreshold = document.getElementById('hiddenThreshold').value;
    var potentialThreshold = document.getElementById('potentialThreshold').value;

    var command = {};
    command.type = "first";
    command.dataset = datasetchoice;
    command.potentialMetric = potentialchoice;
    command.hiddenMetric = hiddenchoice;

    if(!hiddenThreshold.match(/^0{0,1}(?:\.\d{0,2}){0,1}$/)){
      alert("The inserted value of hidden threshold is incorrect. Correct usage:[0.xx] for x = {0,...9}");
      return false;
    }else{
      command.hiddenThreshold = hiddenThreshold;
    }

    if(!potentialThreshold.match(/^0{0,1}(?:\.\d{0,2}){0,1}$/)){
      alert("The inserted value of potential threshold is incorrect. Correct usage:[0.xx] for x = {0,...9}");
      return false;
    }else{
      command.potentialThreshold = potentialThreshold;
    }


    if(potentialchoice == "QUASI_LOCAL") {
      command.potentialLocality = document.getElementById('potentialLocality').value;
    }
    else if(potentialchoice == "WEIGHTED_QUASI_LOCAL") {
      var steps = document.getElementById('potentialLocality').value;
      command.potentialLocality = steps;
      var weights = [];
      var data = document.getElementById('potentialWeights').value.split(';');

      var size = Object.keys(data).length;
      
      if(size != steps){
        alert("Cardinality of potential's weights added is different of the steps number");
        return false;
      }
      
      var sum = 0.0;

      for(var i = 0; i<size;i++){

        if(!data[i].match(/^\d{0,2}(?:\.\d{0,2}){0,1}$/)){
          alert("The added potential's weights are not the correct format. Usage:'[value1 ; value2 ; ... ; valueN]' where N is the value added in the 'steps' field. ");
          return false;
        }
        else{
          sum += parseFloat(data[i]);
          weights[i] = data[i];
        }
      }
      
      if(sum != 1.0) {
        alert("The sum of values inserted in weights is not 1.0");
        return false;
      }
      command.potentialWeights = weights;
    }

    if(hiddenchoice == "QUASI_LOCAL") {
      command.hiddenLocality = document.getElementById('hiddenLocality').value;
    }
    else if(hiddenchoice == "WEIGHTED_QUASI_LOCAL") {
      var steps = document.getElementById('hiddenLocality').value;
      command.hiddenLocality = steps;
      var weights = [];
      var data = document.getElementById('hiddenWeights').value.split(';');

      var size = Object.keys(data).length;
      
      if(size != steps){
        alert("Cardinality of hidden's weights added is different of the steps number");
        return false;
      }
      
      var sum = 0.0;

      for(var i = 0; i<size;i++){

        if(!data[i].match(/^\d{0,2}(?:\.\d{0,2}){0,1}$/)){
          alert("The added hidden's weights are not the correct format. Usage:'[value1 ; value2 ; ... ; valueN]' where N is the value added in the 'steps' field. ");
          return false;
        }
        else{
          sum += parseFloat(data[i]);
          weights[i] = data[i];
        }
      }
      
      if(sum != 1.0) {
        alert("The sum of values inserted in weights is not 1.0");
        return false;
      }
      command.hiddenWeights = weights;
    }

    console.log(JSON.stringify(command));

    $.ajax({
      type: 'POST',
      url: '/start',
      dataType: 'json',
      contentType: 'application/json',
      data: JSON.stringify(command)
    });
  }

  if(type == "start2") {

    var firstid = document.getElementById('first_id_option').value;
    var secondid = document.getElementById('second_id_option').value;
    var weight = document.getElementById('weight_option').value;

    var command = {};

    command.type = "second";
    command.firstid = firstid;
    command.secondid = secondid;

    if(!weight.match(/(^(0{0,1}|([1-9][0-9]*))(\.[0-9]{1,2})?$)/)){
      alert("The inserted weight is not correct. Correct usage:[(x...x).xx] for x = {0,...9}");
      return false;
    }else{
      command.weight = weight;
    }

    console.log(JSON.stringify(command));

    $.ajax({
      type: 'POST',
      url: '/start',
      dataType: 'json',
      contentType: 'application/json',
      data: JSON.stringify(command)
    });
  }

  if(type == "flink") {

    $.ajax({
      type: 'GET',
      url: '/flink',
    });
  }

  if(type == "neo4j") {

    $.ajax({
      type: 'GET',
      url: '/neo4j',
    });
  }

};