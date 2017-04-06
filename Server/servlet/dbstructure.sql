-- phpMyAdmin SQL Dump
-- version 3.3.7deb8
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 04, 2017 at 09:15 PM
-- Server version: 5.1.73
-- PHP Version: 5.3.3-7+squeeze23

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `wmp`
--

-- --------------------------------------------------------

--
-- Table structure for table `Found`
--

CREATE TABLE IF NOT EXISTS `Found` (
  `objectId` char(4) NOT NULL,
  `hunterId` char(4) DEFAULT NULL,
  `lat` float DEFAULT NULL,
  `lon` float DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`objectId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Hunter`
--

CREATE TABLE IF NOT EXISTS `Hunter` (
  `id` char(4) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `lat` float DEFAULT NULL,
  `lon` float DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Object`
--

CREATE TABLE IF NOT EXISTS `Object` (
  `id` char(4) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `ObjectProperty`
--

CREATE TABLE IF NOT EXISTS `ObjectProperty` (
  `objectId` char(4) NOT NULL DEFAULT '',
  `property` varchar(255) NOT NULL DEFAULT '',
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`objectId`,`property`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `Simulation`
--

CREATE TABLE IF NOT EXISTS `Simulation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=171 ;

-- --------------------------------------------------------

--
-- Table structure for table `SimulationObject`
--

CREATE TABLE IF NOT EXISTS `SimulationObject` (
  `SimulationID` int(11) NOT NULL,
  `objectId` int(11) NOT NULL,
  PRIMARY KEY (`SimulationID`,`objectId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SimulationObjectLog`
--

CREATE TABLE IF NOT EXISTS `SimulationObjectLog` (
  `Timestamp` bigint(20) NOT NULL,
  `SimulationID` int(11) NOT NULL,
  `ObjectID` int(11) NOT NULL,
  `property` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`Timestamp`,`SimulationID`,`ObjectID`,`property`),
  KEY `SimulationID` (`SimulationID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SimulationObjectProperty`
--

CREATE TABLE IF NOT EXISTS `SimulationObjectProperty` (
  `SimulationID` int(11) NOT NULL,
  `objectId` int(11) NOT NULL,
  `property` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`SimulationID`,`objectId`,`property`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SimulationObjectState`
--

CREATE TABLE IF NOT EXISTS `SimulationObjectState` (
  `SimulationID` int(11) NOT NULL,
  `ObjectID` int(11) NOT NULL,
  `property` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`SimulationID`,`ObjectID`,`property`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SimulationProperty`
--

CREATE TABLE IF NOT EXISTS `SimulationProperty` (
  `SimulationID` int(11) NOT NULL,
  `property` varchar(255) NOT NULL DEFAULT '',
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SimulationID`,`property`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
