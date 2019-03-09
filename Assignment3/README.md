Assignment 3

Building the database:
- Run database.sql to create the schema and the structure of the tables for my database. 
- I use DELETE statements in my code. If needed, go to MySQLWorkbench -> Preferences -> SQL Editor -> Safe Updates, uncheck the box to turn it off to allow deleting. 

JDBC Connecting to database:
- In the file JDBCDriver.java line 12, change the connection path to whatever is needed (username, password...etc.) to link to the MySQL database correctly. 

Viewing the website on the browser:
- The pages would look best if on Chrome you Right Click -> Inspect, and change the page size to 1440 x 1024, as they were designed with that screen size in mind. 

Occasional session/loading issues for friend profiles:
- A refresh should fix the problem, there are sometimes weird connection issues with JDBC Driver. 

Current user's Profile page HTTP500:
- This is a Tomcat issue. 
- It sometimes may show HTTP500 and null pointer exception for the profile page. The error line will be 48 in Profile.jsp when it tries to access calendar. It is due to an illegal state exception caused by Tomcat stopping the web application instance, which is caused by Tomcat forcefully shutting it down due to it seeing a possible memory leak (Tomcat ships with some memory leak detection stuff)...etc. It basically breaks the sessions related to Google and hence calendar object and googeID...etc. becomes NULL.
- To fix it, just go to the LoggedIn page to sign-out and sign back in again and it should be fine. This was something that occasionally happened in Assignment 2 as well. 
- I know many friends with the same issue. 

Adding events:
- When we click on another user's event to add it and confirm OK, it will create a copy of that event (with new eventID) to add to the current user's events in the database.

Small issues that may or may not appear:
- When logging out, the Google prompt for sign-in will probably pop-up again, and it may be frozen / not working for some reason . Just close the sign-in pop-up, it doesn't affect the logout functionality, and you should already be redirected to the sign-in page. 
- (Small issue I met when running my JSPs on the server: If some/all of the JSP pages are 404, go to project Properties -> Web Project Settings -> Restore Defaults (for the context path). That's just the project path in the URL, and it just may need a refresh for some reason.)
- Not sure if this will be an issue, but if it is: after unzipping the project and when running on the grader's server, if it somehow doesn't recognise stuff, try going to credentials.json (in src folder) and changing the redirect_uirs to "Assignment2" instead of the part that says "Assignment3".
