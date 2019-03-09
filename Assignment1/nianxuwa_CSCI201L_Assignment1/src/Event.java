
public class Event {
	private String Title;
	private String Time; // should this be a string? or another class?
	private Date Date;

	// Getters & Setters
	public String getTitle() {
		return this.Title;
	}

	public void setTitle(String title) {
		this.Title = title;
	}

	public String getTime() {
		return this.Time;
	}

	public void setTime(String time) {
		this.Time = time;
	}

	public Date getDate() {
		return this.Date;
	}

	// Constructor (make sure to create a new date to add to here!)
	public Event(String title, String time, Date d) {
		this.Title = title;
		this.Time = time;
		this.Date = d;
	}
}

