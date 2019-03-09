
// Nianxu Wang
// 9333790525

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class CalenderDatabase {
	/*
	 * use this Scanner in all the other code, don't create another one, as opening
	 * multiple scanner input streams and closing them will affect and cut off the
	 * underlying input stream
	 */
	public static final Scanner scan = new Scanner(System.in);

	/*
	 * Option 7: This function is referenced from:
	 * https://www.mkyong.com/java/how-to-write-to-file-in-java-bufferedwriter-
	 * example/
	 */
	private static void writeFile(Database db, String filename) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String jsonOutput = gson.toJson(db);

		BufferedWriter bWriter = null;
		FileWriter fWriter = null;

		try {
			//Sort the events of each User before writing to file
			db.sortEvents();
			
			fWriter = new FileWriter(filename);
			bWriter = new BufferedWriter(fWriter);
			bWriter.write(jsonOutput);

			System.out.println("File has been saved.");

		} catch (IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
			System.out.println("Could not write file");
		} finally {
			try {
				if (bWriter != null)
					bWriter.close();
				if (fWriter != null)
					fWriter.close();
			} catch (IOException ioe) {
				System.out.println("ioe: " + ioe.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Use GsonBuilder to format any json created
		Database db = new Database();
		String filename = "";

		/* Continuously ask for file until it successfully parses */
		boolean askForFile = true;
		while (askForFile) {
			System.out.print("What is the name of the input file? ");
			filename = scan.nextLine();

			// Uses stringbuilder to create the json string line by line
			StringBuilder sb = new StringBuilder();
			try {
				// Setup for reading the input file line by line
				FileReader fr = new FileReader(filename); // may throw FileNotFoundError
				BufferedReader br = new BufferedReader(fr);

				String line = br.readLine(); // may throw an IOException
				while (line != null) { // While not eof
					line = line.trim(); // get rid of leading and trailing whitespace
					sb.append(line); // add it to stringbuilder
					line = br.readLine(); // read another line
				}
				fr.close();
				br.close();
			} catch (FileNotFoundException fnfe) {
				System.out.println("That file could not be found.");
				continue; // Go for another while loop and ask for file again
			} catch (IOException ioe) {
				System.out.println("ioe: " + ioe.getMessage());
				continue;
			}

			String jsonString = sb.toString();
			
			//if empty json file is passed in
			if (jsonString.isEmpty()) {
				System.out.println("Empty json file.");
				continue;
			}

			/* Deserialize JSON file to a CalenderDatabase object */
			try {
				db = gson.fromJson(jsonString, Database.class); // May throw JsonParseException
				askForFile = false; // Successfully parsed file, can get out of while loop
			} catch (JsonParseException jpe) {
				System.out.println("JsonParseException: " + jpe.getMessage()); // descriptive error message
				System.out.println("That file is not a well-formed JSON file.");
				continue;
			} 

			//After reading in file, sort the events for each user
			db.sortEvents();
		}

		//Detect whether changes have been made since the file was last saved
		boolean changes = false;
		
		/* Querying part */
		while (true) {
			// Create menu options
			System.out.println("1) Display User’s Calendar");
			System.out.println("2) Add User");
			System.out.println("3) Remove User");
			System.out.println("4) Add Event");
			System.out.println("5) Delete Event");
			System.out.println("6) Sort Users");
			System.out.println("7) Write File");
			System.out.println("8) Exit");
			System.out.println("What would you like to do?");

			while (!scan.hasNextInt()) {
				System.out.println("That is not a valid option . not within range of 1-8");
				scan.nextLine(); // if you used scan.next(), and you entered 2 words, then it will loop twice...
				System.out.println("1) Display User’s Calendar");
				System.out.println("2) Add User");
				System.out.println("3) Remove User");
				System.out.println("4) Add Event");
				System.out.println("5) Delete Event");
				System.out.println("6) Sort Users");
				System.out.println("7) Write File");
				System.out.println("8) Exit");
				System.out.println("What would you like to do?");
			}
			int userChoice = scan.nextInt();

			if (userChoice == 1) {
				db.displayUserCalender();
			} else if (userChoice == 2) {
				db.addUser(scan);
				changes = true;
			} else if (userChoice == 3) {
				db.removeUser(scan);
				changes = true;
			} else if (userChoice == 4) {
				db.addEvent(scan);
				changes = true;
			} else if (userChoice == 5) {
				db.deleteEvent(scan);
				changes = true;
			} else if (userChoice == 6) {
				db.sortUsers(scan);
				changes = true;
			} else if (userChoice == 7) {
				writeFile(db, filename);
				changes = false; //since we saved the file already
			} else if (userChoice == 8) {
				if (changes) {
					System.out.println("Changes have been made since the file was last saved.");
					System.out.println("1) Yes");
					System.out.println("2) No");
					System.out.println("Would you like to save the file before exiting?");
					int choice = 0;
					do {
						while (!scan.hasNextInt()) { // must enter an int
							System.out.println("Please choose between 1 and 2");
							scan.next();
						}
						choice = scan.nextInt();
						if (choice != 1 && choice != 2) {
							System.out.println("Please choose between 1 and 2");
						}
					} while (choice != 1 && choice != 2);
					
					if (choice == 1) {
						writeFile(db, filename);
					}
					else if (choice == 2) {
						System.out.println("File has not been saved.");
						System.out.println("Thank you for using my program!");
					}
				}
				else { //if no changes done (just saved file or only used option 1
					System.out.println("Thank you for using my program!");
				}
				break; //get out of while loop exit at the end whether we save or not
			} else { // if not within range of 1-8
				System.out.println("That is not a valid option . not within range of 1-8");
				continue; // redo the choice, back to top of while loop
			}
		}
	}
}
