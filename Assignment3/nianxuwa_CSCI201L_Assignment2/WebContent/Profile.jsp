<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="UTF-8">
<title>Profile</title>
<link rel="stylesheet" type="text/css" href="CSS/Profile.css">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.4.1/css/all.css" integrity="sha384-5sAR7xN1Nv6T6+dT2mhtzEpVJvfS3NScPQTrOxhwjIuvcA67KV2R5Jz6kr4abQsz" crossorigin="anonymous">
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
		<a href="LoggedIn.jsp" class="app-name-link">Sycamore Calendar</a>
		<div class="search-bar-container">
            <form action="SearchResults.jsp" method="GET">
                <input type="text" name="searchTerms" placeholder="Search Friends">
                <button type="submit" name="submit"><i class="fa fa-search fa-3x"></i></button>
            </form>
        </div>
        <a href="Profile.jsp" class="profile-link">Profile</a>
        <a href="Home.jsp" class="home-link">Home</a>
	</header>

	<div class="main">
		<p class="title">Upcoming Events</p>
		<div id="content" class="content-style">
			<div class="table-wrapper">
				
			</div>
		</div>
	</div>

	<footer class="footer"> </footer>
</body>

</html>