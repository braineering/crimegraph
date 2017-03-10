'use strict';


function RestAPI(type) 
{

  if(type == "start") {
    
    var datasetchoice = document.getElementById('dataset_combobox').value;
    var potentialchoice = document.getElementById('potential_combobox').value;
    var hiddenchoice = document.getElementById('hidden_combobox').value;
    
    var command = {};

    command.dataset = datasetchoice;
    command.potential = potentialchoice;
    command.hidden = hiddenchoice;

    if(potentialchoice == "QuasiLocal") {
      command.steps = document.getElementById('steps').value;
    }
    else if(potentialchoice == "WeightedQuasiLocal") {
      var steps = document.getElementById('steps').value;
      command.steps = steps;
      var weights = [];
      var data = document.getElementById('weightedsteps').value.split(';');

      var size = Object.keys(data).length;
      
      if(size != steps){
        alert("Cardinality of weights added is different of the steps number");
        return false;
      }
      
      var sum = 0.0;

      for(var i = 0; i<size;i++){

        if(!data[i].match(/^\d{0,2}(?:\.\d{0,2}){0,1}$/)){
          alert("The added weights are not the correct format. Usage:'[value1 ; value2 ; ... ; valueN]' where N is the value added in the 'steps' field. ");
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
      command.weights = weights;
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
};