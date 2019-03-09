package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import data.Message;
import data.Message.Type;
import server.Game.GameState;

public class HangmanServer {
	//JDBC Stuff
	public static Connection conn = null;
	public static ResultSet rs = null;
	public static PreparedStatement ps = null;
	private static String connectionPath = "";
	
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
	
	//All the serverThreads, one for each client
	private Vector<HangmanServerThread> serverThreads;
	
	//Hashmap of gameName to the Game object
	private Map<String,Game> gamesMap = Collections.synchronizedMap(new HashMap<String,Game>()); 
	
	//Secret word list (from the file)
	private static ArrayList<String> wordList = new ArrayList<String>();
	
	//Hashmap from user's name to their serverthread? For multiplayer
	
	//constructor connects to port
	public HangmanServer(int port) {
		ServerSocket ss = null;
		
		try {
			ss = new ServerSocket(port);
			//System.out.println("DEBUG: HangmanServer bound to port " + port); //DEBUG
			
			serverThreads = new Vector<HangmanServerThread>();
			
			while(true) {
				//This only runs when a new player connects (Blocking call)
				Socket s = ss.accept();
				//System.out.println("DEBUG: Connection from " + s.getInetAddress()); //DEBUG
				
				//Each new HangmanClient is put into a socket, and the HST created below will be associated with that client
				
				//have one hst for every client that connects to out server
				HangmanServerThread hst = new HangmanServerThread(s, this); //will be similar to our chat program
				serverThreads.add(hst);
			}
		} catch (IOException ioe) {
			System.out.println("ioe in HangmanServer constructor: " + ioe.getMessage());
			//unable to bind to port is this exception essentially
		}
	}
	
	//For JDBC connection
	public static boolean connect(String connection, String username, String password){		
		connectionPath = connection + "?user=" + username + "&password=" + password + "&useSSL=false";
			
		try {
			System.out.print("Trying to connect to database...");
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(connectionPath);
			System.out.println("Connected!");
			return true;
		} catch (ClassNotFoundException cnfe) {
			System.out.println(cnfe.getMessage());
			System.out.println("Unable to connect to database " + connection + " with username " + username + " and password " + password + ".");
			return false;
		} catch (SQLException sqle) {
			System.out.println("Unable to connect to database " + connection + " with username " + username + " and password " + password + ".");
			return false;
		}
	}
		
