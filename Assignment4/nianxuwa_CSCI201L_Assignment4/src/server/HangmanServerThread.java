package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import data.Message;
import data.Message.Type;

//each HangmanServerThread is responsible for one client (player)
public class HangmanServerThread extends Thread {
	//for serialization
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

	//The "central" HangmanServer
	private HangmanServer hs;
	
	//DO NOT SAVE GAME VARIABLE HERE. If multiple games and multiplayer, this game variable will be overwritten (?)
//	private Game game; //the game this user serverthread in. 
	
	public Message m; //this message object is unique to this thread
	
	private String thisGameName; //the name of the game this user server thread is in
	
	//This is to be used in broadcast multiplayer when new info needs to be sent to every client hst. Use this to add to their own message.
	public Message getMessage() {
		return m; //returns the Message object by reference
	}
	
	//The basic info of the client connected to this server thread
	public String thisUsername; //public so easy to access
	public int thisWins;
	public int thisLosses;
	
	public boolean isSinglePlayer = true;
	
	public boolean alreadyLostGame = false;
	
	public HangmanServerThread(Socket s, HangmanServer hs) {
		this.hs = hs;
		try {
			//now setup to read/write objects from serverthread
			ois = new ObjectInputStream(s.getInputStream());
			oos = new ObjectOutputStream(s.getOutputStream());
			
			//if exception thrown when reading above, then don't start thread
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in HangmanServerThread: " + ioe.getMessage());
		}
	}
	
	//Just sends this message back to the corresponding HangmanClient
	public void sendMessage(Message mToSend) {
		try {
			oos.writeObject(mToSend);
			oos.flush();
		} catch (IOException ioe) {
			System.out.println("ioe in sendMessage: " + ioe.getMessage());
		}
	}
	
