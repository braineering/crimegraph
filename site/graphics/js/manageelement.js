'use strict';

function changeFunction(type)
{
  if(type == 'potential_combobox') {
    
    var choice = document.getElementById('potential_combobox').value;

    if(choice == "QUASI_LOCAL") {
       $('#div_locality_option_potential').css("display","inherit");
    }
    else if(choice =="WEIGHTED_QUASI_LOCAL") {
      $('#div_locality_option_potential').css("display","inherit");
      $('#div_weights_option_potential').css("display","inherit");
    }
    else {
      $('#div_locality_option_potential').css("display","none");
      $('#div_weights_option_potential').css("display","none"); 
    }
  }

  if(type == 'hidden_combobox') {
    
    var choice = document.getElementById('hidden_combobox').value;

    if(choice == "QUASI_LOCAL") {
       $('#div_locality_option_hidden').css("display","inherit");
    }
    else if(choice =="WEIGHTED_QUASI_LOCAL") {
      $('#div_locality_option_hidden').css("display","inherit");
      $('#div_weights_option_hidden').css("display","inherit");
    }
    else {
      $('#div_locality_option_hidden').css("display","none");
      $('#div_weights_option_hidden').css("display","none"); 
    }
  }
  return true;
};
