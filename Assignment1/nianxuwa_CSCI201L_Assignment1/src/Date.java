
public class Date {
	private String Month;
	private int Day;
	private int Year;

	// Getters
	public String getMonth() {
		return this.Month;
	}

	public int getDay() {
		return this.Day;
	}

	public int getYear() {
		return this.Year;
	}

	// Constructor
	public Date(int m, int d, int y) {
		switch (m) {
		case 1:
			this.Month = "January";
		case 2:
			this.Month = "February";
		case 3:
			this.Month = "March";
		case 4:
			this.Month = "April";
		case 5:
			this.Month = "May";
		case 6:
			this.Month = "June";
		case 7:
			this.Month = "July";
		case 8:
			this.Month = "August";
		case 9:
			this.Month = "September";
		case 10:
			this.Month = "October";
		case 11:
			this.Month = "November";
		case 12:
			this.Month = "December";
		}

		this.Day = d;
		this.Year = y;
	}
}