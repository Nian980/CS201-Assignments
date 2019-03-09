import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Servlet implementation class Profile
 */
@WebServlet("/Profile")
public class Profile extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Profile() {
        super();
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(false); //"false" mean do not create a new session
		String fullName = (String)session.getAttribute("name");
		String imageURL = (String)session.getAttribute("image");
		Calendar myCalendar = (Calendar)session.getAttribute("calendar");
		
		String googleId = (String)session.getAttribute("googleId");
		String accessToken = (String)session.getAttribute("accessToken");
		
//		System.out.println("googleId before = " + googleId);
//		System.out.println("fullname = " + fullName);
//		System.out.println("imageURL = " + imageURL);
//		System.out.println("myCalendar = " + myCalendar);
		
		if (myCalendar == null) { //if its null
			//go back to signin page and sign in again, this is a very difficult issue to solve
			response.sendRedirect("SignIn.jsp");
		}
		
		//content type tells browser which type you're sending: json, xml, image, video...
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		
//		System.out.println("myCalendar after = " + myCalendar);
		
		// List the next 30 events from the primary calendar - from the Quickstart guide
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = myCalendar.events().list("primary")
                .setMaxResults(30)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
		List<Event> items = events.getItems();
        if (items.isEmpty()) {
        	out.println("<div class=\"table-wrapper\">"); 
        	
            out.println("<p class=\"no-events\">");
            out.println("No upcoming events found.");
            out.println("</p>");
            
            out.println("</div>");
        } else {
            
            //Now print out content, including table
            out.println("<div class=\"table-wrapper\">");
            
            out.println("<table class=\"event-table\">");
            out.println("<tr>");
            out.println("<th>");
            out.println("Date");
            out.println("</th>");
            
            out.println("<th>");
            out.println("Time");
            out.println("</th>");
            
            out.println("<th>");
            out.println("Event Summary");
            out.println("</th>");
            out.println("</tr>");
  
            //Note: the below only gets start date/time of event
            for (Event event : items) {
            	boolean allDayEvent = false;
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate(); //just get Date if no time?
                    allDayEvent = true;
//                    System.out.println("all day event");
//                    System.out.println(start);
                }

                String summary = event.getSummary();

                String startDateString = start.toString();
                
                String[] initialDateArray = startDateString.split("-");
                String year = initialDateArray[0];
                String month = initialDateArray[1];
                String dayAndRest = initialDateArray[2];
                
//                System.out.println(year);
//                System.out.println(month);
//                System.out.println(dayAndRest);
                
                if (month.equals("01")) {
                	month = "January";
                } else if (month.equals("02")) {
                	month = "February";
                } else if (month.equals("03")) {
                	month = "March";
                } else if (month.equals("04")) {
                	month = "April";
                } else if (month.equals("05")) {
                	month = "May";
                } else if (month.equals("06")) {
                	month = "June";
                } else if (month.equals("07")) {
                	month = "July";
                } else if (month.equals("08")) {
                	month = "August";
                } else if (month.equals("09")) {
                	month = "September";
                } else if (month.equals("10")) {
                	month = "October";
                } else if (month.equals("11")) {
                	month = "November";
                } else if (month.equals("12")) {
                	month = "December";
                }

                String day = "";
                String hour = "";
                String minute = "";
                String amOrPm = "";
                
                //if it's an all day event, it will be of format 2018-10-18, and you only have the date
                if (allDayEvent) {
                	day = dayAndRest;
                } else {
                	String[] dayAndTime = dayAndRest.split("T");
                    day = dayAndTime[0];
                    String timeAndRest = dayAndTime[1];
          
                    String[] times = timeAndRest.split(":");
                    hour = times[0];
                    minute = times[1];
                    
                    int hourInt = Integer.parseInt(hour);
                    boolean pm = false;
                    amOrPm = "";
                    if (hourInt > 12) {
                    	hourInt -= 12;
                    	pm = true;
                    }
                    hour = Integer.toString(hourInt);
                       
                    if (pm || hour.equals("12")) {
                    	amOrPm = "PM";
                    } else {
                    	amOrPm = "AM";
                    }
                    if (hour.equals("0")) { //if 0:00, use 12am cuz we have
                    	hour = "12";
                    }
                }
                
                //print to Profile.jsp page
                String finalDate = month + " " + day + ", " + year;
                String finalTime = "";
                
                if (allDayEvent) {
                	finalTime = "All Day";
                } else {
                	finalTime = hour + ":" + minute + " " + amOrPm;
                }
                
                out.println("<tr>");
                
                out.println("<td>"); //date
                out.println(finalDate);
                out.println("</td>");
                
                out.println("<td>"); //time
                out.println(finalTime);
                out.println("</td>");
                
                out.println("<td>"); //event summary
                out.println(summary);
                out.println("</td>");
                
                out.println("</tr>");
            }
            
            out.println("</table>");
            out.println("</div>");
        }
        //finished table, now print out user stuff
        
        out.println("<div class=\"user\">");
        out.print("<img class=\"profile-pic\" src=\"");
        out.print(imageURL);
        out.println("\" alt=\"Tommy Trojan\" width=\"250px\" height=\"250px\">");
        out.print("<p class=\"profile-name-class\">");
        out.print(fullName);
        out.println("</p>\n </div>");
	}

}
