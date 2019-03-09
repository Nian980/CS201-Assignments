package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import data.Message;
import data.Message.Type;

public class Game {
	
	public enum GameState {
		LetterAlreadyGuessed,
		WrongGuess,
		CorrectGuess
	}

	//private HangmanServer hs; //actually don't need this?
	
	//Name of the game and players
	private String gameName;
	private int maxPlayers; //total number of players this game can take, 1-4
	
	private int currentNumOfPlayers; //how many players are currently in the game
	
	public String gameCreatorName; //just to conveniently store who created this game, for displaying purposes
	
	//Store the players usernames. Make this thread safe (in lecture pdfs)
	public List<HangmanServerThread> playerThreads = Collections.synchronizedList(new ArrayList<HangmanServerThread>());
	
	
	//For multiplayer, from 0 to 3 depending on num of players, increment to index of player's hst for their turn
	public int playerTurnIndex = 1; //start from 1 as we do 0 (game creator's) turn initially
	//REMEMBER at end of turn or somewhere, check if the player is still in the game and stuff, 
	//         skip over him if he lost and is spectating
	
	//in case all players guessed wrongly in the middle of the game and lose and so game will then end
	public int numberOfLostPlayers = 0;
	
	//The secret word for this game
	private String secretWord; 
	private int wordLength;
	
	//Attemps at guessing: 
	//number of letters still left blank
	private int lettersLeft; //not good, because you can guess same letter again and again
	//All of the indices of matched letters in the word
	private Set<Integer> indicesMatched = Collections.synchronizedSet(new HashSet<Integer>());
	//how many guesses left players can make about this game
	private int remainingGuesses;
	
	
	//Constructor
	public Game(String n) {
		gameName = n;
		//some defaults
		maxPlayers = 1;
		wordLength = 0;
		lettersLeft = 0;
		remainingGuesses = 7;
	}
	
	
	/* This is for the user that just joins the game to get user info about other users */
	public void getOtherPlayersInfo(Message m, HangmanServerThread hst) {
		//this is for the player that just joined the game
		for (HangmanServerThread thread : playerThreads) { //for each user's thread in this game
			if (thread != hst) {
				//do not add this own player's info, as you don't want to display it
				//add the current iteration thread's username, wins and losses to this new player
				m.setType(Type.JustJoinedOtherPlayersInfo);
				m.otherUsernames.add(thread.thisUsername); 
				m.otherWins.add(thread.thisWins); 
				m.otherLosses.add(thread.thisLosses);
			}
		}
		hst.sendMessage(m); //after message populated with info, send to the just joined user
	}
	
	/* Called each time a user guessed a correct letter to see if they have guessed the last letter(s), hence winning game. */
	public boolean allLettersGuessed() {
		if (indicesMatched.size() == wordLength) {
			return true;
		}
		return false;
	}
	
	//getters and setters
	
	public String getGameName() {
		return gameName;
	}
	
	public void setMaxPlayers(int n) {
		maxPlayers = n;
	}
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public int getCurrentNumOfPlayers() {
		return currentNumOfPlayers;
	}
	
	public boolean addPlayer(HangmanServerThread hst) {
		if (currentNumOfPlayers < maxPlayers) { //if there is space for new players
			currentNumOfPlayers++;
			playerThreads.add(hst);
			return true;
		}
		return false;
	}
	
	public boolean isSinglePlayer() {
		if (maxPlayers == 1) {
			return true;
		}
		return false;
	}
	
	public void setSecretWord(String sw) {
		secretWord = sw;
		wordLength = secretWord.length();
		lettersLeft = wordLength;
	}
	public String getSecretWord() {
		return secretWord;
	}
	public int getWordLength() {
		return wordLength;
	}
	
	/* Used to display what positions a letter are in the word */
	//Stores the list of positions where the correct letter is located in
	private List<Integer> tempIndices = new ArrayList<Integer>();
	//gets that list of indices
	public String getLetterIndicesAsString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<tempIndices.size(); i++) {
			sb.append(" " + tempIndices.get(i));
		}
		return sb.toString();
	}
	
	/* When you guess a letter, there are 3 possible results: 
	 * The letter is already guessed. - LetterAlreadyGuessed
	 * The letter is not in the secret word.  - WrongGuess
	 * The letter is in the secret word. - CorrectGuess
	 * Do I need to make this synchronized so only one thread can guess a letter at a time
	 * */
	public GameState guessLetter(Character letter) {
		
		tempIndices.clear(); //clear it every time you guess a letter
		
		boolean anyMatch = false; //whether the letter you guessed is in the word at all
		for (int i=0; i<wordLength; i++) { //go through entire word, check for every spot the letter could be in
			if (secretWord.charAt(i) == letter) { //if the guessed letter matches
				anyMatch = true; 
				lettersLeft--; //one less letter to guess... Do I need this?
				//now add the index to the matched indices, so we know which indices we can replace with letters
				tempIndices.add(i);
				indicesMatched.add(i);
			}
		}
		
		if (anyMatch) { //if secret word contains the letter
			return GameState.CorrectGuess;
		}
		else {
			remainingGuesses--; //minus 1 guess for incorrect guesses
			return GameState.WrongGuess;
		}
	}
	
	/* returns the partial word with all its underscores */
	public String getPartialWord() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<wordLength; i++) {
			if (indicesMatched.contains(i)) {
				sb.append(secretWord.charAt(i));
				sb.append(" ");
			}
			else {
				sb.append("_ ");
			}
		}

		return sb.toString().toUpperCase();
	}
	
	/* Attempt to guess the whole word */
	public boolean guessWord(String wordToGuess) {
		if (wordToGuess.equals(secretWord)) { //if they are equal by value
			return true;
		}
		remainingGuesses--;
		return false;
	}
	
	/* Called to check if the word is guessed completely. Don't think I ever use this... */
	public boolean wordGuessed() {
		if (lettersLeft <= 0) {
			return true;
		}
		return false;
	}
	
	public int getRemainingGuesses() {
		return remainingGuesses;
	}
}
