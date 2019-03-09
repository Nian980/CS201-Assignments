package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
	private static final long serialVersionUID = 1;
	
	/* Possible types of statuses: 
	 * UsernameLogin - Sent by client & server, passing username back into server, or need user's username
	 * PasswordLogin - Sent by client & server, passing password back into server, or need user's pw
	 * AccountDoesNotExist - Sent by server, If user tries to log in and account does not exist.
	 * PasswordNotMatch - Sent by server, If user exists in database, but password is wrong.
	 * UserInfo - Sent by server, which extracts user's name and win loss from database. Signals end of user account phase. 
	 * PromptUserSameUserPw - Sent by server, which tells client-side to ask user if they want to create acc with the user/pw above
	 * CreateAccount - Sent by client, who wants to create account.
	 * InvalidStartJoinChoice - Send by server, user did not choose 1 or 2.
	 * StartNewGame - Sent by server & client, user chose to start a new game.
	 * JoinGame - Sent by serverthread, user chose to join a game.
	 * GameAlreadyExists - Seny by server, if the game name already in use.
	 * GameDoesNotExist - Sent by server, if the user tries to join a game name that doesn't exist
	 * SuccessfullyJoinedGame - Sent by server, the user successfully joined a game
	 * UnableToJoinGame - Sent by server, game already has max number of players, so cannot join
	 * JustJoinedOtherPlayersInfo - Sent by Game, this user just joined a game, so send other players info to him/her
	 * AskHowManyUsers - Sent by server, asks client who created new game how many users it will contain.
	 * InvalidNumOfUsers - Sent by server, if user did not enter an integer 1-4.
	 * xxxx MultipleUsersNotYetImplemented - temp sent by server, can only do 1 player game for now.
	 * StartSinglePlayerGame - Sent by server, will begin singleplayer game (also determine secret word as well).
	 * StartMultiplayerGame - Sent by server, note that the message carrying this will be a new message.
	 * WaitingForUsers - Sent by server to client, the game you are in is waiting for more players to join.
	 * Option - Sent by client, either 1 or 2 when user is playing game and making guesses. SinglePlayer.
	 * MultiplayerTurn - Sent by server, indicates that client can take their turn.
	 * WaitForTurn - Sent by server, tells this player to wait for their turn. Stores name of current turn player
	 * InvalidOption - Sent by server, did not choose 1 or 2.
	 * GuessLetter - Sent by server & client, allows user to guess a letter.
	 * GuessWord - Sent by server & client, allows user to guess the word.
	 * NotALetter - Sent by server, when user wanted to guess letter, but didn't enter a letter.
	 * LetterGuessCorrect - Sent by server, the user's letter guess was correct.
	 * LetterGuessWrong - Sent by server, the user's letter guess was wrong.
	 * MultiplayerLetterCorrect - Multiplayer equivalent of above, just slightly different output and behaviour
	 * MultiplayerLetterWrong - Multiplayer equivalent of above, just slightly different output and behaviour
	 * OtherPlayerGuessLetter - Broadcasted by server, what letter current turn player guessed
	 * NoGuessesLeftSingle - Sent by server, no more guesses left for that game. SinglePlayer
	 * MultiplayerOutOfGuesses - Sent by server, no more guesses left for that game. Multiplayer, all lose game
	 * GuessedWrongWord - Sent by server, user tried guessing a word, got it wrong, and hence lost the game. Multiplayer
	 * GuessedCorrectWord - Sent by server, user guessed the correct secret word. Multiplayer
	 * SingleGuessedWrongWord - Single player, sent by server, guessed wrong word, single player game ends player loses.
	 * SingleGuessedCorrectWord - Single player, sent by server, user guessed correct word. Game ends player wins. 
	 * MultiGuessedWrongWord - This player guessed the wrong word, out of game
	 * MultiGuessedCorrectWord - This player wins, prints out stats, others lose.
	 * SomeoneCorrectGuessYouLose - Sent by server, another player guessed the correct word, so you lost
	 * EveryoneLoses - 0 guesses left multiplayer, all lose
	 * EveryoneGuessedWrong - all lose because all of the guessed a wrong word
	 * */
	
	public enum Type {
		UsernameLogin,
		PasswordLogin,
		AccountDoesNotExist,
		PasswordNotMatch,
		UserInfo,
		PromptUserSameUserPw,
		CreateAccount,
		InvalidStartJoinChoice,
		StartNewGame,
		JoinGame,
		GameAlreadyExists,
		GameDoesNotExist,
		SuccessfullyJoinedGame,
		UnableToJoinGame,
		JustJoinedOtherPlayersInfo,
		AskHowManyUsers,
		InvalidNumOfUsers,
		StartSinglePlayerGame,
		StartMultiplayerGame,
		WaitingForUsers,
		Option,
		MultiplayerTurn,
		WaitForTurn,
		InvalidOption,
		GuessLetter,
		GuessWord,
		NotALetter,
		LetterGuessCorrect,
		LetterGuessWrong,
		MultiplayerLetterCorrect,
		MultiplayerLetterWrong,
		OtherPlayerGuessLetter,
		NoGuessesLeftSingle,
		MultiplayerOutOfGuesses,
		GuessedWrongWord,
		GuessedCorrectWord,
		SingleGuessedWrongWord,
		SingleGuessedCorrectWord,
		MultiGuessedWrongWord,
		MultiGuessedCorrectWord,
		SomeoneCorrectGuessYouLose,
		EveryoneLoses,
		EveryoneGuessedWrong
	}

	//Message type
	private Type type;
	
	//The game where this object is being passed around in
	//Use this to name to find the correscponding game the gamesMap in HangmanServer
	private String gameName;
	
	private String username;
	private String password;
	
	private int wins;
	private int losses;
	
	private String answerString; //stores user input
	
	//not sure what's the point of having the secret word in the message here. may delete later?
	private String secretWord;
	private int wordLength;
	
	//Used to display how many guesses this user has left in the client console
	private int guessesRemaining; //stores an up-to-date number of total guesses remaining.
	
	//Used to display e.g. _ _ A _ E _ E R or something
	private String partialWord;
	
	//the letter that was just guessed by the user. User for displaying on the client side. 
	private String letterJustGuessed;
	
	//for multiplayer, the number of players left to join the game
	public int numOfUsersToJoin; //public so easier
	
	//the name of the player taking the turn right now
	public String currentTurnPlayerName;
	public String currentTurnGuess; //what is the letter/word that is being guessed right now
	
	public String winnerName; //name of the winner of this game
	
	public String loserName; //name of the player who guessed a wrong word and lost
	
	//Constructor: The message at the very least must have a type
	public Message(Type t) {
		this.type = t;
	}
	
	
	//Other players' info to display, when this player just joined a game OR another user just the game this user is in
	//e.g. otherUsernames[0], otherWins[0], otherLosses[0] are respective stats of the first player to be displayed
	public List<String> otherUsernames = new ArrayList<String>();
	public List<Integer> otherWins = new ArrayList<Integer>();
	public List<Integer> otherLosses = new ArrayList<Integer>();
	
	//Called by client side when a new user joins, displays the other user(s) info
	public void displayOtherUserInfo() {
		if (!otherUsernames.isEmpty()) { //just in case, but it shouldn't be empty at the time this function is called
			for (int i=0; i<otherUsernames.size(); i++) {
				System.out.println("User " + otherUsernames.get(i) + " is in the game.");
				System.out.println(otherUsernames.get(i) + "'s Record");
				System.out.println("--------------");
				System.out.println("Wins - " + otherWins.get(i));
				System.out.println("Losses - " + otherLosses.get(i));
				System.out.println("");
			}
		}
		
		//clear the above 3 lists after you're done, as you don't want to print them out the next time, 
		//	cuz a new player might join and their info will be added to the above lists, then the old info will be printed again
		otherUsernames.clear();
		otherWins.clear();
		otherLosses.clear();
	}
	
	public void displayOtherUsersRecordOnly() {
		if (!otherUsernames.isEmpty()) {
			for (int i=0; i<otherUsernames.size(); i++) {
				System.out.println(otherUsernames.get(i) + "'s Record");
				System.out.println("--------------");
				System.out.println("Wins - " + otherWins.get(i));
				System.out.println("Losses - " + otherLosses.get(i));
				System.out.println("");
			}
		}
		
		otherUsernames.clear();
		otherWins.clear();
		otherLosses.clear();
	}
	
	//getters and setters for everything
	
	public void setType(Type t) {
		this.type = t;
	}
	public Type getType() {
		return type;
	}
	
	public void setUsername(String u) {
		this.username = u;
	}
	public String getUsername() {
		return username;
	}
	
	public void setPassword(String pw) {
		this.password = pw;
	}
	public String getPassword() {
		return password;
	}
	
	public void setWins(int w) {
		this.wins = w;
	}
	public int getWins() {
		return wins;
	}
	
	public void setLosses(int l) {
		this.losses = l;
	}
	public int getLosses() {
		return losses;
	}
	
	public void setAnswerString(String s) {
		this.answerString = s;
	}
	public String getAnswerString() {
		return answerString;
	}
	
	public void setGameName(String s) {
		gameName = s;
	}
	public String getGameName() {
		return gameName;
	}
	
	public void setSecretWord(String w) {
		secretWord = w;
	}
	public String getSecretWord() {
		return secretWord;
	}
	
	public void setWordLength(int wl) {
		wordLength = wl;
	}
	public int getWordLength() {
		return wordLength;
	}
	
	public void setGuessesRemaining(int gr) {
		guessesRemaining = gr;
	}
	public int getGuessesRemaining() {
		return guessesRemaining;
	}
	
	public void setPartialWord(String partial) {
		partialWord = partial;
	}
	public String getPartialWord() {
		return partialWord;
	}
	
	public void setLetterJustGuessed(String ljg) {
		letterJustGuessed = ljg;
	}
	public String getLetterJustGuessed() {
		return letterJustGuessed;
	}
}
