

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class HomeFollowers
 */
@WebServlet("/HomeFollowers")
public class HomeFollowers extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HomeFollowers() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* Get the userID of this user that's logged-in (by email) */
		HttpSession session = request.getSession(false); //"false" mean do not create a new session
		String myEmail = (String)session.getAttribute("email");
		int currUserID = 0; //later set to the userID of the logged in user
		JDBCDriver.connect();
		try {
			JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT userID FROM Users WHERE email=?");
			JDBCDriver.ps.setString(1, myEmail);
			JDBCDriver.rs = JDBCDriver.ps.executeQuery();
			if(JDBCDriver.rs.next()) {
				currUserID = JDBCDriver.rs.getInt("userID");
			}
		} catch (SQLException e) {
			System.out.println("SQLException in \"HomeFollowers Servlet\"");
			e.printStackTrace();
		}
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();	
		
		/* Get the userIDs of people this user is following */
		try {
			JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT friendID FROM Follow WHERE userID=?");
			JDBCDriver.ps.setInt(1, currUserID);
			JDBCDriver.rs = JDBCDriver.ps.executeQuery();
			//Now rs should be a column of userIDs that are the friends that this user follows
			
			//Store all the friendIDs here
			ArrayList<Integer> friendIDList = new ArrayList<Integer>();
			while (JDBCDriver.rs.next()) {
				friendIDList.add(JDBCDriver.rs.getInt("friendID"));
			}
			
			for (int i=0; i<friendIDList.size(); i++) { //for each following user's ID, get their user record
				int currFriendID = friendIDList.get(i);

				JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT * FROM Users WHERE userID=?");
				JDBCDriver.ps.setInt(1, currFriendID);
				JDBCDriver.rs = JDBCDriver.ps.executeQuery();
				//now rs should be right before a row of all the user data of currFriendID
				
				if (JDBCDriver.rs.next()) {
					String friendFullName = JDBCDriver.rs.getString("fullname");
					String friendImg = JDBCDriver.rs.getString("imglink");
					
					out.println("<div class=\"user\">");
					out.print("<a href=\"FriendProfile.jsp?userid=" + Integer.toString(currFriendID));
					out.print("\"><img class=\"user-pic\" src=\"" + friendImg);
					out.println("\" alt=\"user image\" width=\"150px\" height=\"150px\"></a>");
					out.print("<a href=\"FriendProfile.jsp?userid=" + Integer.toString(currFriendID));
					out.print("\" class=\"user-name-class\">");
					out.print(friendFullName);
					out.println("</a>");
					out.println("</div>");
				}
			}
		} catch (SQLException e) {
			System.out.println("SQLException in \"HomeFollowers Servlet\"");
			e.printStackTrace();
		}
		
//		JDBCDriver.close();
	}
}
