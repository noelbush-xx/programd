SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


-- -----------------------------------------------------
-- Table `bot`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bot` ;

CREATE  TABLE IF NOT EXISTS `bot` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `label` VARCHAR(128) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `file`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file` ;

CREATE  TABLE IF NOT EXISTS `file` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `path` VARCHAR(512) NOT NULL ,
  `last_loaded` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `bot_file`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bot_file` ;

CREATE  TABLE IF NOT EXISTS `bot_file` (
  `bot_id` INT(11) NOT NULL ,
  `file_id` INT(11) NOT NULL ,
  INDEX `bot_id` (`bot_id` ASC) ,
  INDEX `file_id` (`file_id` ASC) ,
  CONSTRAINT `fk_bot_file_bot`
    FOREIGN KEY (`bot_id` )
    REFERENCES `bot` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_bot_file_file1`
    FOREIGN KEY (`file_id` )
    REFERENCES `file` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `user` ;

CREATE  TABLE IF NOT EXISTS `user` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(128) NOT NULL ,
  `password` VARCHAR(128) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `bot_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bot_user` ;

CREATE  TABLE IF NOT EXISTS `bot_user` (
  `bot_id` INT(11) NOT NULL ,
  `user_id` INT(11) NOT NULL ,
  INDEX `fk_bot_user_bot1` (`bot_id` ASC) ,
  INDEX `fk_bot_user_user1` (`user_id` ASC) ,
  CONSTRAINT `fk_bot_user_bot1`
    FOREIGN KEY (`bot_id` )
    REFERENCES `bot` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_bot_user_user1`
    FOREIGN KEY (`user_id` )
    REFERENCES `user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `botidnode_file`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `botidnode_file` ;

CREATE  TABLE IF NOT EXISTS `botidnode_file` (
  `botidnode_id` INT(11) NOT NULL ,
  `file_id` INT(11) NOT NULL ,
  INDEX `botidnode_id` (`botidnode_id` ASC) ,
  INDEX `file_id` (`file_id` ASC) ,
  CONSTRAINT `fk_botidnode_file_file1`
    FOREIGN KEY (`file_id` )
    REFERENCES `file` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `node`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `node` ;

CREATE  TABLE IF NOT EXISTS `node` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `edge`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `edge` ;

CREATE  TABLE IF NOT EXISTS `edge` (
  `from_node_id` INT(11) NOT NULL ,
  `label` MEDIUMTEXT NULL DEFAULT NULL ,
  `to_node_id` INT(11) NOT NULL ,
  INDEX `from_node_id_to_node_id` (`from_node_id` ASC, `to_node_id` ASC) ,
  CONSTRAINT `fk_edge_node1`
    FOREIGN KEY (`from_node_id` )
    REFERENCES `node` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_edge_node2`
    FOREIGN KEY (`to_node_id` )
    REFERENCES `node` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `exchange`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `exchange` ;

CREATE  TABLE IF NOT EXISTS `exchange` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `timestamp` DATETIME NOT NULL ,
  `user_id` INT(11) NOT NULL ,
  `bot_id` INT(11) NOT NULL ,
  `input` MEDIUMTEXT NOT NULL ,
  `response` MEDIUMTEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `user_id` (`user_id` ASC) ,
  INDEX `bot_id` (`bot_id` ASC) ,
  CONSTRAINT `fk_exchange_user1`
    FOREIGN KEY (`user_id` )
    REFERENCES `user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_exchange_bot1`
    FOREIGN KEY (`bot_id` )
    REFERENCES `bot` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `file_node`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `file_node` ;

CREATE  TABLE IF NOT EXISTS `file_node` (
  `file_id` INT(11) NOT NULL ,
  `node_id` INT(11) NOT NULL ,
  INDEX `file_id` (`file_id` ASC) ,
  CONSTRAINT `fk_file_node_file1`
    FOREIGN KEY (`file_id` )
    REFERENCES `file` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_file_node_node1`
    FOREIGN KEY (`node_id` )
    REFERENCES `node` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `template`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `template` ;

CREATE  TABLE IF NOT EXISTS `template` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `text` MEDIUMTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `node_template`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `node_template` ;

CREATE  TABLE IF NOT EXISTS `node_template` (
  `node_id` INT(11) NOT NULL ,
  `template_id` INT(11) NOT NULL ,
  INDEX `template_id` (`template_id` ASC) ,
  CONSTRAINT `fk_node_template_node1`
    FOREIGN KEY (`node_id` )
    REFERENCES `node` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_node_template_template1`
    FOREIGN KEY (`template_id` )
    REFERENCES `template` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `predicate`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `predicate` ;

CREATE  TABLE IF NOT EXISTS `predicate` (
  `user_id` INT(11) NOT NULL ,
  `bot_id` INT(11) NOT NULL ,
  `name` VARCHAR(128) NOT NULL ,
  `value` MEDIUMTEXT NOT NULL ,
  UNIQUE INDEX `unique_predicates` (`user_id` ASC, `bot_id` ASC, `name` ASC) ,
  INDEX `user_id` (`user_id` ASC) ,
  INDEX `bot_id` (`bot_id` ASC) ,
  CONSTRAINT `fk_predicate_user1`
    FOREIGN KEY (`user_id` )
    REFERENCES `user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_predicate_bot1`
    FOREIGN KEY (`bot_id` )
    REFERENCES `bot` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