	//This reads messages sent from HangmanClient
	public void run() {
		//just read however many messages the user wants to send (?)
		try {
			while (true) {
				m = (Message)ois.readObject(); //blocking call. 
				//The message passed back gets saved into this thread's message object

				if (m != null) {
//					System.out.println("DEBUG In a ServerThread, message is read, of type: " + m.getType());
					
					/* Check the message type sent from the client side and decide what to do */
					
					//If user is entering a username
					if (m.getType().equals(Type.UsernameLogin)) 
					{
						String user = m.getAnswerString().trim(); //username is stored in the answer string
						if (user.isEmpty()) { //if empty string entered for username, just re-prompt for username
							m.setType(Type.UsernameLogin);
						}
						else { //else set the message username and set type to ask for pw
							m.setUsername(user);
							m.setType(Type.PasswordLogin);
						}
						sendMessage(m);
					}
					else if (m.getType().equals(Type.PasswordLogin)) 
					{
						m.setPassword(m.getAnswerString()); //password will be stored in the answer string
						hs.userInDatabase(m, this); //now check if user is in the database
						//the userInDatabase function can send PasswordDoesNotMatch or AccountDoesNotExist messages
					}
					else if (m.getType().equals(Type.AccountDoesNotExist)) 
					{
						if (m.getAnswerString().equals("Yes") || m.getAnswerString().equals("yes") || m.getAnswerString().equals("y") || m.getAnswerString().equals("YES") || m.getAnswerString().equals("Y")) {
							m.setType(Type.PromptUserSameUserPw);
							sendMessage(m);
						}
						else
						{
							//Repeat the same process for getting user to input username and password
							m.setType(Type.UsernameLogin); //first ask for username
							sendMessage(m);
						}
					}
					else if (m.getType().equals(Type.PromptUserSameUserPw)) 
					{
						if (m.getAnswerString().equals("Yes") || m.getAnswerString().equals("yes") || m.getAnswerString().equals("y") || m.getAnswerString().equals("YES") || m.getAnswerString().equals("Y")) {
							hs.createNewUser(m, this); //call database create user and set stats
							//createNewUser function sets message type to userinfo
						}
						else
						{
							//Repeat the same process for getting user to input username and password
							m.setType(Type.UsernameLogin);
							sendMessage(m);
						}
					}
					else if (m.getType().equals(Type.UserInfo))
					{
						//This means the user has just logged in and displayed the user info on client side, and now
						//	wants to choose between starting or joining a game
						
						//perfect time to update this user's info, as they have just logged in and are correct 
						thisUsername = m.getUsername();
						thisWins = m.getWins();
						thisLosses = m.getLosses();
						
						String option = m.getAnswerString().trim();
						if (option.equals("1")) { //start a game
							m.setType(Type.StartNewGame);
							sendMessage(m);
						}
						else if (option.equals("2")) { //join a game
							m.setType(Type.JoinGame);
							sendMessage(m);
						}
						else { //invalid choice, choose again
							m.setType(Type.InvalidStartJoinChoice);
							sendMessage(m);
						}
					}
					else if (m.getType().equals(Type.StartNewGame)) 
					{
						thisGameName = m.getAnswerString(); //the name of the game is stored here at this point
						//this function will also check if a game with same name already exists
						hs.createGame(m, this);
					}
					else if (m.getType().equals(Type.AskHowManyUsers)) 
					{
						//So from client side what is returned should be the number of users
						String numStr = m.getAnswerString(); //contains in string form the number the user entered
						boolean exceptionThrown = false;
						int numUsers = 1;
						try {
							numUsers = Integer.parseInt(numStr);
						}
						catch (NumberFormatException nfe) {
							exceptionThrown = true;
							//remember, after catch clause, code still runs, so use a boolean exceptionThrown to control it
						}
						
						if (exceptionThrown || numUsers < 1 || numUsers > 4) { //only between 1-4 players
							m.setType(Type.InvalidNumOfUsers);
							sendMessage(m);
						}
						else if (numUsers == 1) {
							isSinglePlayer = true;
							
							m.setType(Type.StartSinglePlayerGame);
							hs.setNumPlayers(1, m);
							
						    System.out.print(sdf.format(new Date()));
						    System.out.println(" " + m.getUsername() + " - " + m.getGameName() + " needs 1 player to start game.");
							//note: secret word is stored in message for convenience
						    System.out.print(sdf.format(new Date()));
						    System.out.println(" " + m.getUsername() + " - " + m.getGameName() + " has 1 player so starting game. Secret word is " + m.getSecretWord() + ".");
						    
							m.setGuessesRemaining(7); //initially 7 guesses. message needs this to display
							//the game will now start for single player!
							sendMessage(m);
						}
						else {
							isSinglePlayer = false;
							
							hs.setNumPlayers(numUsers, m); //set number of users
							
							System.out.print(sdf.format(new Date()));
							System.out.println(" " + m.getUsername() + " - " + m.getGameName() + " needs " + numUsers + " player(s) to start game.");
							
							m.setType(Type.WaitingForUsers);
							m.numOfUsersToJoin = numUsers - 1; //cuz the creator is already in the game
							
							//Note: the client that starts the game should be the only one in this part
							
							m.setGuessesRemaining(7); //initially 7 guesses for multiplayer game
							//don't think this part gets called...
							
							sendMessage(m);
						}
					}
					else if (m.getType().equals(Type.JoinGame)) {						
						String gameName = m.getAnswerString(); //user answers what is the name of the game
						m.setGameName(gameName); //set the game name so can display in client console
						
						System.out.print(sdf.format(new Date()));
						System.out.println(" " + m.getUsername() +  " - wants to join a game called " + gameName + ".");
						
						String result = hs.tryJoinGame(m, gameName, this);
						
						if (result.equals("GameDoesNotExist")) {
							m.setType(Type.GameDoesNotExist);
							sendMessage(m);
						}
						else if (result.equals("SuccessfullyJoinedGame")) {
							m.setType(Type.SuccessfullyJoinedGame);
							
							isSinglePlayer = false; //any user that has to join is not in a single player game
							
							thisGameName = gameName; //set game name when a user wants to join this game
							
							System.out.print(sdf.format(new Date()));
							System.out.println(" " + m.getUsername() + " - successfully joined game " + gameName + ".");

							//do a broadcast from hs and the game to say that user joined
							hs.joinGameBroadcast(m, this);
						}
						else { //max players in game
							m.setType(Type.UnableToJoinGame);
							System.out.print(sdf.format(new Date()));
							System.out.println(" " + m.getUsername() + " - " + gameName + " exists, but " + m.getUsername() + " unable to join because maximum number of players have already joined " + gameName + ".");
							sendMessage(m);
						}
					}
					else if (m.getType().equals(Type.Option)) 
					{
						String answerStr = m.getAnswerString().trim(); //contains in string form the number the user entered
						
						if (answerStr.equals("1")) { //user wants to guess a letter
							m.setType(Type.GuessLetter);
							sendMessage(m);
						}
						else if (answerStr.equals("2")) { //user wants to guess the word
							m.setType(Type.GuessWord);
							sendMessage(m);
						}
						else {
							m.setType(Type.InvalidOption);
							sendMessage(m);
						}
					}
					else if (m.getType().equals(Type.GuessLetter))
					{
						//from above, user should enter a letter
						String letter = m.getAnswerString().toLowerCase().trim(); //make case insensitive
						
						//re-set the username and game name cuz they were lost
						m.setUsername(thisUsername); 
						m.setGameName(thisGameName);
						
						m.setLetterJustGuessed(letter); //so it can be retrieved for display later in client
						
						//ensure that a single letter is guessed
						if (letter.length() > 1 || letter.isEmpty()) {
							//not a letter. make this a type in message
							m.setType(Type.NotALetter);
							sendMessage(m);
						}
						else {
							//multiplayer broadcast what this player has guessed
							if (!isSinglePlayer) { 
								//E.g. Tommy has guessed letter ‘a’.
								Message currentLetterGuess = new Message(Type.OtherPlayerGuessLetter);
								currentLetterGuess.currentTurnGuess = letter;
								currentLetterGuess.currentTurnPlayerName = thisUsername;
								hs.broadcastToAllExcept(currentLetterGuess, this, thisGameName);
							}
							
							//if it is an actual letter
						    String result = hs.guessLetter(m, letter);
						    
						    //don't need to consider if the letter is already guessed right or wrong
						    if (result.equals("CorrectGuess")) {
						    	if (isSinglePlayer) {
						    		m.setType(Type.LetterGuessCorrect);
							    	sendMessage(m);
						    	}
						    	else { //multiplayer and the current turn player guessed correctly
						    		
						    		// m has partialword and guesses remaining, from the hs.guessLetter function
						    		Message letterCorrect = new Message(Type.MultiplayerLetterCorrect);
						    		letterCorrect.setLetterJustGuessed(letter);
						    		letterCorrect.setPartialWord(m.getPartialWord());
						    		letterCorrect.setGuessesRemaining(m.getGuessesRemaining());
						    		letterCorrect.setUsername(thisUsername);
						    		
						    		//broadcast to EVERYONE this letter guess was correct
						    		hs.broadcastToAll(letterCorrect, thisGameName);
						    		
						    		//now move onto the next player's turn
						    		//(Miller kinda made it easier so that only by choosing option 2 can you win...)
						    		
						    		//If this current player has guessed the last letter(s) in the word, they win, others lose
						    		if (hs.allLettersGuessed(thisGameName)) {
						    			//do the same thing as if this player guessed the correct word:
						    			
						    			//server output
						    			System.out.print(sdf.format(new Date()));
									    System.out.print(" " + thisGameName + " " + thisUsername + " - " + hs.getSecretWord(thisGameName) + " is correct. ");
									    System.out.print(thisUsername + " wins game. ");
									    
									    //Server output "<otherUsernames> have lost the game."
										hs.otherUsernamesLost(this, thisGameName);
										
										//update stats in db for this winning user first
										hs.incrementWin(thisUsername);
										thisWins++;
										m.setUsername(thisUsername);
										m.setWins(thisWins);
										m.setLosses(thisLosses);
										
										//update stats in db for the remaining losing users
										hs.incrementLossAllPlayersExcept(thisGameName, this);
										
										/* For the player that wins: In client console: */
										m.setType(Type.MultiGuessedCorrectWord);
										
										//get into message and display all the other users stats for this winning player
										hs.uploadOtherPlayersStats(m, thisGameName, this);

										sendMessage(m);
										
										//For all the other players that lose: In client console:
										// For the other users, list their own stats first, then other players stats
										hs.otherUsersLost(this, thisGameName, thisUsername);
										
										hs.removeGame(thisGameName);
						    		}
						    		//otherwise if not, then move to next turn
						    		else {
						    			//next player's turn
							    		hs.nextPlayerTurn(thisGameName);
						    		}						    		
						    	}
						    }
						    else if (result.equals("WrongGuess")) {
						    	//if this game is out of guesses
						    	if (hs.outOfGuesses(thisGameName)) {						    		
					    			if (isSinglePlayer) { //deal with single player wrong guess, end game
					    				System.out.print(sdf.format(new Date()));
						    			System.out.println(" " + thisUsername + " has lost and is no longer in the game.");
						    			m.setType(Type.NoGuessesLeftSingle);
						    			int newLosses = m.getLosses() + 1;
										m.setLosses(newLosses);
										hs.updateStats(m);
										hs.removeGame(thisGameName); //remove game from the map, single player game is done
										sendMessage(m);
						    		}
						    		else { //multiplayer after wrong letter guess, OUT OF GUESSES. Game ends for ALL players
						    			m.setType(Type.MultiplayerOutOfGuesses);
						    			
						    			//add a loss for all the players to db and each thread
						    			hs.incrementLossAllPlayers(thisGameName);
						    			
						    			//end game for all users
						    			hs.allPlayersLost(thisGameName);
						    			
						    			hs.removeGame(thisGameName);
						    		}
						    	}
						    	else { //else if there are still lives left in the game
						    		if (isSinglePlayer) {
							    		m.setType(Type.LetterGuessWrong);
								    	sendMessage(m);
							    	}
							    	else { //multiplayer, guessed wrong	    								    		
							    		Message letterWrong = new Message(Type.MultiplayerLetterWrong);
							    		letterWrong.setLetterJustGuessed(letter);
							    		letterWrong.setPartialWord(m.getPartialWord());
							    		letterWrong.setGuessesRemaining(m.getGuessesRemaining());
							    		letterWrong.setUsername(thisUsername);
							    		
							    		//broadcast to EVERYONE this letter guess was wrong
							    		hs.broadcastToAll(letterWrong, thisGameName);
							    		
							    		//Next player's turn
								    	hs.nextPlayerTurn(thisGameName);
							    	}
						    	}
						    }
						}
					}
					else if (m.getType().equals(Type.GuessWord)) 
					{
						String guessWord = m.getAnswerString().toLowerCase().trim();
						
					    System.out.print(sdf.format(new Date()));
					    System.out.println(" " + thisGameName + " " + thisUsername + " - guessed word " + guessWord + ".");
					    
					    m.setGameName(thisGameName);
					    
						boolean guessCorrect = hs.guessWord(m, guessWord);
						if (guessCorrect) { //correctly guessed the whole word
							//server output
						    System.out.print(sdf.format(new Date()));
						    System.out.print(" " + thisGameName + " " + thisUsername + " - " + guessWord + " is correct. ");
						    System.out.print(thisUsername + " wins game. ");
							
							//for single player game, correct word guess ends game
							if (isSinglePlayer) {
								m.setType(Type.SingleGuessedCorrectWord);
								//update stats (+1 win) for this player
								int newWins = m.getWins() + 1;
								m.setWins(newWins);
								hs.updateStats(m);
								hs.removeGame(thisGameName); //remove game from the map, cuz the game is done
								sendMessage(m);
							}
							//multiplayer, broadcast to all that this player guessed correct word
							else {
								//Server output "<otherUsernames> have lost the game."
								hs.otherUsernamesLost(this, thisGameName);
								
								//update stats in db for this winning user first
								hs.incrementWin(thisUsername);
								thisWins++;
								m.setUsername(thisUsername);
								m.setWins(thisWins);
								m.setLosses(thisLosses);
								
								//update stats in db for the remaining losing users
								hs.incrementLossAllPlayersExcept(thisGameName, this);
								
								/* For the player that wins: In client console: */
								m.setType(Type.MultiGuessedCorrectWord);
								
								//get into message and display all the other users stats for this winning player
								hs.uploadOtherPlayersStats(m, thisGameName, this);

								sendMessage(m);
								
								//For all the other players that lose: In client console:
								// For the other users, list their own stats first, then other players stats
								hs.otherUsersLost(this, thisGameName, thisUsername);
								
								hs.removeGame(thisGameName);
							}
						}
						else { //guessed the wrong word
						    System.out.print(sdf.format(new Date()));
						    System.out.print(" " + thisGameName + " " + thisUsername + " - " + guessWord + " is incorrect. ");
						    System.out.println(thisUsername + " has lost and is no longer in the game.");
						    
						    if (isSinglePlayer) {
						    	m.setType(Type.SingleGuessedWrongWord);
						    	//update stats (+1 loss) for this player
								int newLosses = m.getLosses() + 1;
								m.setLosses(newLosses);
								hs.updateStats(m);
								hs.removeGame(thisGameName); //remove game from the map, cuz the game is done
						    	sendMessage(m);
						    }
						    else {
						    	//indicates this user lost in the middle of the game
						    	alreadyLostGame = true;
						    	
						    	//broadcast so other players know
						    	Message aUserLost = new Message(Type.GuessedWrongWord);
						    	aUserLost.currentTurnGuess = guessWord;
						    	aUserLost.loserName = thisUsername;
						    	hs.broadcastToAllExcept(aUserLost, this, thisGameName);
						    	
						    	//update loss stats in db for this losing user
//						    	hs.incrementLossDB(thisUsername);
//						    	this.thisLosses++;
						    	
						    	//don't update losses for this player yet, because this player cannot win at end of game.
						    	//so either someone else wins, or all player lose, and this player's stats will be updated as a loss anyway

						    	//for this losing user in the client console
						    	m.setType(Type.MultiGuessedWrongWord);
						    	sendMessage(m);
						    	
						    	//check if all the users are dead
						    	hs.incrementLosingPlayer(thisGameName);
						    	if (hs.allPlayersGuessedWrong(thisGameName)) {
						    		//no more players left to play game
						    		hs.allPlayersLost(thisGameName);
						    		hs.removeGame(thisGameName); //if all players lost then remove game
						    	}
						    	else { //there are still players left to play the game
						    		//then move to next person's turn.
							    	hs.nextPlayerTurn(thisGameName);
						    	}
						    }
						}
					}
				}
			}
		} catch (IOException ioe) {
			System.out.println("ioe in HangmanServerThread.run(): " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in HangmanServerThread.run(): " + cnfe.getMessage());
		}
	}
}
