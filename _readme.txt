This is a quick reference to the steps needed to get ProgramD up and running.

It is assumed that you have already installed the JAVA JDK. If you do not, then you must
install it, and make sure that it is working.

SETP 1.
Create a directory on your system for your bot.
An example would be "md c:\ProgramD"

STEP 2.
UNZIP the entire ProgramD distribution into this directory, making sure you
create all the sub directories.  WINZIP will do this for you.

STEP 3.
If you have downloaded the "binary" distribution rather than the "source" distribution
skip ahead to STEP 6.

STEP 4. 
Edit the "build.bat" file in directory .\build so it points to your java JDK.
See the instructions in that file how to do this.  Be sure you remove the "rem"
from the set statement after you edit it.

STEP 5.
Execute the build.bat file.  The compile should end with a successful build.  If
it does not, then stop right here, and solve the problem.

STEP 6.
Create a directory in the .\bot directory for your bot.  An example would be
"md c:\ProgramD\bot\standard"

STEP 7.
In your ProgramD root directory, edit the file "server.properties" to make sure
the line "server.engine.bot=standard" points to the directory you created
in STEP 6.  My example was "standard".

STEP 8.
Now download an AIML set, and place it into this directory.  Presently
only the "stardard set" of AIML has the startup.aiml file setup to run with
ProgramD release 4.1.1 or later.

STEP 9.
Edit the "startup.aiml" file to personalize your bot.  This is done by editing the
<property> tags you find at the very start of this file.

The <property> tag is not an AIML 1.0 tag, it is a bot implementation specific
tag used by ProgramD.  It is only valid in the startup.aiml file, and must come
before any <learn> tags.

STEP 10.
Start your bot by executing the "server.bat" file, and enjoy.


Thomas Ringate

