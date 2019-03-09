<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta name="google-signin-scope" content="https://www.googleapis.com/auth/calendar">
<!-- Specify the client ID created for your app in the Google Developers Console -->
<meta name="google-signin-client_id" content="1046249806654-94khvs10um4f5sce6d58v5m05j4nlq9r.apps.googleusercontent.com">
<!-- Must include the Google Platform Library on your web pages that integrate Google Sign-In -->
<script src="https://apis.google.com/js/platform.js"></script>

<link rel="stylesheet" type="text/css" href="CSS/LoggedIn.css">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.4.1/css/all.css" integrity="sha384-5sAR7xN1Nv6T6+dT2mhtzEpVJvfS3NScPQTrOxhwjIuvcA67KV2R5Jz6kr4abQsz" crossorigin="anonymous">
<title>Sycamore Calendar</title>
</head>

<body>
	<header class="header">
		<span></span>
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
		<img src="Image/SycamoreLeaf.jpg" alt="Sycamore Leaf Logo" height="360" width="420">
		<div class="title-signin">
			<h1 class="title">Sycamore Calendar</h1>			
			<div id="signout-button" class="g-signin2" data-theme="dark">
				<script>
				var signout = document.getElementById("signout-button");
				signout.onclick = function() {
				    signOut();
				};
				</script>
				<script>
				function signOut() {
					var auth2 = gapi.auth2.getAuthInstance();
						auth2.signOut().then(function () {
						console.log('User signed out.');
					});
					window.location.assign("SignIn.jsp");
				}
				</script>
			</div>
		</div>
	</div>
	
	<div class="footer"></div>
</body>
</html>
