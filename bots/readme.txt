Place all your AIML bots in this directory.

Example to make a bot 'Alice':
* Create a directory with the name 'Alice'
* Modify the 'server.engine.bot=' line in the SERVER.properties so that it says
  server.engine.bot=Alice
* Either load an aiml set into this directory or start a bot from scratch.

How to start a bot from scratch:
* Make a file called Startup.aiml and copy this into it:

<!-- begin startup.aiml file -->
<?xml version="1.0"?>
<!DOCTYPE aiml PUBLIC "-//Artificial Intelligence Markup Language 1.0//EN" "http://www.alicebot.org/dtd/aiml10.dtd">
<aiml version="1.0">
<!-- Alicebot.Net Startup File -->
<category>
  <pattern>STARTUP</pattern>
  <template>
    <learn>filename.aiml</learn>
  </template>
</category>
</aiml>
<!-- end startup aiml file -->

* Change the line saying <learn> to point to your own aiml file. You can add
  more files with additional <learn></learn> tags