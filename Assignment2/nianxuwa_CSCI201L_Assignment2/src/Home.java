
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Home
 */
@WebServlet("/Home")
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Home() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

//		Do error checking here, and if there is error, redirect to AddEventFail.jsp. Save variables and errorMsg
//		in session. If no error, go to AddEventSuccess.jsp
		
		String eventTitle = request.getParameter("eventTitle");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		String startTime = request.getParameter("startTime");
		String endTime = request.getParameter("endTime");
		//note: time is given in 24hr clock 0-23 hours, 0-59 min
		
		//DEBUG
		System.out.println("eventTitle = " + eventTitle);
		System.out.println("startDate = " + startDate);
		System.out.println("endDate = " + endDate);
		System.out.println("startTime = " + startTime);
		System.out.println("endTime = " + endTime);

		//set them into session so that you get them elsewhere
		HttpSession session = request.getSession(false);
		session.setAttribute("eventTitle", eventTitle);
		session.setAttribute("startDate", startDate);
		session.setAttribute("endDate", endDate);
		session.setAttribute("startTime", startTime);
		session.setAttribute("endTime", endTime);
		
		response.setContentType("text/html");
		
		String nextPage = ""; //the page to redirect to whether success or fail
		boolean hasError = false;
		String errorMsg = "";
		
		if (eventTitle.equals("") || eventTitle.equals("Event Title")) {
			errorMsg += "Event Title cannot be empty. ";
			hasError = true;
			nextPage = "Home.jsp";
		}
		if (startDate.equals("") || startDate.equals("Start Date")) {
			errorMsg += "Start Date cannot be empty. ";
			hasError = true;
			nextPage = "Home.jsp";
		}
		if (endDate.equals("") || endDate.equals("End Date")) {
			errorMsg += "End Date cannot be empty. ";
			hasError = true;
			nextPage = "Home.jsp";
		}
		if (startTime.equals("") || startTime.equals("Start Time")) {
			errorMsg += "Start Time cannot be empty. ";
			hasError = true;
			nextPage = "Home.jsp";
		}
		if (endTime.equals("") || endTime.equals("End Time")) {
			errorMsg += "End Time cannot be empty. ";
			hasError = true;
			nextPage = "Home.jsp";
		}

		/*if all of start and end date, start and end time entered and they are not empty,
			check if end date is before start date (and end time is not before start time) */
		if (!startDate.equals("") && !startDate.equals("Start Date") && !endDate.equals("")
				&& !endDate.equals("End Date") && !startTime.equals("") && !startTime.equals("Start Time")
				&& !endTime.equals("") && !endTime.equals("End Time")) {
			errorMsg += "<br>"; // put a break first to next line

			String[] startDateArray = startDate.split("-");
			String[] endDateArray = endDate.split("-");
			int startYear = Integer.parseInt(startDateArray[0]);
			int endYear = Integer.parseInt(endDateArray[0]);
			int startMonth = Integer.parseInt(startDateArray[1]);
			int endMonth = Integer.parseInt(endDateArray[1]);
			int startDay = Integer.parseInt(startDateArray[2]);
			int endDay = Integer.parseInt(endDateArray[2]);

			String[] startTimeArray = startTime.split(":");
			String[] endTimeArray = endTime.split(":");
			int startHour = Integer.parseInt(startTimeArray[0]);
			int endHour = Integer.parseInt(endTimeArray[0]);
			int startMinute = Integer.parseInt(startTimeArray[1]);
			int endMinute = Integer.parseInt(endTimeArray[1]);
			
			if (endYear < startYear) { // if end year is earlier than start year
				errorMsg += "Ending year cannot be before starting year";
				hasError = true;
				nextPage = "Home.jsp";
			} else if (endYear == startYear) { // if in same year, check month
				if (endMonth < startMonth) { // if end month earlier than start month
					errorMsg += "Ending month cannot be before starting month";
					hasError = true;
					nextPage = "Home.jsp";
				} else if (endMonth == startMonth) { // if in same month (and year), check day
					if (endDay < startDay) { // if end day earlier than start day
						errorMsg += "Ending day cannot be before starting day";
						hasError = true;
						nextPage = "Home.jsp";
					} else if (endDay == startDay) { //if in same day (and month and year), check time
						if (endHour < startHour) { //if ending hour is earlier than starting hour
							errorMsg += "Ending hour cannot be before starting hour";
							hasError = true;
							nextPage = "Home.jsp";
						} else if (endHour == startHour) { //if on same hour
							if (endMinute < startMinute) {
								errorMsg += "Ending minute cannot be before starting minute";
								hasError = true;
								nextPage = "Home.jsp";
							}
						}
					}
				}
			}
		}
		
		//store the error messages above into some session, so you can get them elsewhere if needed
		session.setAttribute("errorMsg", errorMsg);
		
		//this sets the errorMsg variable in Home.jsp to the new errorMsg
		request.setAttribute("errorMsg", errorMsg);
		
		if (!hasError) { //if no errors
			nextPage = "AddEventSuccess.jsp";
		}
		
		//yes this finally kinda works! g'damn it.
		RequestDispatcher dispatch = request.getRequestDispatcher(nextPage);
		dispatch.forward(request, response);
	}

}
