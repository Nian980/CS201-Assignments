<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Results</title>
<link rel="stylesheet" type="text/css" href="CSS/SearchResults.css">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.4.1/css/all.css" integrity="sha384-5sAR7xN1Nv6T6+dT2mhtzEpVJvfS3NScPQTrOxhwjIuvcA67KV2R5Jz6kr4abQsz" crossorigin="anonymous">

<script>
function searchResults() {
	var url_string = window.location.href;
	var url = new URL(url_string);
	var search_string = url.searchParams.get("searchTerms");
	console.log(search_string); //DEBUG
	
	//send to backend, get response
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "SearchResults", true);
	xhttp.onreadystatechange = function() { //callback function
		document.getElementById("search-results").innerHTML = this.responseText;
	};
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhttp.send("query=" + search_string);
}

</script>

</head>

<body onload="searchResults()">
	<header class="header">
        <a href="LoggedIn.jsp" class="app-name-link">Sycamore Calendar</a>
        <div class="search-bar-container">
            <form class="search-bar-form">
                <input type="text" name="searchTerms" placeholder="Search Friends" id="searchText">
                <button type="submit" name="submit"><i class="fa fa-search fa-3x"></i></button>
            </form>
        </div>
        <a href="Profile.jsp" class="profile-link">Profile</a>
        <a href="Home.jsp" class="home-link">Home</a>
    </header>
	
    <div class="user-grid-container" id="search-results">

    </div>

    <footer class="footer"></footer>
</body>
</html>