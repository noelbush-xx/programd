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
There is already a subdirectory in the "./bots" directory called "standard".  This
directory contains one file called "startup.xml".

STEP 6.
In your ProgramD root directory, the file "server.properties" to make sure
points programd.startup= points to "./bots/standard/startup.xml".  The startup.xml
file, in turn, is set to load any aiml files that are in the "./bots/standard"
subdirectory.  You can keep this setting, or create your own subdirectory in "./bots",
copy the "startup.xml" file there.

STEP 7.
Download an AIML set (such as the Standard AIML set) and place it into "./bots/standard",
or into the new subdirectory of "./bots" that you created.

STEP 8.
Edit the "startup.xml" file to personalize your bot, if desired.

STEP 9.
Start your bot by executing "run.bat" (Windows) or "server.sh" (*nix), and enjoy.


Thomas Ringate
(amendments by Noel Bush)
