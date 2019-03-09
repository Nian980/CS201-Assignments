import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Database {
	private ArrayList<User> Users;

	/*
	 * Option 1: Shows all users and their events
	 */
	public void displayUserCalender() {
		// For each User, get all the info and display them
		for (int i = 0; i < Users.size(); i++) {
			// Print number
			int index = i + 1;
			System.out.print(index + ") ");
			// Get full name
			Name currentName = Users.get(i).getName();
			System.out.println(currentName.getLname() + ", " + currentName.getFname()); // newline

			//This User's events should already be sorted, as I sort when I read in the file, and when adding a new user
			
			// Get this user's events
			ArrayList<Event> events = Users.get(i).getEvents();

			// Only if events ArrayList is not empty, like there's actually events in it
			if (!events.isEmpty()) {
				// For each Event, get all the info and display them
				for (int j = 0; j < events.size(); j++) {
					System.out.print("\t"); // tab for the events formatting
					// Print number
					int jndex = j + 97;
					char alphaNumber = (char) jndex;
					System.out.print(alphaNumber + ". ");
					// Print event info
					System.out.print(events.get(j).getTitle() + ", ");
					System.out.print(events.get(j).getTime() + ", ");
					Date date = events.get(j).getDate();
					System.out.println(date.getMonth() + " " + date.getDay() + ", " + date.getYear());
					// note: println for the new line at the end
				}
			}
		}
	}

	/* Helper function that shows all the users (names) that are currently in the database */
	public void displayUsers() {
		for (int i = 0; i < Users.size(); i++) {
			// Print number
			int index = i + 1;
			System.out.print(index + ") ");
			// Get full name
			Name currentName = Users.get(i).getName();
			System.out.println(currentName.getLname() + ", " + currentName.getFname()); // newline
		}
	}

	/* Helper function to allow client to select a user based on their listed index */
	public int chooseUserIndex(Scanner scan) {
		int number = 0;
		do {
			while (!scan.hasNextInt()) { // must enter an int
				System.out.println("Invalid, please enter another number.");
				scan.nextLine();
			}
			number = scan.nextInt();
			if (number < 1 || number > Users.size()) { //index must be valid, within the total number of users
				System.out.println("Invalid, please enter another number.");
			}
		} while (number < 1 || number > Users.size());

		return number;
	}

	/*
	 * Option 2: Add a user
	 */
	public void addUser(Scanner scan) {
		String[] arrayOfName;
		Name currName = new Name();
		boolean loop = false;

		scan.nextLine(); // gets rid of most likely the residual newline from up in the menu
		
		do {
			loop = false;
			System.out.println("What is the user’s name?");

			String name = scan.nextLine();
			name = name.trim(); // get rid of leading and trailing whitespace

			// Make sure it's a first and last name
			// E.g. Mary Lane Sue: Fname = Mary, Lname = Lane Sue !
			int wordsEntered = name.split("\\s+").length;
			// regex parameter "\\s+" is regular expression quantifier that matches whitespaces

			while (wordsEntered == 1) {
				System.out.println("Invalid, must have first and last name.");
				System.out.println("What is the user’s name?");
				name = scan.nextLine();
				name = name.trim(); // get rid of leading and trailing whitespace
				wordsEntered = name.split("\\s+").length;
			}

			// Get first and last name
			arrayOfName = name.split("\\s+", 2); // 2 means get 2 parts, so split on first whitespace occurence

			// Check that there isn't already another user with the same name:
			for (User currUser : Users) { // loop through each user
				currName = currUser.getName();

				// Convert to lowercase to compare, as its case insensitive
				// == compares reference (whether they are same object)
				// .equals() compares value equality
				if (currName.getFname().toLowerCase().equals(arrayOfName[0].toLowerCase()) && currName.getLname().toLowerCase().equals(arrayOfName[1].toLowerCase())) {
					System.out.println("There is already another user with the same name.");
					loop = true;
					break;
				}
			}
		} while (loop);

		User newUser = new User();

		newUser.setName(arrayOfName[0], arrayOfName[1]);

		Users.add(newUser); // Add the new user to the database user arraylist
	}

	/*
	 * Option 3: Removes a user
	 */
	public void removeUser(Scanner scan) {
		if (Users.isEmpty()) {
			System.out.println("There are no users currently.");
			return;
		}

		displayUsers(); // Show the users in the db in the order of the current ArrayList
		System.out.println("Who would you like to remove?");

		int index = chooseUserIndex(scan);

		// Remove the user at index number
		Users.remove(index - 1); // because arraylist is 0-based indexing
	}

	/*
	 * Option 4: Adds an event to the chosen user
	 */
	public void addEvent(Scanner scan) {
		if (Users.isEmpty()) {
			System.out.println("There are no users currently.");
			return;
		}
		
		displayUsers();
		System.out.println("To which user would you like to add an event?");

		int index = chooseUserIndex(scan);
		index = index - 1; // 0-based indexing for arraylist

		scan.nextLine(); // gets rid of most likely the residual <enter> command from up in the menu

		System.out.println("What is the title of the event?");
		String title = scan.nextLine();
		System.out.println("What time is the event?");
		String time = scan.nextLine();
		System.out.println("What month?");
		int month = 0;
		do {
			while (!scan.hasNextInt()) { // must enter an int
				System.out.println("Invalid, please enter a number between 1 - 12.");
				scan.nextLine();
			}
			month = scan.nextInt();
			if (month < 1 || month > 12) {
				System.out.println("Invalid, please enter a number between 1 - 12.");
			}
		} while (month < 1 || month > 12);

		System.out.println("What day?");
		int day = 0;
		do {
			while (!scan.hasNextInt()) { // must enter an int
				System.out.println("Invalid, please enter a number between 1 - 31.");
				scan.nextLine();
			}
			day = scan.nextInt();
			if (day < 1 || day > 31) {
				System.out.println("Invalid, please enter a number between 1 - 31.");
			}
		} while (day < 1 || day > 31);

		System.out.println("What year?");
		int year = 0;
		while (!scan.hasNextInt()) { // must enter an int
			System.out.println("Invalid, please enter a number.");
			scan.nextLine();
		}
		year = scan.nextInt();

		// create the date object
		Date newDate = new Date(month, day, year);
		// create the event
		Event newEvent = new Event(title, time, newDate);
		// get the user at index and add an event
		Users.get(index).addEvent(newEvent);
		// sort the events
		Users.get(index).sortEvents();
	}

	/*
	 * Option5: Delete an event from chosen user
	 */
	public void deleteEvent(Scanner scan) {
		if (Users.isEmpty()) {
			System.out.println("There are no users currently.");
			return;
		}
		
		displayUsers();
		System.out.println("From which user would you like to delete an event?");

		int index = chooseUserIndex(scan);
		index = index - 1;

		scan.nextLine();

		ArrayList<Event> userEvents = Users.get(index).getEvents();

		if (userEvents.isEmpty()) {
			System.out.println("The calender is empty");
			return; // leave function to go back to main menu
		}

		// display the available events
		for (int i = 0; i < userEvents.size(); i++) {
			System.out.print((i + 1) + ") ");
			System.out.print(userEvents.get(i).getTitle() + ", ");
			System.out.print(userEvents.get(i).getTime() + ", ");

			Date date = userEvents.get(i).getDate();
			System.out.print(date.getMonth() + " ");
			System.out.print(date.getDay() + ", ");
			System.out.println(date.getYear());
		}

		System.out.print("Which event would you like to delete?");
		int choice = 0;
		do {
			while (!scan.hasNextInt()) { // must enter an int
				System.out.println("Invalid, please enter another number.");
				scan.next();
			}
			choice = scan.nextInt();
			if (choice < 1 || choice > userEvents.size()) {
				System.out.println("Invalid, please enter another number.");
			}
		} while (choice < 1 || choice > userEvents.size());

		// delete that event from the arraylist Events inside this user
		Users.get(index).deleteEvent(choice - 1); // choice-1 cuz 0-based indexing
	}

	/*
	 * Comparator: Sort users ascending by last name
	 */
	class sortAscending implements Comparator<User> {

		@Override
		public int compare(User user1, User user2) {
			Name name1 = user1.getName();
			Name name2 = user2.getName();
			
			//if same last name, compare first names
			if (name1.getLname().compareToIgnoreCase(name2.getLname()) == 0) {
				return name1.getFname().compareToIgnoreCase(name2.getFname());
			}
			//else just compare last name
			return name1.getLname().compareToIgnoreCase(name2.getLname());
		}
	}

	/*
	 * Comparator: Sort users descending by last name
	 */
	class sortDescending implements Comparator<User> {

		@Override
		public int compare(User user1, User user2) {
			Name name1 = user1.getName();
			Name name2 = user2.getName();
			
			if (name2.getLname().compareToIgnoreCase(name1.getLname()) == 0) {
				return name2.getFname().compareToIgnoreCase(name1.getFname());
			}
			return name2.getLname().compareToIgnoreCase(name1.getLname());
		}
	}

	/*
	 * Option 6: Sorts the Users ascending or descending
	 */
	public void sortUsers(Scanner scan) {
		if (Users.isEmpty()) {
			System.out.println("There are no users currently.");
			return;
		}
		
		System.out.println("1) Ascending (A-Z)");
		System.out.println("2) Descending (Z-A)");

		int choice = 0;
		do {
			while (!scan.hasNextInt()) { // must enter an int
				System.out.println("Invalid, please enter 1 or 2.");
				scan.next();
			}
			choice = scan.nextInt();
			if (choice != 1 && choice != 2) { // if it's neither 1 or 2
				System.out.println("Invalid, please enter 1 or 2.");
			}
		} while (choice != 1 && choice != 2);

		if (choice == 1) {
			Collections.sort(Users, new sortAscending());
		} else if (choice == 2) {
			Collections.sort(Users, new sortDescending());
		}
	}
	
	/* Helper function for option 7: sort events for each user, as well as when initially reading in from file */
	public void sortEvents() {
		// For each User, sort their events
		for (int i = 0; i < Users.size(); i++) {
			Users.get(i).sortEvents();
		}
	}
}
