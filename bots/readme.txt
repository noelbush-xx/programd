Place all your AIML sets in this directory.

Example: to use an AIML set called 'Sue':
* Create a directory with the name 'Sue'
* Put a startup.xml file in the Sue directory (you can copy the example from
  "./bots/standard"
* Modify the 'programd.startup=' line in the server.properties file so that it says
  programd.startup=bots/Sue/startup.xml
* Either load an aiml set into the ./bots/Sue directory or start a bot from scratch.

The bot loading process follows these rules:
1. The file pointed to by programd.startup is used to configure the bot.
2. If this file is a "programd-startup" file, bot predicate values, substitutions, and
   load-time learn commands are processed from this file