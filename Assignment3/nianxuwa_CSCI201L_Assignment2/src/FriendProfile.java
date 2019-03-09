

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class FriendProfile
 */
@WebServlet("/FriendProfile")
public class FriendProfile extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FriendProfile() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JDBCDriver.connect();
		
		/* Get friend's info */		
		String friendid = request.getParameter("friend");
		int friendID = Integer.parseInt(friendid); //ID of who's profile you're on right now
		String friendFullName = "";
		String friendFirstName = "";
		String friendImg = "";
		//get this friend's row from the Users table.
		try {
			JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT * FROM Users WHERE userID=?");
			JDBCDriver.ps.setInt(1, friendID);
			JDBCDriver.rs = JDBCDriver.ps.executeQuery();
			if(JDBCDriver.rs.next()) { //first row
//				System.out.println("FriendProfile Servlet 1, inside the next()");
				friendFullName = JDBCDriver.rs.getString("fullname");
				friendFirstName = JDBCDriver.rs.getString("firstname");
				friendImg = JDBCDriver.rs.getString("imglink");
//				System.out.println("fullname = " + friendFullName);
//				System.out.println("firstname = " + friendFirstName);
//				System.out.println("imglink = " + friendImg);
			} 
		} catch (SQLException e) {
			System.out.println("SQLException in \"FriendProfile servlet 1\"");
			e.printStackTrace();
		}
		
		/* Get the userID of this user that's logged-in (by email) */
		HttpSession session = request.getSession(false); //"false" mean do not create a new session
		String myEmail = (String)session.getAttribute("email");
