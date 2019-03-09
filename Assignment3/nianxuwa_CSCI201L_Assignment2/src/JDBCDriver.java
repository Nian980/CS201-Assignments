
import java.sql.*;
import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

public class JDBCDriver {
	public static Connection conn = null;
	public static ResultSet rs = null;
	public static PreparedStatement ps = null;
	private static String connectionPath = "jdbc:mysql://localhost:3306/nianxuwa_Database?user=root&password=Wilsonblade98&useSSL=false";
	
	public static void connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(connectionPath);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void close(){
		try{
			if (rs!=null){
				rs.close();
				rs = null;
			}
			if(conn != null){
				conn.close();
				conn = null;
			}
			if(ps != null ){
				ps = null;
			}
		}catch(SQLException sqle){
			System.out.println("connection close error");
			sqle.printStackTrace();
		}
	}
	
	public static void addUser(String email, String fullName, String firstName, String imgUrl, List<Event> eventItems){
		connect();
		try {
			ps = conn.prepareStatement("SELECT userID FROM Users WHERE email=?;");
			ps.setString(1, email); //set the first ? to the email value
			rs = ps.executeQuery();
			
			int userID = 0;
			
			//System.out.println("What's inside resultSet email: " + rs); //DEBUG, pring out the email
			
			if(rs.next()){ 
				//if not null (hence true), then user is already in DB, delete their events (and re-add later below)
				userID = rs.getInt("userID");
				deleteUserEvents(userID);
			} else { 
				//if ResultSet is null (hence false), so user is not already in DB, add the user, (and add their events below)
				ps = conn.prepareStatement("INSERT INTO Users (email, fullname, firstname, imglink) VALUES (?, ?, ?, ?);");
				ps.setString(1, email);
				ps.setString(2, fullName);
				ps.setString(3, firstName);
				ps.setString(4, imgUrl);
				ps.executeUpdate(); //use executeUpdate() for insert statements
			}
			
			//now add the user's events into DB
			ps = conn.prepareStatement("SELECT userID FROM Users WHERE email=?;");
			ps.setString(1, email);
			rs = ps.executeQuery();
			rs.next();
			userID = rs.getInt("userID");
			
			//now I have the userID and the list of events to add to this user's database
			addUserEvents(userID, eventItems);
			
		} catch (SQLException e) {
			System.out.println("SQLException in function \"addUser\"");
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	/* Delete all events associated with this user */
	public static void deleteUserEvents(int userID) {
		//may not need to connect again? as the function addUser calls this with connect already?
//		System.out.println("inside deleteUserEvents function");
		try {
			ps = conn.prepareStatement("DELETE FROM EventTable WHERE userID=?;");
			ps.setInt(1, userID);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQLException in function \"deleteUserEvents\"");
			e.printStackTrace();
		}
	}
	
	/* Add all the events for this user */
	public static void addUserEvents(int userID, List<Event> eventItems) {
		//get a list of the user's events
//		System.out.println("inside addAllEvents function");
		
		//need to store eventName and eventDateTime
		for (Event event : eventItems) {
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            if (start == null) { //if its an all-day event
                start = event.getStart().getDate(); //just get Date
            }
            if (end == null) {
            	end = event.getEnd().getDate();
            }

            String eventSummary = event.getSummary();
            String startDateTime = start.toString();
            String endDateTime = end.toString();
            
//            System.out.println("adding event: " + eventSummary);
            
            //add this event to database
            try {
            	ps = conn.prepareStatement("INSERT INTO EventTable (userID, eventName, startDateTime, endDateTime) VALUES (?, ?, ?, ?);");
            	ps.setInt(1, userID);
            	ps.setString(2, eventSummary);
            	ps.setString(3, startDateTime);
            	ps.setString(4, endDateTime);
            	ps.executeUpdate();
            } catch (SQLException e) {
    			System.out.println("SQLException in function \"deleteUserEvents\"");
    			e.printStackTrace();
    		}
            
            //Note: Store the event date times as is. Parse them in front end later if needed.
            //If the startDateTime/endDateTime is only a date, then it's an all-day event
		}
		
	}
	
}
