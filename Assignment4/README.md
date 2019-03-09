CSCI201 Assignment 4-5


For JDBC connection, I used use mysql-connector-java-5.1.46

The database.sql file in the src folder is required to build the database called "Hangman_db".


When a prompt requires a yes or no answer, only "Yes", "yes", "YES", "y", or "Y" is recognized by the program as yes. 
Typing anything else is interpreted as no. 

For muliplayer games, all the user records will be displayed at the end of the game. If a player loses in the middle of a game, all the other players will simply be notified that this player has lost, and that player's stats/records will only be displayed at the end of the game, along with the records of other users in the game.  

For my hangman game, if a user guesses the last letter(s), they essentially guessed the word and hence will win the game. 

Guessing a wrong word will also result in a deduction of incorrect guesses left. This is clarified in Piazza. 