	public static void close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
			if (ps != null ) {
				ps.close();
				ps = null;
			}
		}
		catch(SQLException sqle) {
			System.out.println("connection close error");
			sqle.printStackTrace();
		}
	}
	
	/* Called when a player guessed a wrong word in the middle of the game */
	public void incrementLosingPlayer(String gameName) {
		Game currGame = gamesMap.get(gameName);
		currGame.numberOfLostPlayers++;
	}
	/* No more players left to play game because they all guessed a wrong word */
	public boolean allPlayersGuessedWrong(String gameName) { 
		Game currGame = gamesMap.get(gameName);
		if (currGame.getMaxPlayers() == currGame.numberOfLostPlayers) {
			return true;
		}
		return false;
	}
	
	/* Multiplayer, goes to next player's turn */
	public void nextPlayerTurn(String gameName) {
		Game currGame = gamesMap.get(gameName);
		int turnIndex = currGame.playerTurnIndex; //index of the upcoming player's turn
		currGame.playerTurnIndex++; //increment so next time you'll get the next player
		if (currGame.playerTurnIndex > currGame.getMaxPlayers()-1) { //to circle back if needed
			currGame.playerTurnIndex = 0;
		}
		HangmanServerThread nextPlayer = currGame.playerThreads.get(turnIndex); //get that player at that index
		
		//careful, can be an infinite loop if all players guessed wrongly and lost
		
		//if nextPlayer has already lost, skip him/her and move on to next player
		while (nextPlayer.alreadyLostGame) {
			//find next player
			turnIndex = currGame.playerTurnIndex;
			currGame.playerTurnIndex++;
			if (currGame.playerTurnIndex > currGame.getMaxPlayers()-1) { //to circle back
				currGame.playerTurnIndex = 0; 
			}
			nextPlayer = currGame.playerThreads.get(turnIndex);
		}
		
		//broadcast to other users that it's this player's move
		Message waitMessage = new Message(Type.WaitForTurn);
		waitMessage.currentTurnPlayerName = nextPlayer.thisUsername;
		for (HangmanServerThread thread : currGame.playerThreads) {
			if (thread != nextPlayer) {
				thread.sendMessage(waitMessage);
			}
		}
		
		//send message to the next player for it to pick a move
		Message turnMessage = new Message(Type.MultiplayerTurn);
		currGame.playerThreads.get(turnIndex).sendMessage(turnMessage);
	}
	
	public void broadcastToAllExcept(Message m, HangmanServerThread hst, Game game) {
		for (HangmanServerThread thread : game.playerThreads) {
			if (thread != hst) {
				thread.sendMessage(m);
			}
		}
	}
	public void broadcastToAllExcept(Message m, HangmanServerThread hst, String gameName) { //overloaded with game name
		Game currGame = gamesMap.get(gameName);
		for (HangmanServerThread thread : currGame.playerThreads) {
			if (thread != hst) {
				thread.sendMessage(m);
			}
		}
	}
	
	public void broadcastToAll(Message m, Game game) {
		for (HangmanServerThread thread : game.playerThreads) {
			thread.sendMessage(m);
		}
	}
	public void broadcastToAll(Message m, String gameName) { //overloaded with game name
		Game currGame = gamesMap.get(gameName);
		for (HangmanServerThread thread : currGame.playerThreads) {
			thread.sendMessage(m);
		}
	}
	
	//Called when a user joins a game, broadcast to all users in that game that this user has joined
	public void joinGameBroadcast(Message message, HangmanServerThread hst) {
		Game currGame = gamesMap.get(message.getGameName()); //get the game this user is in
		
		//this gets all the other player info for this newly joined player to display
		currGame.getOtherPlayersInfo(message, hst);
		
		//this broadcasts this player's info for all the other users in the game.
		Message tempMessage = new Message(Type.JustJoinedOtherPlayersInfo);
		tempMessage.otherUsernames.add(hst.thisUsername);
		tempMessage.otherWins.add(hst.thisWins);
		tempMessage.otherLosses.add(hst.thisLosses);
		broadcastToAllExcept(tempMessage, hst, currGame);
		
		//now check if all users have joined, if they have, start the game, if not, wait
		
		//not all users joined yet
		if (currGame.getCurrentNumOfPlayers() < currGame.getMaxPlayers()) { 
			//broadcast to all players of this game to wait for however many more players
			Message waitMessage = new Message(Type.WaitingForUsers);
			waitMessage.numOfUsersToJoin = currGame.getMaxPlayers() - currGame.getCurrentNumOfPlayers();
			broadcastToAll(waitMessage, currGame);
		}
		//all users joined, so can start game. Also, let player who created game start first
		else { 
			//username is the creator of the game
			System.out.print(sdf.format(new Date()));
			System.out.print(" " + currGame.gameCreatorName + " - " + currGame.getGameName() + " has " + currGame.getCurrentNumOfPlayers() + " players so starting game.");
			System.out.println(" Secret word is " + currGame.getSecretWord() + ".");
			
			Message startMessage = new Message(Type.StartMultiplayerGame);
			startMessage.setGuessesRemaining(7); //initially there are 7 guesses total for the game
			startMessage.setSecretWord(currGame.getSecretWord());
			startMessage.setWordLength(currGame.getWordLength());
			broadcastToAll(startMessage, currGame);
			
			
			//Kickstart the game by having player1 (creator of game) begin their turn, and the other users wait
			Message waitForTurn = new Message(Type.WaitForTurn);
			waitForTurn.currentTurnPlayerName = currGame.playerThreads.get(0).thisUsername; //username of first player
			broadcastToAllExcept(waitForTurn, currGame.playerThreads.get(0), currGame);
			Message firstTurn = new Message(Type.MultiplayerTurn);
			currGame.playerThreads.get(0).sendMessage(firstTurn);
			
			//ok done starting the game and getting it up and running for now
		}
	}
	
	
	/* Called when a user tries to log in, check if that user is already in database 
	 * If user is in and password correct: Message UserInfo and send their win-loss record over. 
	 * If user is in but password wrong: Message PasswordNotMatch.
	 * Is user is not in database: Message AccountDoesNotExist. */
	public void userInDatabase(Message message, HangmanServerThread hst) {
		//Pass the message object around, just modify it, don't need to create new messages
		
		try {
		    System.out.print(sdf.format(new Date())); //turns current time into string format to print out
			System.out.println(" " + message.getUsername() + " - trying to log in with password " + message.getPassword() + ".");
			
			int userID = 0;
			ps = conn.prepareStatement("SELECT * FROM Users WHERE BINARY username=?"); //BINARY makes it case-sensitive
			ps.setString(1, message.getUsername());
			rs = ps.executeQuery();
			
			boolean userExists = false; //if username is in database
			boolean correctPass = false; //whether both username and password are correct
			
			if(rs.next()) { //if user with that username exists
				userExists = true;
				userID = rs.getInt("userID");
				if (message.getPassword().equals(rs.getString("pw"))) {
					correctPass = true;
				}
			}
			
			if (correctPass) { //both username and password is correct, user can login. Get their win/loss record
				message.setType(Type.UserInfo);
				int wins = 0;
				int losses = 0;
				ps = conn.prepareStatement("SELECT * FROM Stats WHERE userID=?");
				ps.setInt(1, userID);
				rs = ps.executeQuery();
				if(rs.next()) {
					wins = rs.getInt("wins");
					losses = rs.getInt("losses");
				}
				message.setWins(wins);
				message.setLosses(losses);
				
			    System.out.print(sdf.format(new Date()));
				System.out.println(" " + message.getUsername() + " - successfully logged in.");
				System.out.print(sdf.format(new Date()));
				System.out.println(" " + message.getUsername() + " - has record " + wins + " wins and " + losses + " losses.");
			}
			else if (userExists && !correctPass) { //username exists, but wrong password
				message.setType(Type.PasswordNotMatch);
			    System.out.print(sdf.format(new Date()));
				System.out.println(" " + message.getUsername() + " - has an account but not successfully logged in.");
			}
			else {
				message.setType(Type.AccountDoesNotExist); 
			    System.out.print(sdf.format(new Date()));
				System.out.println(" " + message.getUsername() + " - does not have an account so not successfully logged in.");
			}
			
			//send this message back to the corresponding hst that tried to log in
			hst.sendMessage(message);
			
		} catch (SQLException e) {
			System.out.println("SQLException in HangmanServer userInDatabase");
			e.printStackTrace();
		}
	}
	
	/* Called when a new user is to be created */
	public void createNewUser(Message message, HangmanServerThread hst) {
		try {
			ps = conn.prepareStatement("INSERT INTO Users (username, pw) VALUES (?, ?)"); //add new user
			ps.setString(1, message.getUsername()); //use the username and pw stored in this message
			ps.setString(2, message.getPassword());
			ps.executeUpdate();
			//Now this username and password is a new record in the Users table
			
			//need to create a row for the Stats table for this user as well
			int userID = 0;
			ps = conn.prepareStatement("SELECT userID FROM Users WHERE BINARY username=?");
			ps.setString(1, message.getUsername());
			rs = ps.executeQuery();
			if (rs.next()) {
				userID = rs.getInt("userID");
			}
			ps = conn.prepareStatement("INSERT INTO Stats (userID, wins, losses) VALUES (?, ?, ?)");
			ps.setInt(1, userID);
			ps.setInt(2, 0);
			ps.setInt(3, 0);
			ps.executeUpdate();
			
			//Server output
		    System.out.print(sdf.format(new Date()));
		    System.out.println(" " + message.getUsername() + " - created an account with password " + message.getPassword() + ".");

		    System.out.print(sdf.format(new Date()));
			System.out.println(" " + message.getUsername() + " - successfully logged in.");
			
			System.out.print(sdf.format(new Date()));
			System.out.println(" " + message.getUsername() + " - has record 0 wins and 0 losses.");
		    
			message.setType(Type.UserInfo);
			message.setWins(0); //initially for new account the stats are 0
			message.setLosses(0);
			hst.sendMessage(message);
		}
		catch (SQLException e) {
			System.out.println("SQLException in HangmanServer createNewUser");
			e.printStackTrace();
		}
	}
	
	/* Called when a new game is to be created */
	public void createGame(Message message, HangmanServerThread hst) {
		String thisGameName = message.getAnswerString(); //the game name will be stored in the answer string
		
		message.setGameName(thisGameName); //also need attempted game name to display in client
		
	    System.out.print(sdf.format(new Date()));
	    System.out.println(" " + message.getUsername() + " - wants to start a game called " + thisGameName + ".");
		
		//check if the game name is already in use
		boolean gameAlreadyExists = false;
		for (String nameKey : gamesMap.keySet()) { //loop over keys (which has the game name)
			if (nameKey.equals(thisGameName)) {
				gameAlreadyExists = true;
				break;
			}
		}
		
		if (gameAlreadyExists) {
			Date now2 = new Date();
		    System.out.print(sdf.format(now2));
		    System.out.println(" " + message.getUsername() + " - " + thisGameName + " already exists, so unable to start " + thisGameName + ".");
		    
			message.setType(Type.GameAlreadyExists);
			hst.sendMessage(message);
		}
		else {
			Game newGame = new Game(thisGameName);
			newGame.addPlayer(hst); //add the player hst who tries to start a game as the first player.
			newGame.gameCreatorName = message.getUsername(); //this user created this game
			//note that addPlayer returns a boolean, but it must be true as this is the first player, and maxPlayer is initially 1
			message.setGameName(thisGameName); //the name of this game will be stored in the message
			message.setType(Type.AskHowManyUsers);
			gamesMap.put(thisGameName, newGame);
			
			Date now3 = new Date();
		    System.out.print(sdf.format(now3));
		    System.out.println(" " + message.getUsername() + " - successfully started game " + thisGameName + ".");
			
			//now determine a secret word for it
			determineSecretWord(newGame, message);
			
			hst.sendMessage(message);
		}
	}
	
	public String tryJoinGame(Message message, String gameName, HangmanServerThread hst) {
		Game currGame = gamesMap.get(gameName);
		if (currGame == null) {
			return "GameDoesNotExist";
		}
		if (currGame.addPlayer(hst)) { //addPlayer will return true for successful adding 
			return "SuccessfullyJoinedGame";
		}
		else { //and false for cannot add player, cuz max players
			return "UnableToJoinGame";
		}
	}
	
	/* This generates random word, puts it into the game and message object (so it can be passed around) */
	private void determineSecretWord(Game game, Message message) {
		int totalWords = wordList.size();
		Random rand = new Random();
		int index = rand.nextInt(totalWords); //generate a number between 0 and totalWords(not inclusive)
		String randomWord = wordList.get(index).toLowerCase();
		
		//put the secret word into the Game and Message objects
		game.setSecretWord(randomWord);
		
		//Store the secret word in the message so it's easy to access in hst if needed
		message.setSecretWord(randomWord);
		message.setWordLength(randomWord.length());
	}
	
	/* Called by  a hst, when they need to know the secret word of the game they're in */
	public String getSecretWord(String gameName) {
		Game currGame = gamesMap.get(gameName);
		return currGame.getSecretWord();
	}
	
	public void setNumPlayers(int num, Message message) {
		String gameName = message.getGameName();
		Game currGame = gamesMap.get(gameName); //currGame is now a reference to the same object.
		currGame.setMaxPlayers(num);
		//now the game associated with that message has the number of players set.
		//this function is called early on when creating the new game
	}
	
	public String guessLetter(Message message, String letter) {
		String gameName = message.getGameName();
		Game currGame = gamesMap.get(gameName);
		
	    System.out.print(sdf.format(new Date()));
	    System.out.println(" " + gameName + " " + message.getUsername() + " - guessed letter " + letter + ".");
		
		char l = letter.charAt(0); //convert a string letter to char for this function
		GameState gs = currGame.guessLetter(l);
		String partialWord = currGame.getPartialWord();
		
		//This Hangman game is simple such that you don't need to keep track of letters already guessed, only right/wrong
		if (gs.equals(GameState.CorrectGuess)) {
			String positions = currGame.getLetterIndicesAsString();
			
		    System.out.print(sdf.format(new Date()));
		    System.out.print(" " + gameName + " " + message.getUsername() + " - " + letter + " is in " + currGame.getSecretWord());
		    System.out.println(" in position(s)" + positions + ". Secret word now shows " + partialWord + ".");
		    
		    //these two needs to be displayed to client, partial word and guesses remaining
		    message.setPartialWord(partialWord);
		    message.setGuessesRemaining(currGame.getRemainingGuesses()); //still set this here, in case multiplayer changes
		    
			return "CorrectGuess";
		}
		
		//else must be wrong guess
		
	    System.out.print(sdf.format(new Date()));
	    System.out.print(" " + gameName + " " + message.getUsername() + " - " + letter + " is not in " + currGame.getSecretWord() + ". ");
	    System.out.println(gameName + " now has " + currGame.getRemainingGuesses() + " guesses remaining.");
		
		message.setPartialWord(partialWord);
		message.setGuessesRemaining(currGame.getRemainingGuesses());
		return "WrongGuess";
	}
	
	//Make this synchronized? Basically make sure only one thread can access it at one time
	public boolean guessWord(Message message, String wordToGuess) {
		//get the relevant game associated with that message
		String gameName = message.getGameName();
		Game currGame = gamesMap.get(gameName);

		return currGame.guessWord(wordToGuess);
	}
	
	/* Called in HST after guessing a wrong word and before moving to next turn */
	public boolean outOfGuesses(String gameName) {
		Game currGame = gamesMap.get(gameName);
		if (currGame.getRemainingGuesses() <= 0) {
			return true;
		}
		return false;
	}
	
	//server output "<otherUsernames> have lost the game."
	public void otherUsernamesLost(HangmanServerThread hst, String gameName) {
		Game currGame = gamesMap.get(gameName);
		
		for (HangmanServerThread thread : currGame.playerThreads) {
			if (thread != hst) {
				System.out.print(thread.thisUsername + " ");
			}
		}
		System.out.println("have lost the game.");
	}
	
	/* Called when 1 player guessed word and won, and others lost. To display all users stats */
	public void otherUsersLost(HangmanServerThread hst, String gameName, String winner) {
		Game currGame = gamesMap.get(gameName);
		
		for (HangmanServerThread eachPlayerThread : currGame.playerThreads) {
			Message stats = new Message(Type.SomeoneCorrectGuessYouLose);
			stats.winnerName = winner;
			stats.setSecretWord(currGame.getSecretWord());
			stats.setUsername(eachPlayerThread.thisUsername);
			stats.setWins(eachPlayerThread.thisWins);
			stats.setLosses(eachPlayerThread.thisLosses);
			for (HangmanServerThread otherPlayerThread : currGame.playerThreads) {
				if (eachPlayerThread != otherPlayerThread) {
					stats.otherUsernames.add(otherPlayerThread.thisUsername);
					stats.otherWins.add(otherPlayerThread.thisWins);
					stats.otherLosses.add(otherPlayerThread.thisLosses);
				}
			}
			eachPlayerThread.sendMessage(stats);
		}
	}
	
	/* Called when guessed = 0, all players lose */
	public void allPlayersLost(String gameName) {
		Game currGame = gamesMap.get(gameName);
		
		for (HangmanServerThread eachPlayerThread : currGame.playerThreads) {
			Message stats = new Message(Type.EveryoneLoses);
			stats.setSecretWord(currGame.getSecretWord());
			stats.setUsername(eachPlayerThread.thisUsername);
			stats.setWins(eachPlayerThread.thisWins);
			stats.setLosses(eachPlayerThread.thisLosses);
			for (HangmanServerThread otherPlayerThread : currGame.playerThreads) {
				if (eachPlayerThread != otherPlayerThread) {
					stats.otherUsernames.add(otherPlayerThread.thisUsername);
					stats.otherWins.add(otherPlayerThread.thisWins);
					stats.otherLosses.add(otherPlayerThread.thisLosses);
				}
			}
			eachPlayerThread.sendMessage(stats);
		}
	}
	
	//ONLY FOR SINGLEPLAYER
	//May need to make this synchronized as well, as all player threads update their stats at the end
	public void updateStats(Message message) {
		int userID = 0;
		
		//current stats after win/lose the game, stored in message (updated )
		int currWins = message.getWins();
		int currLosses = message.getLosses();
		
		try {
			ps = conn.prepareStatement("SELECT userID FROM Users WHERE BINARY username=?");
			ps.setString(1, message.getUsername());
			rs = ps.executeQuery();
			if (rs.next()) {
				userID = rs.getInt("userID");
			}
			
			ps = conn.prepareStatement("UPDATE Stats SET wins=?, losses=? WHERE userID=?");
			ps.setInt(1, currWins);
			ps.setInt(2, currLosses);
			ps.setInt(3, userID);
			ps.executeUpdate();
			//check db to see if it's updated
		}
		catch (SQLException e) {
			System.out.println("SQLException in HangmanServer createNewUser");
			e.printStackTrace();
		}
	}
	
	/* Popluate the message with other users stats in the game */
	public void uploadOtherPlayersStats(Message message, String gameName, HangmanServerThread hst) {
		Game currGame = gamesMap.get(gameName);
		for (HangmanServerThread thread : currGame.playerThreads) {
			if (thread != hst) {
				message.otherUsernames.add(thread.thisUsername);
				message.otherWins.add(thread.thisWins);
				message.otherLosses.add(thread.thisLosses);
			}
		}
	}
	
	//For multiplayer use, add a loss to all players, used at end of game
	public void incrementLossAllPlayers(String gameName) {
		Game currGame = gamesMap.get(gameName);
		for (HangmanServerThread thread : currGame.playerThreads) {
//			if (!thread.alreadyLostGame) { //if this player haven't already lost in the middle of the game
			//  because if the player lost already, all their stats would have already been incremented once
			thread.thisLosses++;
			String username = thread.thisUsername;
			incrementLossDB(username);
//			}
		}
	}
	public void incrementLossAllPlayersExcept(String gameName, HangmanServerThread hst) {
		Game currGame = gamesMap.get(gameName);
		for (HangmanServerThread thread : currGame.playerThreads) {
			if (thread != hst) {
				thread.thisLosses++; //update the local losses as well
				String username = thread.thisUsername;
				incrementLossDB(username);
			}
		}
	}
	public void incrementLossDB(String username) { //adds a loss in the database
		int userID = 0;
		int oldLosses = 0;
		
		try {
			ps = conn.prepareStatement("SELECT * FROM Users WHERE BINARY username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next()) {
				userID = rs.getInt("userID");
			}
			
			ps = conn.prepareStatement("SELECT * FROM Stats WHERE userID=?");
			ps.setInt(1, userID);
			rs = ps.executeQuery();
			if (rs.next()) {
				oldLosses = rs.getInt("losses");
			}
			int newLosses = oldLosses + 1;
			
			ps = conn.prepareStatement("UPDATE Stats SET losses=? WHERE userID=?");
			ps.setInt(1, newLosses);
			ps.setInt(2, userID);
			ps.executeUpdate();
		}
		catch (SQLException e) {
			System.out.println("SQLException in HangmanServer incrementLosses");
			e.printStackTrace();
		}
	}
	
	//For multiplayer use, add a win to this player
	public void incrementWin(String username) {
		int userID = 0;
		int oldWins = 0;
		
		try {
			ps = conn.prepareStatement("SELECT userID FROM Users WHERE BINARY username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next()) {
				userID = rs.getInt("userID");
			}
			
			ps = conn.prepareStatement("SELECT * FROM Stats WHERE userID=?");
			ps.setInt(1, userID);
			rs = ps.executeQuery();
			if (rs.next()) {
				oldWins = rs.getInt("wins");
			}
			int newWins = oldWins + 1;
			
			ps = conn.prepareStatement("UPDATE Stats SET wins=? WHERE userID=?");
			ps.setInt(1, newWins);
			ps.setInt(2, userID);
			ps.executeUpdate();
		}
		catch (SQLException e) {
			System.out.println("SQLException in HangmanServer incrementWin");
			e.printStackTrace();
		}
	}
	
	public void removeGame(String gameName) {
		gamesMap.remove(gameName);
	}
	
	public boolean allLettersGuessed(String gameName) {
		Game currGame = gamesMap.get(gameName);
		return currGame.allLettersGuessed();
	}
	
	public static void main(String[] args) {
		/* Open config file and read the parameters */
		System.out.print("What is the name of the configuration file? ");
		Scanner scan = new Scanner(System.in);
		String configFilename = "";
		
		String ServerHostname = "";
		String ServerPort = "";
		String DBConnection = "";
		String DBUsername = "";
		String DBPassword = "";
		String SecretWordFile = "";

		while (true) {
			try {
				configFilename = scan.nextLine();
				
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
					
					//Add all the words of secret word file into an arraylist first
					try {
						FileReader fr = new FileReader(SecretWordFile); // may throw FileNotFoundError
						BufferedReader br = new BufferedReader(fr);
						
						String word = br.readLine(); // may throw an IOException
						while (word != null) { // While not eof
							wordList.add(word);
							word = br.readLine(); // read another line
						}
						
						fr.close();
						br.close();
					} catch (FileNotFoundException fnfe) {
						System.out.println("The secret word file could not be found.");
						System.out.print("What is the name of the configuration file?");
						continue;
					} catch (IOException ioe) {
						System.out.println("ioe: " + ioe.getMessage());
					}
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
		scan.close();
		
		//now try to establish a connection to the database
		if (!connect(DBConnection, DBUsername, DBPassword)) { //connect returns true if success
			return; //just quit program if cannot connect to database
		}
		//after this is connected, do we need to connect again? or we can now just straight up use ps and rs...etc.?
		
		/* Now that we have gotten all properties, construct the Hangman server with the ServerPort */ 
		int portNum = Integer.parseInt(ServerPort);
		new HangmanServer(portNum);
		//note: you can do just do new without assigning it to HangmanServer hs, we're not using that variable anyway
		
		
		//maybe only close database at the very end? Do I even need to close it?
		//JDBCDriver.close();
	}
}
