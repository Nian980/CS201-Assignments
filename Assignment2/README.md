Assignment2

Google OAuth redirect_uri choice: http://localhost:8080

Please run the assignment from SignIn.jsp, as other pages won't work until the user provided authorization.

The website has been optimised for a screen resolution of around 1440 x 1024.

Home.jsp's add events function allows a user to add a time that is not in 15min increments. In the actual Google Calendar, dragging and selecting a time for the event only goes in 15min increments, but you can manually set the time to whatever minute, e.g. you can start an event at 6:02pm or something, and my implementation reflects that choice. 

(Home.jsp's add event error checking is done on the backend, and reloads the page with the error message added if there are any errors. It also ends up clearing the inputs. It would probably have been better to do it the way the solution was done in lab5 so inputs are retained, but my redirect does not seem to work with XMLHttpRequest(), but I think this is fine as far as assignment requirements go.)

When the user goes to the LoggedIn page, and click on the button to sign out, they will be signed out and redirected to the sign in page. For me, there will be a brief popup but it goes away quickly and does not affect the functionality of the site. But that may be because of my Chrome settings? For the grader they may have a popup asking to choose an account, I'm not sure. 

The event added time may be affected by daylight savings. If the user e.g. add an event late december, the time actually added onto Google calendar will be an hour before the time the user specified, e.g. an event added at 8pm will be shown as 7pm on Google Calendar. This does not affect most events added, i.e. you can add events in October just fine and it will give you the correct time. 