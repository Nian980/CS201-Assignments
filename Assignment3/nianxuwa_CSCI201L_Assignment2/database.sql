DROP DATABASE IF EXISTS nianxuwa_Database;
CREATE DATABASE nianxuwa_Database; 
USE nianxuwa_Database;

-- Note: Generally avoid SQL keywords such as user and event
CREATE TABLE Users (
	userID INT(11) PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(50) NOT NULL,
    fullname VARCHAR(50) NOT NULL,
    firstname VARCHAR(50) NOT NULL,
    imglink VARCHAR(200) NOT NULL
);

CREATE TABLE Follow (
	followID INT(11) PRIMARY KEY AUTO_INCREMENT,
    userID INT(11) NOT NULL,
    friendID INT(11) NOT NULL,
    FOREIGN KEY fk1(userID) REFERENCES Users(userID),
    FOREIGN KEY fk2(friendID) REFERENCES Users(userID)
);

CREATE TABLE EventTable (
	eventID INT(11) PRIMARY KEY AUTO_INCREMENT,
    userID INT(11) NOT NULL,
	eventName VARCHAR(100) NOT NULL,
	startDateTime VARCHAR(50) NOT NULL,
    endDateTime VARCHAR(50) NOT NULL,
    FOREIGN KEY fk3(userID) REFERENCES Users(userID)
);

-- In EventTable, userID will be the user that has a copy of that event


-- CREATE TABLE UserEvents (
-- 	userEventID INT(11) PRIMARY KEY AUTO_INCREMENT,
--     userID INT(11) NOT NULL,
--     eventID INT(11) NOT NULL,
--     FOREIGN KEY fk3(userID) REFERENCES Users(userID),
--     FOREIGN KEY fk4(eventID) REFERENCES EventTable(eventID)
-- );
