setInterval(function() {
  // load the data from your endpoint into the div
  $("#object_list").load("/objects");
}, 1000);

setInterval(function() {
  // load the data from your endpoint into the div
  $("#state").load("/state");
}, 100);

$(function() {
  $("a#start").bind("click", function() {
    $.getJSON("/start", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#stop").bind("click", function() {
    $.getJSON("/stop", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#reset").bind("click", function() {
    $.getJSON("/reset", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#calibrate").bind("click", function() {
    $.getJSON("/calibrate", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#manual").bind("click", function() {
    $.getJSON("/manual", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#automatic").bind("click", function() {
    $.getJSON("/automatic", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#all").bind("click", function() {
    $.getJSON("/all", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#circles").bind("click", function() {
    $.getJSON("/circles", function(data) {});
    return false;
  });
});
$(function() {
  $("a#squares").bind("click", function() {
    $.getJSON("/squares", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#rectangles").bind("click", function() {
    $.getJSON("/rectangles", function(data) {
      //do nothing
    });
    return false;
  });
});
$(function() {
  $("a#triangles").bind("click", function() {
    $.getJSON("/triangles", function(data) {
      //do nothing
    });
    return false;
  });
});
