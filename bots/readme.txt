Place all your AIML bots in this directory.

Example to make a bot 'Alice':
* Create a directory with the name 'Alice'
* Modify the 'server.engine.bot=' line in the SERVER.properties so that it says
  server.engine.bot=alice
* Either load an aiml set into the ./bot/alice directory or start a bot from scratch.

The bot loading process follows these rules:
1. The bot AIML is located in the directory pointed to by server.engine.bot= that is
   in the server.properties file. The root directory is ./bot
2. The file "startup.xml" contains all of the bot properties and is found in this directory.
   This file can be moved to your specific bot aiml directory if you like.
   server.engine.startup=startup.xml in server.properties to the pointer to this file.
   Using the default server.properties configuration, you need to have a startup.xml
   file in your AIML directory.  The standard AIML set comes with a generic file.
3. The distributed startup.xml file uses the global learn <learn*</learn> to learn all
   of the AIML files contained in your AIML directory.  If you have a "startup.aiml" file
   to learn your files, you may want to change this one line to something like this.
   <learn>startup.aiml</learn>
   You could also move all of your LEARN tags into startup.xml and delete your old
   startup.aiml file.

How to start a bot from scratch:
* Edit the startup.xml file to your desired values.
* Place your AIML files into the directory you are pointing to with 'server.engine.bot='.
  The bot will load all AIML file that are in this directory.