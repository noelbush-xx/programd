Place all your AIML bots in this directory.

Example to make a bot 'Alice':
* Create a directory with the name 'Alice'
* Modify the 'server.engine.bot=' line in the SERVER.properties so that it says
  server.engine.bot=Alice
* Either load an aiml set into this directory or start a bot from scratch.

How to start a bot from scratch:
* Copy the file called Startup.aiml to your bot directory. This is contained in the
  standard aiml file set.

* Edit all of the <property> tags to your liking.

* Edit the lines saying <learn> to point to your own aiml files. You can add
  more files with additional <learn></learn> tags