import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
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

		//content type tells browser which type you're sending: json, xml, image, video...
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		
		// List the next 10 events from the primary calendar - from the Quickstart guide
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = myCalendar.events().list("primary")
                .setMaxResults(20)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
		List<Event> items = events.getItems();
        if (items.isEmpty()) {
        	out.println("<div class=\"table-wrapper\">"); 
        	
            out.println("<p>");
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
  
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate(); //just get Date if no time?
                }

                String summary = event.getSummary();

                String startDateString = start.toString();
                
                String[] initialDateArray = startDateString.split("-");
                String year = initialDateArray[0];
                String month = initialDateArray[1];
                String dayAndRest = initialDateArray[2];
                
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

                String[] dayAndTime = dayAndRest.split("T");
                String day = dayAndTime[0];
                String timeAndRest = dayAndTime[1];
      
                String[] times = timeAndRest.split(":");
                String hour = times[0];
                String minute = times[1];
                
                int hourInt = Integer.parseInt(hour);
                boolean pm = false;
                String amOrPm = "";
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
                 
                //print to Profile.jsp page
                String finalDate = month + " " + day + ", " + year;
                String finalTime = hour + ":" + minute + " " + amOrPm;
                
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
