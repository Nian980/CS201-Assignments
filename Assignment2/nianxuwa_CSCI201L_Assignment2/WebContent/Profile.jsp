<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="UTF-8">
<title>Profile</title>
<link rel="stylesheet" type="text/css" href="CSS/Profile.css">
<script>
function getInfo() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "Profile", true);
	xhttp.onreadystatechange = function() { //callback function
		document.getElementById("content").innerHTML = this.responseText;
	};
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhttp.send();
}
</script>
</head>

<body class="main-container" onload="getInfo()">
	<header class="header">
		<p class="app-name-button">
			<a href="LoggedIn.jsp">Sycamore Calendar</a>
		</p>
		<p class="profile-button">
			<a href="Profile.jsp">Profile</a>
		</p>
		<p class="home-button">
			<a href="Home.jsp">Home</a>
		</p>
	</header>

	<div class="main">
		<p class="title">Upcoming Events</p>
		<div id="content" class="content-style">
			
		</div>
	</div>

	<footer class="footer"> </footer>
</body>

</html>