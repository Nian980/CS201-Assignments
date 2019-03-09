

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Servlet implementation class AddEventSuccess
 */
@WebServlet("/AddEventSuccess")
public class AddEventSuccess extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public AddEventSuccess() {
        super();
    }

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		System.out.println("Inside AddEventSuccess servlet");
		
		HttpSession session = request.getSession(false); //"false" mean do not create a new session

		String eventTitle = (String)session.getAttribute("eventTitle");
		String startDate =(String)session.getAttribute("startDate");
		String endDate =(String)session.getAttribute("endDate");
		String startTime =(String)session.getAttribute("startTime");
		String endTime =(String)session.getAttribute("endTime");
		
		Calendar myCalendar = (Calendar)session.getAttribute("calendar");
		
		//debug
//		System.out.println("In AddEventSuccess servlet: ");
//		System.out.println("eventTitle = " + eventTitle);
//		System.out.println("startDate = " + startDate);
//		System.out.println("endDate = " + endDate);
//		System.out.println("startTime = " + startTime);
//		System.out.println("endTime = " + endTime);

		response.setContentType("text/html");
			
		//add event to calendar
		Event event = new Event().setSummary(eventTitle);
		
		String myStartDateTime = startDate + "T" + startTime + ":00-07:00";
//		System.out.println("myStartDateTime = " + myStartDateTime); //DEBUG
		DateTime startDateTime = new DateTime(myStartDateTime);
		EventDateTime start = new EventDateTime()
			    .setDateTime(startDateTime)
			    .setTimeZone("America/Los_Angeles");
		event.setStart(start);

		String myEndDateTime = endDate + "T" + endTime + ":00-07:00";
//		System.out.println("myEndDateTime = " + myEndDateTime); //DEBUG
		DateTime endDateTime = new DateTime(myEndDateTime);
		EventDateTime end = new EventDateTime()
		    .setDateTime(endDateTime)
		    .setTimeZone("America/Los_Angeles");
		event.setEnd(end);

		String calendarId = "primary";
		event = myCalendar.events().insert(calendarId, event).execute();
		
//		System.out.printf("Event created: %s\n", event.getHtmlLink()); //DEBUG
		
	}
}
