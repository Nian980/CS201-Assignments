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
<title>Sycamore Calendar</title>
</head>

<body>
	<header class="header">
        <p class="app-name-link"><a href="LoggedIn.jsp">Sycamore Calendar</a></p>
        <p class="profile-link"><a href="Profile.jsp">Profile</a></p>
        <p class="home-link"><a href="Home.jsp">Home</a></p>
    </header>
	
	<div class="main">
		<img src="Image/SycamoreLeaf.jpg" alt="Sycamore Leaf Logo" height="360" width="420">

		<div class="title-signin">
			<h1 class="title">Sycamore Calendar</h1>
			
			<!-- Just Create a fake sign-out button and call the sign out? 
			Do I need to redirect, or really just use the same page? With extra menu buttons when signed in
			Or try two button switching????
			-->
			
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
