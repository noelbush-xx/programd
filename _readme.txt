This is a quick reference to the steps needed to get ProgramD up and running.

It is assumed that you have already installed the JAVA JDK. If you do not, then you must
install it, and make sure that it is working.

STEP 1.
UNZIP the ProgramD distribution, making sure you create all the sub directories.
WINZIP will do this for you.

STEP 2.
If you have downloaded the "binary" distribution rather than the "source" distribution
skip ahead to STEP 5.

STEP 3. 
Edit the "build.bat" file in directory .\build so it points to your java JDK.
See the instructions in that file how to do this.  Be sure you remove the "rem"
from the set statement after you edit it.

STEP 4.
Execute the build.bat file.  The compile should end with a successful build.  If
it does not, then stop right here, and solve the problem.

STEP 5.
Create a directory in the .\bot directory for your bot.  An example would be
"md c:\ProgramD\bot\standard".  This is the directory where you will place all
of your AIML files.

STEP 6.
In your ProgramD root directory, edit the file "server.properties" to make sure
the line server.engine.bot= points to the directory you created
in STEP 5.  My example was "standard".

STEP 7.
Now download an AIML set, and place it into this directory.

STEP 8.
Edit the "startup.xml" file to personalize your bot.  This file is found in the
standard AIML set.  As a convenience, a copy of this file is also included in
the .\bot directory.  If you are using your own AIML files, you may want to
copy this file into your AIML directory, and edit it to point to your initial,
main, startup AIML file.

The <property> tag is not an AIML 1.0 tag, it is a bot implementation specific
tag used by ProgramD.  It is only valid in the startup.xml file, and must come
before any <learn> tags.
With release 4.1.2 the bot has it's personality established with this XML
<property> tag.

STEP 9.
Start your bot by executing the "server.bat", or "run.bat" file, and enjoy.


Thomas Ringate