//		System.out.print("email of loggedin user: " + myEmail);
		int currUserID = 0; //later set to the userID of the logged in user
		try {
			JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT userID FROM Users WHERE email=?");
			JDBCDriver.ps.setString(1, myEmail);
			JDBCDriver.rs = JDBCDriver.ps.executeQuery();
			if(JDBCDriver.rs.next()) {
				currUserID = JDBCDriver.rs.getInt("userID");
			}
		} catch (SQLException e) {
			System.out.println("SQLException in \"FriendProfile servlet 2\"");
			e.printStackTrace();
		}
		
		/* Determine whether user if following this friend */		
		boolean following = false;
		
		/* prototype: get follow/unfollow command */
		String followCommand = request.getParameter("follow");
		//0 means initial load page, determine through database
		//1 means we follow this user, add to Follow table
		//2 means we unfollow this user, delete from Follow table
		if (followCommand.equals("0")) {
			try {
				//Check follow table whether currUserID follows friendid
				JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT * FROM Follow WHERE userID=? AND friendID=?");
				JDBCDriver.ps.setInt(1, currUserID);
				JDBCDriver.ps.setInt(2, friendID);
				JDBCDriver.rs = JDBCDriver.ps.executeQuery();
				if (!JDBCDriver.rs.isBeforeFirst()) { //if nothing returned, then not following
					following = false;
				} else { //else if something is returned, then is following
					following = true;
				}
			} catch (SQLException e) {
				System.out.println("SQLException in \"FriendProfile servlet 3\"");
				e.printStackTrace();
			}
		} else if (followCommand.equals("1")) {
			following = true;
			/* Add a row to Follow table of (currUserID, friendID) */
			try {
				JDBCDriver.ps = JDBCDriver.conn.prepareStatement("INSERT INTO Follow (userID, friendID) VALUES (?, ?)");
				JDBCDriver.ps.setInt(1, currUserID);
				JDBCDriver.ps.setInt(2, friendID);
				JDBCDriver.ps.executeUpdate();
			} catch (SQLException e) {
				System.out.println("SQLException in \"FollowFriend part 1\"");
				e.printStackTrace();
			}
		} else { //followCommand == 2
			/* Delete a row to Follow table of (currUserID, friendID) */
			try {
				JDBCDriver.ps = JDBCDriver.conn.prepareStatement("DELETE FROM Follow WHERE userID=? AND friendID=?");
				JDBCDriver.ps.setInt(1, currUserID);
				JDBCDriver.ps.setInt(2, friendID);
				JDBCDriver.ps.executeUpdate();
			} catch (SQLException e) {
				System.out.println("SQLException in \"UnfollowFriend part 1\"");
				e.printStackTrace();
			}
		}
		
		/* Response printing out stuff below */
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();		
		
		/* These are a few things that must be printed regardless of following or not */
		if (following) { //if following, Unfollow button should be displayed, and Follow button shown
			out.println("<button type=\"button\" class=\"follow-button\" id=\"unfollow\" onclick=\"toggleButtons()\">Unfollow</button>");
			out.println("<button type=\"button\" class=\"follow-button\" id=\"follow\" onclick=\"toggleButtons()\" style=\"display: none;\">Follow</button>");
		} else {
			out.println("<button type=\"button\" class=\"follow-button\" id=\"unfollow\" onclick=\"toggleButtons()\" style=\"display: none;\">Unfollow</button>");
			out.println("<button type=\"button\" class=\"follow-button\" id=\"follow\" onclick=\"toggleButtons()\">Follow</button>");
		}
		out.println("<p class=\"title\">" + friendFirstName + "\'s Upcoming Events</p>");
		
		out.println("<div class=\"table-wrapper\" id=\"table-wrapper\">");
		out.println("<table class=\"event-table\">");
		out.println("<tr>");
		out.println("<th>Date</th>");
		out.println("<th>Time</th>");
		out.println("<th>Event Summary</th>");
		out.println("</tr>");

		/* Events should be printed here if we're following them */
		if (following) {
			//use jdbc to get all events associated with friend
			try {
				JDBCDriver.ps = JDBCDriver.conn.prepareStatement("SELECT * FROM EventTable WHERE userID=" + friendID);
				JDBCDriver.rs = JDBCDriver.ps.executeQuery();
				
				//for each row of events
				while (JDBCDriver.rs.next()) {
//					System.out.println("FriendProfile Servlet 4, inside the next()");
					int eventID = JDBCDriver.rs.getInt("eventID");
					String summary = JDBCDriver.rs.getString("eventName");
					String startDateTime = JDBCDriver.rs.getString("startDateTime");
					
//					System.out.println("Doing event: " + summary); //DEBUG
//					System.out.println("startdatetime: " + startDateTime);
					
					boolean isAllDayEvent = false;
					if (startDateTime.length() < 12) { //all day events only have data, 10 characters long
						isAllDayEvent = true;
					}
					//note: endDateTime should be a date only for all day events
					
					//keep track of the the start date/time for display
					String startYear = "";
					String startMonth = "";
					String startDay = "";
					String startDisplayTime = "";	
					
					//parse the start date 
					String[] startDateThenTime = startDateTime.split("T");
					String startDate = startDateThenTime[0];
					String startTime = "";
					
					//If not all day event, then get time
					if (!isAllDayEvent) {
						startTime = startDateThenTime[1];
					}
					
					String[] startSplitDates = startDate.split("-");
					startYear = startSplitDates[0];
					startMonth = startSplitDates[1];
					startDay = startSplitDates[2];
					
					if (startMonth.equals("01")) {
	                	startMonth = "January";
	                } else if (startMonth.equals("02")) {
	                	startMonth = "February";
	                } else if (startMonth.equals("03")) {
	                	startMonth = "March";
	                } else if (startMonth.equals("04")) {
	                	startMonth = "April";
	                } else if (startMonth.equals("05")) {
	                	startMonth = "May";
	                } else if (startMonth.equals("06")) {
	                	startMonth = "June";
	                } else if (startMonth.equals("07")) {
	                	startMonth = "July";
	                } else if (startMonth.equals("08")) {
	                	startMonth = "August";
	                } else if (startMonth.equals("09")) {
	                	startMonth = "September";
	                } else if (startMonth.equals("10")) {
	                	startMonth = "October";
	                } else if (startMonth.equals("11")) {
	                	startMonth = "November";
	                } else if (startMonth.equals("12")) {
	                	startMonth = "December";
	                }
					
					//parse the start time
					if (isAllDayEvent) {
						startDisplayTime = "All Day";
					} else {
						String[] startSplitTimes = startTime.split(":");
						String startHour = startSplitTimes[0];
						String startMinute = startSplitTimes[1];
						int startHourInt = Integer.parseInt(startHour);
						if (startHourInt > 12) {
							startHourInt -= 12;
							startDisplayTime = Integer.toString(startHourInt) + ":" + startMinute + " PM";
						} else if (startHourInt == 12) {
							startDisplayTime = startHour + ":" + startMinute + " PM";
						} else {
							startDisplayTime = startHour + ":" + startMinute + " AM";
						}
					}
					
					String finalStartDate = startMonth + " " + startDay + ", " + startYear;
					
					//Now print events out to the JSP page
					out.println("<tr id=\"" + eventID + "\" onclick=\"clickedEvent(this.id, this)\">");
					out.println("<td>" + finalStartDate + "</td>");
					out.println("<td>" + startDisplayTime + "</td>");
					out.println("<td>" + summary + "</td>");
					out.println("</tr>");					
				}
			} catch (SQLException e) {
				System.out.println("SQLException in \"FriendProfile servlet 4\"");
				e.printStackTrace();
			}
		}
		
		/* Need to end table here */
		out.println("</table>");

		/* This part if for is we're not following the user */		
		if (!following) {
			out.println("<p class=\"follow-prompt\">Follow " + friendFirstName + " to view Upcoming Events</p>");
		}
		
		/* These are a few things that must be printed at end */
		out.println("</div>");
		out.println("<div class=\"user\">");
		out.println("<img class=\"profile-pic\" src=\"" + friendImg + "\" alt=\"User Image\" width=\"250px\" height=\"250px\">");
		out.println("<p class=\"profile-name-class\">" + friendFullName + "</p>");
		out.println("</div>");
		
//		JDBCDriver.close();
	}
}
