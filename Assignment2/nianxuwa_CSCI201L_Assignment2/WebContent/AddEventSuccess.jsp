<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Event Add Success</title>
<script>
function getInfo() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "AddEventSuccess", true); //send to this servlet

	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhttp.send();
}
</script>
<link rel="stylesheet" type="text/css" href="CSS/AddEventSuccess.css">
</head>
<body onload="getInfo()">
    <header class="header">
        <p class="app-name-link"><a href="LoggedIn.jsp">Sycamore Calendar</a></p>
        <p class="profile-link"><a href="Profile.jsp">Profile</a></p>
        <p class="home-link"><a href="Home.jsp">Home</a></p>
    </header>
    <p class="title">Events added successfully!</p>
    <div id="content" class="content-style">
    	<br>
    	<a href="Home.jsp">Click here to go back to Home</a>
    </div>
    <footer class="footer">
    </footer>
</body>
</html>