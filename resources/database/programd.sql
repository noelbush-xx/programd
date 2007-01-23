-- ----------------------------------------------------------
-- Program D database schema

-- ----------------------------------------------------------
-- Basic types

CREATE TABLE `bots` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(128) NOT NULL,
  PRIMARY KEY  (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `files` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `path` VARCHAR(512) NOT NULL,
  `last_loaded` datetime NOT NULL,
  PRIMARY KEY  (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1  */;

CREATE TABLE `nodes` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY  (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `templates` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `text` TEXT,
  PRIMARY KEY  (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1  */;

CREATE TABLE `users` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `password` VARCHAR(128) default NULL,
  PRIMARY KEY  (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

-- ----------------------------------------------------------
-- Relationships

CREATE TABLE `bot_file` (
  `bot_id` INT(11) NOT NULL,
  `file_id` INT(11) NOT NULL,
  KEY `bot_id` (`bot_id`),
  KEY `file_id` (`file_id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `bot_user` (
  `bot_id` INT(11) NOT NULL,
  `user_id` INT(11) NOT NULL
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `botidnode_file` (
  `botidnode_id` INT(11) NOT NULL,
  `file_id` INT(11) NOT NULL,
  KEY `botidnode_id` (`botidnode_id`),
  KEY `file_id` (`file_id`),
  CONSTRAINT `botidnode_file_ibfk_1` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`),
  CONSTRAINT `botidnode_file_ibfk_2` FOREIGN KEY (`botidnode_id`) REFERENCES `nodes` (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `edges` (
  `from_node_id` INT(11) NOT NULL,
  `label` TEXT,
  `to_node_id` INT(11) NOT NULL,
  KEY `from_node_id` (`from_node_id`),
  KEY `to_node_id` (`to_node_id`),
  KEY `from_node_id_to_node_id` (`from_node_id`,`to_node_id`),
  KEY `label` (`label`(255)),
  KEY `from_node_id_label` (`from_node_id`,`label`(255))
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `exchanges` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `user_id` INT(11) NOT NULL,
  `bot_id` INT(11) NOT NULL,
  `input` TEXT NOT NULL,
  `response` TEXT NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `user_id` (`user_id`),
  KEY `bot_id` (`bot_id`),
  CONSTRAINT `exchanges_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `exchanges_ibfk_2` FOREIGN KEY (`bot_id`) REFERENCES `bots` (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1  */;

CREATE TABLE `file_node` (
  `file_id` INT(11) NOT NULL,
  `node_id` INT(11) NOT NULL,
  PRIMARY KEY  (`node_id`),
  KEY `file_id` (`file_id`),
  CONSTRAINT `file_node_ibfk_1` FOREIGN KEY (`node_id`) REFERENCES `nodes` (`id`),
  CONSTRAINT `file_node_ibfk_2` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `node_template` (
  `node_id` INT(11) NOT NULL,
  `template_id` INT(11) NOT NULL,
  PRIMARY KEY  (`node_id`),
  KEY `template_id` (`template_id`),
  CONSTRAINT `node_template_ibfk_1` FOREIGN KEY (`template_id`) REFERENCES `templates` (`id`),
  CONSTRAINT `node_template_ibfk_2` FOREIGN KEY (`node_id`) REFERENCES `nodes` (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

CREATE TABLE `predicates` (
  `user_id` INT(11) NOT NULL,
  `bot_id` INT(11) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `value` TEXT NOT NULL,
  UNIQUE KEY `unique_predicates` (`user_id`,`bot_id`,`name`),
  KEY `user_id` (`user_id`),
  KEY `bot_id` (`bot_id`),
  CONSTRAINT `predicates_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `predicates_ibfk_2` FOREIGN KEY (`bot_id`) REFERENCES `bots` (`id`)
) /* ENGINE=InnoDB DEFAULT CHARSET=latin1 */;

