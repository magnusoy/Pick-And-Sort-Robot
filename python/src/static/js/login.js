function validate() {
  var username = document.getElementById("username").value;
  var password = document.getElementById("password").value;
  if (username == "admin" && password == "admin") {
    window.location.replace("http://localhost:5000/");
    return false;
  }
}
