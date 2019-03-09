<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta name="google-signin-scope" content="https://www.googleapis.com/auth/calendar">
<!-- Specify the client ID created for your app in the Google Developers Console -->
<meta name="google-signin-client_id" content="1046249806654-94khvs10um4f5sce6d58v5m05j4nlq9r.apps.googleusercontent.com">
<!-- Must include the Google Platform Library on your web pages that integrate Google Sign-In -->
<script src="https://apis.google.com/js/platform.js?onload=init">
/* async defer: script will load asynchronously, but my code does not wait for the script to finish loading */
function init() {
	gapi.load('auth2', function() {}); //Ready
}
</script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<link rel="stylesheet" type="text/css" href="CSS/SignIn.css">
<title>Sycamore Calendar</title>
</head>

<body>
	<div class="header"></div>
	
	<div class="main">
		<img src="Image/SycamoreLeaf.jpg" alt="Sycamore Leaf Logo" height="360" width="420">
		<div class="title-signin">
			<h1 class="title">Sycamore Calendar</h1>
			<!-- the default google sign-in button -->
			<div class="g-signin2" data-onsuccess="onSignIn" data-theme="dark"></div>
			<script>
				function onSignIn(googleUser) {
					// Useful data for your client-side scripts:
					var profile = googleUser.getBasicProfile();
					
					//Pass user's google ID (below) into the backend
					var google_id = profile.getId();
					//console.log("ID: " + google_id); // Don't send this directly to your server! 
					//dOnt SeND tHIs dIrEcTlY tO YoUr sErVer

					// Don't need to pass the ID token you need to backend?
					var id_token = googleUser.getAuthResponse().id_token;
					//ID token - a secret that gives you access to user's basic profile info (name, picture). unnecessary?
					
					//get calendar access rights
					/* https://www.youtube.com/watch?v=zZt8SFivjps&index=3&list=PLNYkxOF6rcIBQCKXOfi4AUtSpMj78pX5f */
					if(googleUser.hasGrantedScopes('https://www.googleapis.com/auth/calendar'))
					{
					    console.log("We have already been granted the Calendar scope! Thanks Jamie!")
					}
					else
					{
					   googleUser.grant({'scope':'https://www.googleapis.com/auth/calendar'});
					}
					
					//Pass the permission-enabled access token below to the backend
					var access_token = googleUser.getAuthResponse().access_token;
					
					//use jQuery and ajax instead of JS to pass to backend:
					// make an ajax call to this url (servlet). default is POST
					$.ajax({
						url: "Servlet", 
						data: { //pass these data in
							"googleId": google_id,
							"accessToken": access_token,
							"fullName": profile.getName(),
							"firstName": profile.getGivenName(),
							"imageURL": profile.getImageUrl(),
							"email": profile.getEmail()
						},
						success: function(result) { //if successful,
							window.location.assign("Profile.jsp"); //redirect to this page
						}
					});
				};
			</script>
		</div>
	</div>
	
	<div class="footer"></div>
</body>
</html>
