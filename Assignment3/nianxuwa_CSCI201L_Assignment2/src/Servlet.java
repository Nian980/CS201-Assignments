//From https://developers.google.com/calendar/quickstart/java
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

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
 * Servlet implementation class Servlet
 */
@WebServlet("/Servlet")
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//From: https://developers.google.com/calendar/quickstart/java, Piazza
    private static final String APPLICATION_NAME = "Sycamore Calendar";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";  
    //final means you can't change the value after declaring
    private static NetHttpTransport HTTP_TRANSPORT; //The network HTTP Transport
    private static GoogleAuthorizationCodeFlow flow;
    
    /**
     * Constructor
     */
    public Servlet() {
        super();
        
        //just chuck everything into a try catch block for IOE cuz it keeps giving me errors for that...
        try {
        	InputStream in = Servlet.class.getResourceAsStream(CREDENTIALS_FILE_PATH); //finds a resource with that filepath name
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in)); 
            //the above may throw ioe if the credentials.json file cannot be found
        	
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); //may throw gse
            
			flow = new GoogleAuthorizationCodeFlow.Builder(
					HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
					.setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance())
					.setAccessType("offline")
					.build(); //may throw ioe
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (GeneralSecurityException gse) {
			System.out.println("GeneralSecurityException: " + gse.getMessage());
			gse.printStackTrace();
		}
    }

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Get the access token and google id from frontend
		String googleId = request.getParameter("googleId");
		String accessToken = request.getParameter("accessToken");
		//Get profile image and name
		String fullName = request.getParameter("fullName");
		String firstName = request.getParameter("firstName");
		String imageURL = request.getParameter("imageURL");
		String email = request.getParameter("email");
		
		
		
		//DEBUG
//		System.out.println("googleId: " + googleId);
//		System.out.println("accessToken: " + accessToken);
//		System.out.println("fullName: " + fullName);
//		System.out.println("imageURL: " + imageURL);
//		System.out.println("firstName: " + firstName);
//		System.out.println("email: " + email);
		
		TokenResponse token = new TokenResponse();
		token.setAccessToken(accessToken);
		
		//An authorized Credential object
		Credential myCredential = flow.createAndStoreCredential(token, googleId);
		
		//Now you can build the calender here
		Calendar myCalendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, myCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();

		//getSession - Returns the current session associated with this request, or if request has no session, create one.
		HttpSession session = request.getSession();
		//setAttribute - Binds an object to this session, using the name specified
		session.setAttribute("name", fullName); //bind the fullName object to this session
		session.setAttribute("image", imageURL);
		session.setAttribute("calendar", myCalendar);
		session.setAttribute("credential", myCredential);
		session.setAttribute("email", email);
		
		//forefully try to fix profile issue, calendar null
		session.setAttribute("googleID", googleId);
		session.setAttribute("accessToken", accessToken);
		
		//Store basic info above (except googleId and accessToken) to database
		//Also pass list of events to JDBC to add to database
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = myCalendar.events().list("primary")
                .setMaxResults(30)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
		List<Event> items = events.getItems();
		
		JDBCDriver.addUser(email, fullName, firstName, imageURL, items);
	}
}
