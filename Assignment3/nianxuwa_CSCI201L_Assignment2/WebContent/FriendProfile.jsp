<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Profile</title>
<!-- just using the Profile.css's style should be fine -->
<link rel="stylesheet" type="text/css" href="CSS/Profile.css">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.4.1/css/all.css" integrity="sha384-5sAR7xN1Nv6T6+dT2mhtzEpVJvfS3NScPQTrOxhwjIuvcA67KV2R5Jz6kr4abQsz" crossorigin="anonymous">
<script>
function getInfo(followParam) {
	/* follow param: 0 let the servlet determine, 1 follow, 2 unfollow */
	var url_string = window.location.href;
	var url = new URL(url_string);
	var friendID = url.searchParams.get("userid");
	// console.log(friendID); //DEBUG
	
	//send userID to backend, get that user's profile
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "FriendProfile", true);
	xhttp.onreadystatechange = function() { //callback function
		document.getElementById("content").innerHTML = this.responseText;
	};
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhttp.send("friend=" + friendID + "&follow=" + followParam);
}

function toggleButtons() {
	var url_string = window.location.href;
	var url = new URL(url_string);
	var friendID = url.searchParams.get("userid");

    if (unfollow.style.display == "none") { //will be following user, so display Unfollow button
		getInfo(1);
    }
    else if (follow.style.display = "none") { //will be unfollowing user, so display Follow button
        getInfo(2);
    }
}

function clickedEvent(clicked_eventid, trTag) {
	var url_string = window.location.href;
	var url = new URL(url_string);
	var friendID = url.searchParams.get("userid");
	
    //access 3rd td in this tr tag
    var tdText = trTag.getElementsByTagName('td')[2].innerHTML; //get event summary of that row
    var addEventConfirm = confirm("Do you want to add this event to your calendar?\n" + tdText);
    
    if (addEventConfirm) {
    	var xhttp = new XMLHttpRequest();
    	xhttp.open("POST", "AddAnEvent", true);
    	xhttp.onreadystatechange = function() {
    		getInfo(0);
    	};
    	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    	xhttp.send("friend=" + friendID + "&eventid=" + clicked_eventid);
    }
}

</script>
</head>

<body class="main-container" onload="getInfo(0)">
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

	<div class="main" id="content">
		<!-- Note: by default, follow will initially be displayed, and unfollow is hidden -->
        
	</div>

	<footer class="footer"></footer>
</body>

</html>