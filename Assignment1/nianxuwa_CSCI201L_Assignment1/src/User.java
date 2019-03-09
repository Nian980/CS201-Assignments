import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class User {
	private Name Name;
	private ArrayList<Event> Events;

	/*
	 * Comparator to sort user's events in chronological order
	 */
	class sortChronological implements Comparator<Event> {
		// Gives int values to the months for comparison
		private int monthValue(String month) {
			switch (month) {
			case "January":
				return 1;
			case "February":
				return 2;
			case "March":
				return 3;
			case "April":
				return 4;
			case "May":
				return 5;
			case "June":
				return 6;
			case "July":
				return 7;
			case "August":
				return 8;
			case "September":
				return 9;
			case "October":
				return 10;
			case "November":
				return 11;
			case "December":
				return 12;
			}
			return -1; // error
		}

		// Sort in chronological order, earliest event first (ascending order)
		@Override
		public int compare(Event a, Event b) {
			Date aDate = a.getDate();
			Date bDate = b.getDate();
			if (aDate.getYear() < bDate.getYear()) { // if a has earlier year
				return -1; // i.e. a is an earlier event
			} else if (aDate.getYear() > bDate.getYear()) { // if a has later year
				return 1;
			} else { // if they both have same year, compare month
				int aMonth = monthValue(aDate.getMonth());
				int bMonth = monthValue(bDate.getMonth());
				if (aMonth < bMonth) {
					return -1;
				} else if (aMonth > bMonth) {
					return 1;
				} else { // if they have same month, compare day
					if (aDate.getDay() < bDate.getDay()) {
						return -1;
					} else if (aDate.getDay() > bDate.getDay()) {
						return 1;
					} else { // same year, month, and day
						return 0;
					}
				}
			}
		}
	}
	
	//Sort the events of this user
	public void sortEvents() {
		Collections.sort(Events, new sortChronological());
	}
	
	// Getters & Setters
	public Name getName() {
		return Name;
	}

	public void setName(String fname, String lname) {
		this.Name.setFname(fname);
		this.Name.setLname(lname);
	}

	public ArrayList<Event> getEvents() { // return the array of Events for this user
		return this.Events;
	}

	public void addEvent(Event e) {
		Events.add(e);
	}

	public void deleteEvent(int index) {
		Events.remove(index);
	}

	public User() { // Default constructor
		this.Name = new Name();
		this.Events = new ArrayList<Event>(); // Remember to initialize all the member variables!
	}
}
