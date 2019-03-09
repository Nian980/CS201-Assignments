<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
//This part is required to "initialize" the errorMsg variable, cuz using it down below it wouldn't know what it is
String errorMsg = (String)request.getAttribute("errorMsg"); //need to cast to string
if (errorMsg == null) {
	errorMsg = "";
}
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Home</title>
<link rel="stylesheet" type="text/css" href="CSS/Home.css">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.4.1/css/all.css" integrity="sha384-5sAR7xN1Nv6T6+dT2mhtzEpVJvfS3NScPQTrOxhwjIuvcA67KV2R5Jz6kr4abQsz" crossorigin="anonymous">
<script>
function getFollowers() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "HomeFollowers", true); //send to the HomeFollowers servlet
	xhttp.onreadystatechange = function() { //callback function
		document.getElementById("following-users").innerHTML = this.responseText;
	};
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhttp.send();
}
</script>
</head>
<body onload="getFollowers()">
	<header class="header">
		<a href="LoggedIn.jsp" class="app-name-link">Sycamore Calendar</a>
        <div class="search-bar-container">
            <form class="search-bar-form" action="SearchResults.jsp" method="GET">
                <input type="text" name="searchTerms" placeholder="Search Friends">
                <button type="submit" name="submit"><i class="fa fa-search fa-3x"></i></button>
            </form>
        </div>
        <a href="Profile.jsp" class="profile-link">Profile</a>
        <a href="Home.jsp" class="home-link">Home</a>
    </header>

    <div class="main">
        <p class="home-title">Home</p>
        <div class="center-box">
        <!-- Note: use = inside the jsp to output the result of the expression between the brackets to the screen -->
            <img class="profile-pic" src="<%=session.getAttribute("image")%>" alt="<%=session.getAttribute("name")%>" width="280px" height="280px">
            <p id="profile-name-id" class="profile-name-class"><%=session.getAttribute("name")%></p>
            
            <p style="color: red; font-weight: bold; margin-left: 3%;" id="errMessage"><%=errorMsg%></p>
            
            <form id="event-form" method="GET" name="eventForm" action="Home">
                <button class="addEvent-button" type="submit" value="Submit">Add Event</button>
                <input class="title-input" type="text" name="eventTitle" value="Event Title"><br>
                <input class="startDateTime-input" type="text" onfocus="(this.type = 'date')" name="startDate" value="Start Date" />
                <input class="endDateTime-input" type="text" onfocus="(this.type = 'date')" name="endDate" value="End Date" /><br>
                <input class="startDateTime-input" type="text" onfocus="(this.type = 'time')" name="startTime" value="Start Time">
                <input class="endDateTime-input" type="text" onfocus="(this.type = 'time')" name="endTime" value="End Time">
            </form>
            
            <p class="following-title">Following</p>
            
            <div class="user-grid-container" id="following-users">
            	
            </div>   
        </div>
    </div>

    <footer class="footer"></footer>
</body>
</html>