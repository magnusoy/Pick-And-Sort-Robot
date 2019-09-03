setInterval(function(){ // load the data from your endpoint into the div
    $("#object_list").load("/objects")
},1000);

setInterval(function(){ // load the data from your endpoint into the div
    $("#state").load("/state")
},1000);