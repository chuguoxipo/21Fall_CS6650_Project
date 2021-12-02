CREATE SCHEMA IF NOT EXISTS Skier;
USE Skier;

DROP TABLE IF EXISTS LiftRide;

CREATE TABLE LiftRide (
	SkierID INT NOT NULL ,
    LiftID INT NOT NULL ,
    Time INT NOT NULL ,
    ResortID INT NOT NULL ,
    SeasonID VARCHAR(255) NOT NULL ,
    DayID VARCHAR(255) NOT NULL ,
    CONSTRAINT pk_LiftRide_SkierID PRIMARY KEY (SkierID)
);