CREATE TABLE `users` (
  `userid` varchar(128) default NULL,
  `password` varchar(128) default NULL,
  `botid` varchar(128) default NULL,
  KEY `userid` (`userid`)
);
CREATE TABLE `predicates` (
  `userid` varchar(128) default NULL,
  `botid` varchar(128) default NULL,
  `name` varchar(128) default NULL,
  `value` blob,
  UNIQUE KEY `unique_predicates` (`userid`,`botid`,`name`)
);