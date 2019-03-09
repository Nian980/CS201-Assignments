package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

import data.Message;
import data.Message.Type;

public class HangmanClient {
	Message message = new Message(Type.UsernameLogin); //initial type
	Scanner scan = new Scanner(System.in);
	
	Message mainMessage; //This stores all useful info up to the point when it is overwritten by multiplayer broadcasts
	//do I even need the above...
	
	//for serialization
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public HangmanClient(String hostname, int port) {
		Socket s = null;
		try {
			System.out.print("Trying to connect to server...");
			s = new Socket(hostname, port);
			System.out.println("Connected!");

			//Serializable objects stuff
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			
			message.setType(Type.UsernameLogin);
			System.out.print("Username: "); //start from this
			
//			mainMessage = message; //set it so that the mainMessage refers to the original message created for this user?
			
			String line = scan.nextLine(); //blocking call?
			message.setAnswerString(line);
			oos.writeObject(message);
			oos.flush(); //send to corresponding HangmanServerThread
			
			run();
		}  catch (IOException ioe) {
			System.out.println("Unable to connect to server " + hostname + " on port " + port + ".");
		}
	}
	
	public void run() {
		//if reading line in run(), then print in main thread, or vice versa if you want
		//in this case, we get message in the run() method, then read line in main thread (the while loop in constructor?)
		try {
			while (true) {
				message = (Message)ois.readObject();
//				System.out.println("DEBUG Inside the run(), just received a message  of type: " + message.getType());
				
				//now interpret the object
				if (message.getType().equals(Type.UserInfo)) {
					System.out.println("Great! You are now logged in as " + message.getUsername() + "!");
					System.out.println("");
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("");
					System.out.println("1) Start a Game");
					System.out.println("2) Join a Game");
					System.out.print("Would you like to start a game or join a game? ");

					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.UsernameLogin)) {
					System.out.print("Username: ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); //send to corresponding HangmanServerThread
				}
				else if (message.getType().equals(Type.PasswordLogin)) {
					System.out.print("Password: ");
					String line = scan.nextLine();
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush();
				}
				else if (message.getType().equals(Type.PasswordNotMatch)) {
					System.out.println("Password does not match the username, please re-enter details");
					message.setType(Type.UsernameLogin);
					System.out.print("Username: ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush();
				}
				else if (message.getType().equals(Type.AccountDoesNotExist)) {
					System.out.println("No account exists with those credentials.");
					System.out.print("Would you like to create a new account? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.PromptUserSameUserPw)) {
					System.out.print("Would you like to use the username and password above? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.InvalidStartJoinChoice)) {
					System.out.println("Please enter 1 to start a game or 2 to join a game: ");
					message.setType(Type.UserInfo); //to go back to the part in serverthread to get 1 or 2
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.StartNewGame)) {
					System.out.print("What is the name of the game? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.GameAlreadyExists)) {
					message.setType(Type.StartNewGame);
					System.out.println(message.getGameName() + " already exists.");
					System.out.print("What is the name of the game? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.JoinGame)) { //multiplayer
					System.out.print("What is the name of the game? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.GameDoesNotExist)) { //multiplayer 
					message.setType(Type.JoinGame);
					System.out.println("There is no game with name " + message.getGameName() + ".");
					System.out.print("What is the name of the game? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.UnableToJoinGame)) { //multiplayer
					message.setType(Type.JoinGame);
					System.out.println("The game " + message.getGameName() + " does not have space for another user to join.");
					System.out.print("What is the name of the game? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.AskHowManyUsers)) {
					System.out.print("How many users will be playing (1-4)? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.InvalidNumOfUsers)) {
					message.setType(Type.AskHowManyUsers); //so it will get user's answer again
					System.out.println("A game can only have between 1-4 players.");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.StartSinglePlayerGame)) {
					message.setType(Type.Option); //Set it because the player will make a decision 1 or 2 next
					System.out.println("All users have joined.");
					System.out.println("Determining secret word...");
					//print out underlines for secret word
					System.out.print("Secret Word ");
					for (int i=0; i<message.getWordLength(); i++) {
						System.out.print("_ ");
					}
					System.out.println(""); //next line
					System.out.print("You have ");
					System.out.print(message.getGuessesRemaining());
					System.out.println(" incorrect guesses remaining.");
					System.out.println("1) Guess a letter.");
					System.out.println("2) Guess the word.");
					System.out.print("What would you like to do? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.WaitingForUsers)) { //multiplayer 
					System.out.println("Waiting for " + message.numOfUsersToJoin + " other user(s) to join...");
					//so if the client hits enter, a message of type WaitingForUsers will be sent back, and it does nothing
					// it just prints again waiting for how many users to join...
				}
				else if (message.getType().equals(Type.JustJoinedOtherPlayersInfo)) { //multiplayer 
					//a user just joined, display relevant user info
					message.displayOtherUserInfo();

					// Note that at this point, the message is replaced with a new empty message, and so no longer 
					// contains all the old info
				}
				else if (message.getType().equals(Type.StartMultiplayerGame)) { //multiplayer 
					System.out.println("All users have joined.");
					System.out.println("Determining secret word...");
					System.out.print("Secret Word ");
					for (int i=0; i<message.getWordLength(); i++) {
						System.out.print("_ ");
					}
					System.out.println(""); //next line
					System.out.print("You have ");
					System.out.print(message.getGuessesRemaining());
					System.out.println(" incorrect guesses remaining.");
				}
				else if (message.getType().equals(Type.MultiplayerTurn)) { //multiplayer, this client's turn
					System.out.println("1) Guess a letter.");
					System.out.println("2) Guess the word.");
					System.out.print("What would you like to do? ");
					message.setType(Type.Option);
					
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush();
				}
				else if (message.getType().equals(Type.WaitForTurn)) {
					System.out.println("Waiting for " + message.currentTurnPlayerName + " to do something...");
				}
				else if (message.getType().equals(Type.InvalidOption)) {
					message.setType(Type.Option);
					System.out.println("That is not a valid option.");
					System.out.println("1) Guess a letter.");
					System.out.println("2) Guess the word.");
					System.out.print("What would you like to do? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush();
				}
				else if (message.getType().equals(Type.GuessLetter)) {
					System.out.print("Letter to guess – "); //change to print later?
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.GuessWord)) {
					System.out.print("What is the secret word? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.NotALetter)) {
					message.setType(Type.GuessLetter);
					System.out.println("That is not a valid letter.");
					System.out.print("Letter to guess – ");  //change to print later?
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush();
				}
				else if (message.getType().equals(Type.LetterGuessCorrect)) {
					message.setType(Type.Option);
					System.out.println("The letter '" + message.getLetterJustGuessed() + "' is in the secret word.");
					System.out.println("Secret word: " + message.getPartialWord());
					System.out.println("You have " + message.getGuessesRemaining() + " incorrect guesses remaining.");
					System.out.println("1) Guess a letter.");
					System.out.println("2) Guess the word.");
					System.out.print("What would you like to do? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.LetterGuessWrong)) {
					message.setType(Type.Option);
					System.out.println("The letter '" + message.getLetterJustGuessed() + "' is not in the secret word.");
					System.out.println("Secret word " + message.getPartialWord());
					System.out.println("You have " + message.getGuessesRemaining() + " incorrect guesses remaining.");
					System.out.println("1) Guess a letter.");
					System.out.println("2) Guess the word.");
					System.out.print("What would you like to do? ");
					String line = scan.nextLine(); 
					message.setAnswerString(line);
					oos.writeObject(message);
					oos.flush(); 
				}
				else if (message.getType().equals(Type.MultiplayerLetterCorrect)) {
					System.out.println("The letter '" + message.getLetterJustGuessed() + "' is in the secret word.");
					System.out.println("Secret word: " + message.getPartialWord());
					System.out.println("You have " + message.getGuessesRemaining() + " incorrect guesses remaining.");
				}
				else if (message.getType().equals(Type.MultiplayerLetterWrong)) {
					System.out.println("The letter '" + message.getLetterJustGuessed() + "' is not in the secret word.");
					System.out.println("Secret word: " + message.getPartialWord());
					System.out.println("You have " + message.getGuessesRemaining() + " incorrect guesses remaining.");
				}
				else if (message.getType().equals(Type.OtherPlayerGuessLetter)) { //multiplayer
					System.out.println(message.currentTurnPlayerName + " has guessed letter '" + message.currentTurnGuess + "'.");
				}
				else if (message.getType().equals(Type.GuessedWrongWord)) { //multiplayer, a user guessed a wrong word and lost
					System.out.println(message.loserName + " has guessed the word '" + message.currentTurnGuess + "'.");
					System.out.println(message.loserName + " has guessed the word incorrectly. They lose!");
				}
				else if (message.getType().equals(Type.NoGuessesLeftSingle)) {
					System.out.println("The letter '" + message.getLetterJustGuessed() + "' is not in the secret word.");
					System.out.println("Secret word " + message.getPartialWord());
					System.out.println("You have " + message.getGuessesRemaining() + " incorrect guesses remaining.");
					System.out.println("You lose!");
					System.out.println("The word was \"" + message.getSecretWord() + "\".");
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("Thank you for playing Hangman!");
					break;
				}
				else if (message.getType().equals(Type.SingleGuessedWrongWord)) { //single player, ends game
					System.out.println("That is incorrect. You lose!");
					System.out.println("The word was \"" + message.getSecretWord() + "\".");
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("Thank you for playing Hangman!");
					break;
				}
				else if (message.getType().equals(Type.SingleGuessedCorrectWord)) { //single player, ends game
					//So if single player, the player guessed the correct word, they win the game
					System.out.println("That is correct! You win!");
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("Thank you for playing Hangman!");
					break;
				}
				else if (message.getType().equals(Type.MultiGuessedCorrectWord)) {
					System.out.println("That is correct! You win!");
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("");
					message.displayOtherUsersRecordOnly();
					System.out.println("Thank you for playing Hangman!");
					break;
				}
				else if (message.getType().equals(Type.MultiGuessedWrongWord)) { //this player guessed wrong word, lost
					System.out.println("That is incorrect! You lose!");
					System.out.println("You are now spectating the game.");
				}
				else if (message.getType().equals(Type.SomeoneCorrectGuessYouLose)) {
					System.out.println(message.winnerName + " has guessed the word '" + message.getSecretWord() + "'.");
					System.out.println(message.winnerName + " has guessed the word correctly. You lose!");
					//own record
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("");
					message.displayOtherUsersRecordOnly(); //others record
					System.out.println("Thank you for playing Hangman!");
					break;
				}
				else if (message.getType().equals(Type.EveryoneLoses)) {
					System.out.println("You have failed to guess the word correctly. You lose!");
					System.out.println("The secret word was '" + message.getSecretWord() + "'.");
					//own record
					System.out.println(message.getUsername() + "'s Record: ");
					System.out.println("--------------");
					System.out.println("Wins - " + message.getWins());
					System.out.println("Losses - " + message.getLosses());
					System.out.println("");
					message.displayOtherUsersRecordOnly(); //others record
					System.out.println("Thank you for playing Hangman!");
					break;
				}
			}
		} catch (IOException ioe) {
			System.out.println("ioe in run: " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in run: " + cnfe.getMessage());
		}
	}
	
	public static void main(String[] args) {
		System.out.print("What is the name of the configuration file? ");
		Scanner scanMain = new Scanner(System.in);
		String configFilename = "";
		
		String ServerHostname = "";
		String ServerPort = "";
		String DBConnection = "";
		String DBUsername = "";
		String DBPassword = "";
		String SecretWordFile = "";
		
		while (true) {
			try {
				configFilename = scanMain.nextLine();
				System.out.println("Reading config file...");
				
				Properties configProps = new Properties(); 
				configProps.load(new FileInputStream(configFilename));
				
				ServerHostname = configProps.getProperty("ServerHostname");
				ServerPort = configProps.getProperty("ServerPort");
				DBConnection = configProps.getProperty("DBConnection");
				DBUsername = configProps.getProperty("DBUsername");
				DBPassword = configProps.getProperty("DBPassword");
				SecretWordFile = configProps.getProperty("SecretWordFile");
				
				boolean missingParam = false;
				
				if (ServerHostname == null) {
					System.out.println("ServerHostname is a required parameter in the configuration file.");
					missingParam = true;
				}
				if (ServerPort == null) {
					System.out.println("ServerPort is a required parameter in the configuration file.");
					missingParam = true;
				}
				if (DBConnection == null) {
					System.out.println("DBConnection is a required parameter in the configuration file.");
					missingParam = true;
				}
				if (DBUsername == null) {
					System.out.println("DBUsername is a required parameter in the configuration file.");
					missingParam = true;
				}
				if (DBPassword == null) {
					System.out.println("DBPassword is a required parameter in the configuration file.");
					missingParam = true;
				}
				if (SecretWordFile == null) {
					System.out.println("SecretWordFile is a required parameter in the configuration file.");
					missingParam = true;
				}
				
				if (missingParam) {
					System.out.print("What is the name of the configuration file?");
					continue;
				}
				else {
					System.out.println("ServerHostName - " + ServerHostname);
					System.out.println("ServerPort - " + ServerPort);
					System.out.println("DBConnection - " + DBConnection);
					System.out.println("DBUsername - " + DBUsername);
					System.out.println("DBPassword - " + DBPassword);
					System.out.println("SecretWordFile - " + SecretWordFile);
				}
				
				break; //successfully read file, break
			} catch (FileNotFoundException fnfe) {
				System.out.println("Configuration file " + configFilename + " could not be found.");
				System.out.print("What is the name of the configuration file?");
				continue;
			} catch (IOException ioe) {
				System.out.println("Configuration file " + configFilename + " could not be found.");
				System.out.print("What is the name of the configuration file?");
				continue;
			}
		}
//		scanMain.close();
		
		new HangmanClient(ServerHostname, Integer.parseInt(ServerPort));
	}

}
