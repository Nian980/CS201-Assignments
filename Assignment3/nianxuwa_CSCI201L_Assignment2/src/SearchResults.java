

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
 * Servlet implementation class SearchResults
 */
@WebServlet("/SearchResults")
public class SearchResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchResults() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		String query = request.getParameter("query");
		query = query.trim(); //get rid of leading/trailing whitespace
		query = query.toLowerCase();
//		System.out.println(query); //DEBUG
		
		HttpSession session = request.getSession(false); //"false" mean do not create a new session
		String myEmail = (String)session.getAttribute("email");
//		System.out.println("current user's email"); //DEBUG
//		System.out.println(myEmail); //DEBUG
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		JDBCDriver.connect();

		if (query == "") { //return all users in database
//			System.out.println("Empty query"); //DEBUG

			try {
				JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT * FROM Users");
				JDBCDriver.rs = JDBCDriver.ps.executeQuery();
				
				if (!JDBCDriver.rs.isBeforeFirst()) { //if empty result set
				    System.out.println("No users (SearchResult Servlet)");
				    out.println("<div class=\"no-users-found\">");
				    out.println("<p>No Users Found</p>");
				    out.println("</div>");
				} else { //else if there are user rows returned
					while(JDBCDriver.rs.next()) {
						//the right string must match name of column exactly
						int userID = JDBCDriver.rs.getInt("userID");
						String fullname = JDBCDriver.rs.getString("fullname");
						String imgurl = JDBCDriver.rs.getString("imglink");
						String email = JDBCDriver.rs.getString("email");
						
						//don't display self in search results
						if (myEmail.equals(email)) {
							continue;
						}
						
						out.println("<div class=\"user\">");
						
						out.print("<a href=\"FriendProfile.jsp?userid=" + Integer.toString(userID));
						out.print("\"><img class=\"user-pic\" src=\"" + imgurl);
						out.println("\" alt=\"user image\" width=\"150px\" height=\"150px\"></a>");
						
						out.print("<a href=\"FriendProfile.jsp?userid=" + Integer.toString(userID));
						out.print("\" class=\"user-name-class\">");
						out.print(fullname);
						out.println("</a>");
						
						out.println("</div>");
					}
				}
			} catch (SQLException e) {
				System.out.println("SQLException in \"SearchResults\"");
				e.printStackTrace();
			}
		} else { //do a query search of the search keywords
			String[] keywords = query.split("\\s+");
			boolean noUsersFound = true; //whether the query returns any users
			
			//DEBUG
//			System.out.println("Keywords: ");
//			for (int i=0; i<keywords.length; i++) {
//				System.out.println(keywords[i]);
//			}

			try {
				JDBCDriver.ps =  JDBCDriver.conn.prepareStatement("SELECT * FROM Users");
				JDBCDriver.rs = JDBCDriver.ps.executeQuery();
				
				while(JDBCDriver.rs.next()) {
					int userID = JDBCDriver.rs.getInt("userID");
					String fullname = JDBCDriver.rs.getString("fullname");
					String fullnameLower = fullname.toLowerCase();
					String imgurl = JDBCDriver.rs.getString("imglink");
					String email = JDBCDriver.rs.getString("email");
					
					//don't display self in search results
					if (myEmail.equals(email)) {
						continue;
					}
					
					//since space is logical OR, if ANY search word is contained in the full name
					for (int i=0; i<keywords.length; i++) { 
						if (fullnameLower.contains(keywords[i])) { //if search word is in the name
							noUsersFound = false;
							
							//print this user out					
							out.println("<div class=\"user\">");
							
							out.print("<a href=\"FriendProfile.jsp?userid=" + Integer.toString(userID));
							out.print("\"><img class=\"user-pic\" src=\"" + imgurl);
							out.println("\" alt=\"user image\" width=\"150px\" height=\"150px\"></a>");
							
							out.print("<a href=\"FriendProfile.jsp?userid=" + Integer.toString(userID));
							out.print("\" class=\"user-name-class\">");
							out.print(fullname);
							out.println("</a>");
							
							out.println("</div>");
							
							break; //get out of for loop, move onto next user record
						}
					}
				}
				
				if (noUsersFound) {
					System.out.println("No users (SearchResult Servlet)");
				    out.println("<div class=\"no-users-found\">");
				    out.println("<p>No Users Found</p>");
				    out.println("</div>");
				}
				
			} catch (SQLException e) {
				System.out.println("SQLException in \"SearchResults\"");
				e.printStackTrace();
			}
		}
//		JDBCDriver.close();
	}
}
