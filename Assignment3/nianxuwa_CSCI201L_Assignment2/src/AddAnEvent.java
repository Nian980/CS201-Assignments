

import java.io.IOException;
import java.sql.SQLException;

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
 * Servlet implementation class AddAnEvent
 */
@WebServlet("/AddAnEvent")
public class AddAnEvent extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddAnEvent() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JDBCDriver.connect();

		HttpSession session = request.getSession(false); //"false" mean do not create a new session

		/* Get the userID of this user that's logged-in (by email) */
		String myEmail = (String)session.getAttribute("email");
		int currUserID = 0; //later set to the userID of the logged in user
		try {
			JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT userID FROM Users WHERE email=?");
			JDBCDriver.ps.setString(1, myEmail);
			JDBCDriver.rs = JDBCDriver.ps.executeQuery();
			if(JDBCDriver.rs.next()) {
				currUserID = JDBCDriver.rs.getInt("userID");
			}
		} catch (SQLException e) {
			System.out.println("SQLException in \"AddAnEvent servlet 2\"");
			e.printStackTrace();
		}
		
		/* Get eventID of the event to add to this user */
		String eventid = request.getParameter("eventid");
		int eventID = Integer.parseInt(eventid);
		
		try {
			JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT * FROM EventTable WHERE eventID=?");
			JDBCDriver.ps.setInt(1, eventID);
			JDBCDriver.rs = JDBCDriver.ps.executeQuery();
			if(JDBCDriver.rs.next()) { //the returned row of event
				String eventName = JDBCDriver.rs.getString("eventName");
				String startDateTime = JDBCDriver.rs.getString("startDateTime");
				String endDateTime = JDBCDriver.rs.getString("endDateTime");
				
				/* Add event to this user's database */
				JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("INSERT INTO EventTable (userID, eventName, startDateTime, endDateTime) VALUES (?, ?, ?, ?)");
				JDBCDriver.ps.setInt(1, currUserID);
				JDBCDriver.ps.setString(2, eventName);
				JDBCDriver.ps.setString(3, startDateTime);
				JDBCDriver.ps.setString(4, endDateTime);
				JDBCDriver.ps.executeUpdate();
				
				
				/* Add event to this user's google calendar */
				Calendar myCalendar = (Calendar)session.getAttribute("calendar");
				
				Event event = new Event().setSummary(eventName);
				
				/* Check for all day events */
				if (startDateTime.length() < 12) {
					DateTime startD = new DateTime(startDateTime);
					EventDateTime start = new EventDateTime()
							.setDate(startD)
						    .setTimeZone("America/Los_Angeles");
					event.setStart(start);
					
					DateTime endD = new DateTime(endDateTime);
					EventDateTime end = new EventDateTime()
							.setDate(endD)
							.setTimeZone("America/Los_Angeles");
					event.setEnd(end);
					
				} else {
					DateTime startDT = new DateTime(startDateTime);
					EventDateTime start = new EventDateTime()
						    .setDateTime(startDT)
						    .setTimeZone("America/Los_Angeles");
					event.setStart(start);
					
					DateTime endDT = new DateTime(endDateTime);
					EventDateTime end = new EventDateTime()
					    .setDateTime(endDT)
					    .setTimeZone("America/Los_Angeles");
					event.setEnd(end);
				}
				
				String calendarId = "primary";
				event = myCalendar.events().insert(calendarId, event).execute();
			}
		} catch (SQLException e) {
			System.out.println("SQLException in \"AddAnEvent servlet 2\"");
			e.printStackTrace();
		}
//		JDBCDriver.close();
	}
}
