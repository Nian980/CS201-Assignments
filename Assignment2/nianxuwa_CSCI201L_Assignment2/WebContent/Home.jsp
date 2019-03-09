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

</head>
<body>
	<header class="header">
        <p class="app-name-link"><a href="LoggedIn.jsp">Sycamore Calendar</a></p>
        <p class="profile-link"><a href="Profile.jsp">Profile</a></p>
        <p class="home-link"><a href="Home.jsp">Home</a></p>
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
        </div>
    </div>

    <footer class="footer">
    </footer>
</body>
</html>