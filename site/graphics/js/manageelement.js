'use strict';

function changeFunction(type)
{
  if(type == 'potential_combobox') {
    console.log('selezionato potential');
    
    var choice = document.getElementById('potential_combobox').value;

    if(choice == "QuasiLocal") {
       $('#div_steps_option').css("display","inherit");
    }
    else if(choice =="WeightedQuasiLocal") {
      $('#div_steps_option').css("display","inherit");
      $('#div_steps_weight_option').css("display","inherit");
    }
    else {
      $('#div_steps_option').css("display","none");
      $('#div_steps_weight_option').css("display","none"); 
    }
  }
  return true;
};
