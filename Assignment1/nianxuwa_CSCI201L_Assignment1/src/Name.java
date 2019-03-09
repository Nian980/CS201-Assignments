
public class Name {
	private String Fname;
	private String Lname;

	// Getters and Setters
	public String getFname() {
		return this.Fname;
	}

	public void setFname(String fname) {
		this.Fname = fname;
	}

	public String getLname() {
		return this.Lname;
	}

	public void setLname(String lname) {
		this.Lname = lname;
	}

	// Constructors
	public Name() { // default constructor
		this.Fname = "";
		this.Lname = "";
	}

	public Name(String fname, String lname) {
		this.Fname = fname;
		this.Lname = lname;
	}
}
