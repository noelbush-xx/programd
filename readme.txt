
Getting Started With Program D

author: Noel Bush
revised from a document by Kim Sullivan, with contributions from Zayn Blore and 
Dennis Daniels.
Last updated: 23 April 2005

Please check http://aitools.org for more documentation, including updates to 
this document.

    * 0. Preparation
    * 1. Installation
    * 2. Configuration
    * 3. First Startup

0. Preparation
0.0. Download Program D

Go to http://aitools.org/downloads and get the latest Program D distribution.

You don't necessarily need the distribution that includes source code--you can 
get a pre-compiled version that will work on any platform for which there's a 
compatible Java 2 (version 1.5 or later) JRE or SDK. So unless you want to 
modify and rebuild the project, we suggest downloading d-bin-current.tar.gz or 
d-bin-current.zip.
0.1. Get the Java Runtime (or SDK)

You need to download and install a Java 2 version 1.5 compatible JVM. Examples 
are the Sun JRE (Java Runtime Edition) or SDK (Software Development Kit). The 
JRE is much smaller than the SDK (~16MB as compared with ~44!!!). You only need 
the SDK if you want to rebuild the program, or want to develop Java programs of 
your own. You can download the necessary software and find installation 
instructions at http://java.sun.com/j2se/1.5.0/download.jsp.
0.2. Get Some AIML

Also at http://aitools.org/aiml-sets, you'll find several choices of 
freely-available AIML. Pick one and download it.
1. Installation
1.0. Java

We won't cover installation instructions for the JRE/Java SDK here. We'll just 
assume that you've followed the instructions. In the following, we'll refer to 
the directory where you installed Java as JAVA_HOME. (If you use Windows, this 
might be something like C:\jdk1.5.0_02. If you're using Linux, this might be 
/usr/java/jdk1.5.0_02.)

You should at least be able to type "java -version" at a command line (syntax 
may vary) and get a response something like:

java version "1.5.0_02"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_02-b09)
Java HotSpot(TM) Client VM (build 1.5.0_02-b09, mixed mode)

If you get some kind of error like "command not found", check that your system 
path settings are correct. Or, just try "JAVA_HOME /java -version" (Linux) or 
"JAVA_HOME \java.exe -version" (Windows), substituting your actual JAVA_HOME 
path.
1.1. Program D

If you're upgrading from a previous version, please check the release notes, 
since some configuration files may have changed.

Unzip/untar the Program D download in a convenient location. On Linux, we 
suggest /usr/local/ProgramD; on Windows, the root directory (C:\, perhaps) is 
as good as anything. The unzipping/untarring process will create a directory 
called ProgramD that will contain all the program files.

Linux users may type:

tar xvjf programd-4.5rc1-bin.tar.bz2

Windows 95-2000 users could try a free unzip utility such as 7-Zip. Windows XP 
users will find that XP is able to open the zip file and guide you through 
extracting it using a "wizard".

We'll refer to the root directory created by the unzip/untar (such as 
/usr/alice/ProgramD) as PROGRAMD.
1.1.1 Compile (source downloads only)

If you downloaded the binary version of Program D (programd-4.5rc1-bin.tar.bz2 
or programd-4.5rc1-bin.zip), you can skip this step.

If you downloaded the source version of Program D (programd-4.5rc1-src.tar.bz2 
or programd-4.5rc1-src.zip), you will need to compile the code before you can 
run it. This is very easily done by running the build shell script (Linux) or 
build.bat batch file (Windows). Here's what will happen:

[noel@emery ProgramD]$ chmod +x bin/build
[noel@emery ProgramD]$ bin/build

JAVA_HOME is not set in your environment.
I have set JAVA_HOME to "/usr/java/jdk1.5.0_02".
Please consider setting your JAVA_HOME environment variable.

Buildfile: /home/noel/d-test/ProgramD/conf/build.xml

init:
     [echo] Building Program D 4.5rc1....

prepare:
    [mkdir] Created dir: /home/noel/d-test/ProgramD/build.tmp

prepare-src:

compile:
    [javac] Compiling 186 source files to /home/noel/d-test/ProgramD/build.tmp
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.

package:
      [jar] Building jar: /home/noel/d-test/ProgramD/lib/programd-main.jar
      [jar] Building jar: /home/noel/d-test/ProgramD/lib/programd-jetty.jar
      [jar] Building jar: /home/noel/d-test/ProgramD/lib/programd-rhino.jar

BUILD SUCCESSFUL
Total time: 4 seconds

1.1.2 Verify Installation

The Program D distribution includes the set of test cases used to check AIML 
interpreter functionality, and as shipped, it is configured to load this small 
AIML set. Therefore, you can verify the installation "out of the box" if you 
wish, by running the simple console immediately. See First Startup for tips on 
running the simple console. If all is well, you should see something like this:

[21:54:58] Starting Program D version 4.5rc1.
[21:54:58] Using Java VM 1.5.0_02-b09 from Sun Microsystems Inc.
[21:54:58] On Linux version 2.6.11-1.14_FC3smp (i386)
[21:54:58] Predicates with no values defined will return: "undefined".
[21:54:58] Initializing FlatFileMultiplexor.
[21:54:58] Starting up the Graphmaster.
[21:54:58] Configuring bot "yourbot".
[21:54:59] Loaded 287 input substitutions.
[21:54:59] Loaded 19 gender substitutions.
[21:54:59] Loaded 9 person substitutions.
[21:54:59] Loaded 60 person2 substitutions.
[21:54:59] 91 categories loaded in 0.602 seconds.
[21:54:59] The AIML Watcher is not active.
[21:54:59] JavaScript interpreter not started.
[21:54:59] emery> CONNECT : * : * : yourbot
[21:54:59] Match: CONNECT : * : * : yourbot
[21:54:59] Filename: "../resources/testing/testcases.aiml"
[21:54:59] Response 1 in 9 ms. (Average: 9.0 ms.)
[21:54:59] Interactive shell: type "/exit" to shut down; "/help" for help.

The fourth line from the bottom ("") indicates that a category from the 
testcases.aiml set has been matched. Now that you know you have a working 
installation, please skim through the rest of these notes and decide what else 
you want to configure.
1.2. AIML

We suggest creating a subdirectory called "aiml" in your PROGRAMD directory, 
and then arranging your AIML files there in a way that makes sense to you. 
You'll need to unzip/untar as above.

Note that, starting with version 4.5 of Program D, your AIML must declare the 
AIML namespace URI. The AIML set you download may not yet have been properly 
formatted in this way; if this is so, you should make the changes yourself. The 
namespace declaration is made on the opening tag of the root element of the 
document (the <aiml> tag). At minimum, you should add this declaration:

xmlns="http://alicebot.org/2001/AIML-1.0.1"

Also, if your AIML contains HTML markup (which is common in many AIML sets), 
you will need to do two things:

   1.

      Declare the HTML or XHTML namespace and assign it to a prefix. This 
should also be done on the root element. This may look like:

      xmlns:html="http://www.w3.org/1999/xhtml"
   2.

      Add the prefix you associated with the HTML/XHTML namespace to all 
HTML/XHTML elements in the document. This is less onerous than it might sound 
if you use a shell script or batch file. An example script that processes the 
"AAA" AIML set is included with this release in the resources/scripts 
subdirectory.

      For instance, every instance of

      <br />

      should become

      <html:br/>

Additionally, please note that your HTML/XHTML markup (all of the markup, in 
fact) must be valid XML—in other words, an opening tag must have a 
corresponding closing tag (<some-element></some-element>) , and an empty 
element ("atomic tag") must be properly notated (<some-element/>). (This will 
all be obvious to you if you are using contemporary standards and 
standards-based tools, but several years ago the notion of well-formed XML was 
not something universally familiar, and so some AIML sets still include 
orphaned tags and other detritus that needs to be cleaned up.)

So, starting from a root element that looks simply like:

<aiml>

you will wind up with something like:

<aiml version="1.0.1" xmlns="http://alicebot.org/2001/AIML-1.0.1"
      xmlns:html="http://www.w3.org/1999/xhtml">

optional step

For purposes of editing AIML locally, you would be well advised to include an 
xsi:schemaLocation notation pointing to a local copy of the AIML schema. If you 
do not, then your XML editing application will attempt to connect to the 
aitools.org server to grab the schema whenever it needs to validate AIML, which 
is probably undesirable (for you and for us). (Note that Program D will not 
connect to the aitools.org server—it knows to look at the local copy of the 
schema.) Just as you would keep a copy of the XHTML schema locally for 
validating XHTML, you want to keep and point to a copy of the AIML schema. A 
copy of the AIML schema is included in the Program D distribution. There are 
two ways of pointing to it. The most direct approach is to add the following, 
again to the root element:

xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://alicebot.org/2001/AIML-1.0.1 
resources/schema/AIML.xsd"

However, this has the disadvantage that it hard-codes a local file path in your 
AIML file. (It is also technically incorrect, since the second part of the 
schemaLocation is supposed to be a "canonical" URI.) So, if you are using an 
XML system that supports the use of XML Catalogs, you can enter the following:

xsi:schemaLocation="http://alicebot.org/2001/AIML-1.0.1 
http://aitools.org/aiml/schema/AIML.xsd"

and then, in your catalog file, include an entry like this:

<system systemId="http://aitools.org/aiml/schema/AIML.xsd" 
uri="./schema/AIML.xsd"/>

Again, an example catalog file is included in the Program D distribution.
2. Configuration

There's a lot to configure with Program D. What's needed for you really depends 
on how you want to use the program. If you don't care about running a web 
server, you don't need to bother with the "HTTP Server" section. If you don't 
care about using Program D with chat interfaces like AOL Instant Messenger or 
IRC, you won't be interested in the information about "Chat Listeners". If you 
don't care about high-volume applications, or don't really want to do 
database-based log analysis, you can skip the "Database" section.
2.0. Bots

Program D lets you configure an unlimited number of bots that can run at the 
same time. The bots are configured in the PROGRAMD/conf/bots.xml file. (In 
previous releases, this file was called startup.xml.)

Open the bots.xml file. Notice that the root element is called <bots>. Inside 
<bots>, we place one or more <bot> elements. These <bot> elements are not the 
same as the AIML tag of the same name.

Each <bot> element has two important attributes: id and enabled. The first one 
assigns an identifier, which should be unique, for the bot. The identifier will 
be used internally by the engine and will be written to some log resources. The 
enabled attribute should have either of the values "true" or "false". If the 
value is "true", then Program D will try to load that bot when the server 
starts up. Switching enabled to "false" is an easy way to quickly turn off a 
bot configuration that you don't want to use (although a restart is required).

Within the <bot> element we define bot properties, listeners, default 
predicates, substitutions, sentence-splitters and learn directives.
2.0.0. Bot Properties

Bot properties are predicates that cannot be changed during the runtime life of 
the bot, but which can be included in AIML patterns for matching. A common 
property to define for a bot is "name". Bot properties are defined in 
individual <property> elements inside a bot's <properties> element, as in the 
example:

<property name="master" value="Joe Schmoe"/>

This associates the bot property name "master" with the value "Joe Schmoe".

(You can also define bot properties in a separate file, and point to that file 
using an "href" attribute on the <properties> element. See other elements 
inside <bot> in the example startup.xml to understand how this is done.)

Properties don't mean anything unless your AIML uses them. You can display the 
value of a bot property inside an AIML template by using the form <bot 
name="property-name"/>.
(2.0.1. Listeners)

Listeners are discussed in more detail below.
2.0.2. Default Predicates

Default predicates can be thought of as your bot's "assumptions" about new 
users. While AIML allows you to use <set name="predicate-name"> ...something... 
</set> and <get name="predicate-name"/> forms without any sort of 
"declaration", Program D does allow you to set default values to associate with 
any predicate names you wish, so that you have more control over what is 
returned by <get name="predicate-name"/> if a corresponding <set> has not yet 
happened for a given user.

The <predicates> section, if it exists, may directly include one or more 
<predicate> children, or it may use an "href" attribute, as in the example, 
that points to another file where the predicates are defined.

You can also use <predicate> elements to mark a predicate as 
return-name-when-set. For instance, this predicate definition is included in a 
sample file:

<predicate name="he" default="somebody" set-return="name"/>

This means that when <set name="he"> ... </set> is included in a template, the 
name of the predicate, "he", will be displayed, rather than whatever value is 
associated with the name by the <set>.
2.0.3. Substitutions

Substitutions have several different purposes, depending on their type. Input 
substitutions contribute to the process of input normalization. person 
substitutions provide macros for transformations by the <person> tag; likewise 
person2 and gender apply to the <person2> and <gender> tags, respectively.

Starting in version 4.5, the find attributes of substitutions are parsed as 
regular expressions. This lends a great deal more power and precision to the 
input normalization step, and to the gender, person and person2 processors. For 
a guide to the regular expression syntax that is available, see the JDK 
documentation.

Each individual substitution specification, regardless of whether it is inside 
an <input>, <gender>, <person> or <person2>, takes the same form as this 
example from conf/substitutions.xml:

<substitute find="\bbecasue\b" replace="because"/>

This means that, when this substitution is applied, each instance of the 
separate word "becasue" will be replaced with "because". (This is an example of 
typo correction in Program D.)

Note the use of the \b marker, which is regular expression notation for "word 
boundary". In previous versions of Program D, you were advised to pad find and 
replace strings with spaces, as a way of approximating word boundary matching. 
This is no longer advised, and in fact will not work properly now that regular 
expression support is included.
2.0.4. Sentence-Splitters

Sentence-splitters, as described in the AIML spec, are:

...heuristics applied to an input that attempt to break it into "sentences". 
The notion of "sentence", however, is ill-defined for many languages, so the 
heuristics for division into sentences are left up to the developer.

Since sentence-splitters are applied to the input after substitution 
normalizations, they can be more general rules. The entire collection of 
example sentence-splitters shipped with Program D is:

<sentence-splitters>
    <splitter value="."/>
    <splitter value="!"/>
    <splitter value="?"/>
    <splitter value=";"/>
</sentence-splitters>

As with some other examples, these are defined in a separate file that is 
pointed to by the "href" attribute on the <sentence-splitters> element.
2.0.5. Learn Directives

Learn directives function identically to the <learn> element in AIML.

You can use simple "glob"-like wildcard patterns for specifying files to load. 
For example, the shipped startup.xml file has this single learn directive:

<learn>../aiml/standard/*.aiml</learn>

The path specification is relative to the current file, so this indicates that 
within a directory reached by going one level up (to the main PROGRAMD 
directory), and then into a directory called "aiml", and from there into a 
subdirectory called "standard", the program should load all files that end in 
".aiml". Note that files containing AIML are not required to have this or any 
particular suffix; also, if you direct the program to learn files that do not 
contain any AIML, you will receive a warning but nothing will be loaded from 
that file.
2.1. HTTP Server

If you want to be able to talk to your bot through a web interface, you'll be 
interested to know that Program D ships with a fully optional "servlet" 
implementation. A servlet is an application that runs on a server and interacts 
with users via the web. As an example we use Program D with Jetty, an open 
source HTTP server and servlet container. It is possible to integrate Program D 
with other servlet containers, or other application frameworks entirely. Here, 
purely for example purposes, we describe using the Jetty interface.

The main file you'll be interested in is PROGRAMD /conf/jetty.xml. This is the 
file that is read by the Jetty server when it is invoked by Program D.

The parameter that will probably be of most interest here is the port number. 
This is specified in the place that looks like:

<Set name="Port">2001</Set>

Most web sites use the port numbered 80, which is the port assumed by a web 
browser when you type a URL without a port specification, so you might never 
have even known that there was such a thing as a port in a URL! Having 
different ports allows different server programs running on the same machine to 
answer requests without bothering each other.

For testing purposes, it is probably fine to use the default value of 2001, or 
whatever you prefer (although on some operating systems you can't use a number 
lower than 1024 without special privileges). But for serious applications 
you'll want to deal with this differently, since many firewalls don't allow 
people to access arbitrary ports. The easiest solution to the port problem is 
to use a proxy server (or the proxy feature of a good web server like Apache) 
to allow external users to access your bot using a regular web address that 
doesn't specify a port (i.e., through port 80).

You probably won't care to change the rest of the Jetty configuration file, 
unless you intend to use Jetty to serve content or run other applications 
besides Program D.
2.2. Chat Listeners

In current Program D nomenclature, a "listener" is a program that knows how to 
speak a particular protocol and interpret messages for the bot. For instance, 
the IRCListener understands the popular IRC protocol and allows you to connect 
your bot to any IRC server.

You can enable one or more listeners for each bot. Each listener has its own 
unique configuration parameters, which are specific to the requirements of the 
protocol for which it's designed. You'll find examples of each 
currently-available listener in the PROGRAMD/conf/bots.xml file.

Listeners for a given bot are specified within the <listeners> element, in 
individual <listener> elements. Each <listener> element must have two 
attributes: "type", and "enabled". The value for "type" must correspond with a 
value assigned by the developer to a listener that is somewhere in the Java 
classpath for Program D. The example bots.xml includes an example of each type 
currently known. The value of "enabled" must be "true" or "false", allowing the 
listener to be switched on or off with ease.

The parameters for a listener are specifed in individual <parameter> elements 
within the <listener>. For example:

<parameter name="host" value="ar.chatjunkies.org"/>
<parameter name="port" value="6667"/>
<parameter name="nick" value="programd"/>
<parameter name="channel" value="#bots"/>

These are the parameters set for the example use of the IRCListener, whose type 
string is "ProgramD-IRC".

When you enable a chat listener, you will see its output mixed in with the 
console output. You can also interact with some listeners if they implement the 
"commandable" interface (see below).
2.3. Database

You are not required to install, configure or use a database in order to use 
Program D. The default configuration is entirely based on text files in order 
to make setup quick, painless, and no more resource-intensive than necessary.

However, for heavy-volume situations, and/or for cases where you want to 
collect dialogue in a form that can perhaps be more easily mined, you may wish 
to use a database.

We have done the most extensive testing with MySQL; however, other users have 
connected Program D with various other DBMSes. The major point is that you must 
have a JDBC driver for the database you wish to use. We include the MySQL JDBC 
driver with the Program D distribution for convenience.

If you do want to use a database, you need to configure it properly in the 
PROGRAMD/core.xml file. The relevant part of the file is this:

    <!--
    DATABASE CONFIGURATION
         * This is only meaningful if you are using a database-enabled 
Multiplexor
         * and/or the database-based chat logging.
    -->
    
    <!--Typical mySQL configuration-->
    
    <!--The URL of the database to use. [String: jdbc:mysql:///programdbot]-->
    <entry key="programd.database.url">jdbc:mysql:///programdbot</entry>
    
    <!--The database driver to use. [String: org.gjt.mm.mysql.Driver]-->
    <entry key="programd.database.driver">org.gjt.mm.mysql.Driver</entry>
    
    
    <!--The maximum number of simultaneous connections to the database. [int: 
25]-->
    <entry key="programd.database.connections">25</entry>
    
    <!--The username which with to access the database. [String: programd]-->
    <entry key="programd.database.user">programd</entry>
    
    <!--The password for the database. [String: yourpassword]-->
    <entry key="programd.database.password">yourpassword</entry>

You should set these values to match your database installation. Remember that 
the driver class must be available from the Java classpath.
2.3.0. DBMultiplexor

If you want to store predicates in the database, you must enable the 
DBMultiplexor. This can be done by switching the comment marks in 
PROGRAMD/core.xml so that this:

    <!--The Multiplexor to use. [String: 
org.aitools.programd.multiplexor.FlatFileMultiplexor]-->
    <entry 
key="programd.multiplexor-classname">org.aitools.programd.multiplexor.FlatFileMu
ltiplexor</entry>
    <!--<entry 
key="programd.multiplexor">org.aitools.programd.multiplexor.DBMultiplexor</entry
>-->

becomes this:

    <!--The Multiplexor to use. [String: 
org.aitools.programd.multiplexor.FlatFileMultiplexor]-->
    <!--<entry 
key="programd.multiplexor-classname">org.aitools.programd.multiplexor.FlatFileMu
ltiplexor</entry>-->
    <entry 
key="programd.multiplexor">org.aitools.programd.multiplexor.DBMultiplexor</entry
>

If DBMultiplexor is enabled, Program D will expect to find two tables called 
users and predicates in the configured database. The SQL commands for creating 
these tables are in PROGRAMD/database/db-multiplexor.script.
2.3.1. Database-based Chat Logging

Database-based chat logging stores each exchange with the bot in a database. 
This is useful for monitoring conversations, data mining, etc. To enable this 
feature, you must set:

    <!--Enable chat logging to the database? [boolean: false]
        * Be sure that the database configuration (later in this file) is 
valid.-->
    <entry key="programd.logging.to-database.chat">true</entry>

(in PROGRAMD /core.xml). If this is enabled, Program D will expect to find a 
table called chatlog in the database. The command for creating this table is in 
PROGRAMD/database/db-chatlog.script. Note that if you are logging chats to a 
database, you might want to disable the redundant logging to XML files. You can 
do this by setting:

programd.logging.to-xml.chat=false
2.4. Shell and Console

In its usual configuration, Program D displays information about what it is 
doing while it starts, runs, and shuts down. We call this the "console". Also, 
you can interact with Program D via a simple shell, if desired. Sometimes it is 
simpler to use this shell than to open a web browser or IM program. Both the 
console and shell can be configured to suit your needs. Configuration for both 
is done in the PROGRAMD/console.xml file.
2.5. AIMLWatcher

The AIML Watcher is a nifty feature that watches to see if you change your AIML 
files. This allows you to update AIML without restarting the bot.

As a security feature, the AIML Watcher only watches files that were loaded by 
a learn directive. If you put a new file into a directory with other AIML 
files, that file will not be automatically loaded (but you can load it with the 
/load shell command if you have access to the console and have not disabled the 
shell).

You can enable the AIML Watcher by setting:

    <!--Enable the AIML Watcher? [boolean: false]
        * This will automatically load your AIML files if they are changed.-->
    <entry key="programd.use-watcher">false</entry>

(in PROGRAMD /core.xml). And you can set the frequency with which the AIML 
Watcher checks your files for modification by changing:

    <!--The delay period when checking changed AIML (milliseconds). [int: 2000]
        * Only applicable if the AIML Watcher is enabled.-->
    <entry key="programd.watcher.timer">2000</entry>

(The units are milliseconds. Don't be unreasonable in setting this too low.)
2.6. Other server.properties Configuration Items

The PROGRAMD /core.xml file is documented inline.
3. First Startup
3.0. Starting Program D

Starting with version 4.5, Program D has a much clearer idea of 
"configurations". You will find three different configurations available for 
your use. Additional configurations can easily be created, to customize the 
environment in which you run Program D.

To start Program D in the simplest way, run PROGRAMD /bin/simple-console (*nix) 
or PROGRAMD/bin/simple-console.bat (Windows).

If you're using Windows and you launch the batch file by double-clicking it, 
but there are some configuration problems, the console window might disappear 
before you have time to read the messages. You are advised in this case to open 
a command prompt and run the batch file from there (cd to the right directory, 
type "run").

If you're using Linux, you may need to make the PROGRAMD /bin/simple-console 
file executable by issuing the command:

chmod +x simple-console

Starting the server script should look something like this:

[17:06:40] Starting Program D version 4.5rc1.
[17:06:40] Using Java VM 1.5.0_02-b09 from Sun Microsystems Inc.
[17:06:40] On Linux version 2.6.11-1.14_FC3smp (i386)
[17:06:40] Predicates with no values defined will return: "undefined".
[17:06:40] Initializing FlatFileMultiplexor.
[17:06:40] Starting up the Graphmaster.
[17:06:40] Configuring bot "yourbot".
[17:06:40] Loaded 287 input substitutions.
[17:06:40] Loaded 19 gender substitutions.
[17:06:40] Loaded 9 person substitutions.
[17:06:40] Loaded 60 person2 substitutions.
[17:06:40] 92 categories loaded in 0.564 seconds.
[17:06:40] The AIML Watcher is not active.
[17:06:40] Initializing org.aitools.programd.interpreter.RhinoInterpreter.
[17:06:40] emery> CONNECT : * : * : yourbot
[17:06:40] Match: CONNECT : * : * : yourbot
[17:06:40] Filename: "../resources/testing/testcases.aiml"
[17:06:40] Response 1 in 8 ms. (Average: 8.0 ms.)
[17:06:40] Interactive shell: type "/exit" to shut down; "/help" for help.
[17:06:40] [YourBot] JUDGE> 

If there are any errors while starting up, an explanation will be printed and 
the server will shut down. If you encounter a confusing error message, please 
file a bug report!!!
3.1. Understanding the Console

While the bot is running, information will be printed to the console to tell 
you what's going on.
3.1.1. Shutdown Messages

Before the bot server can shut down, it must stop the http server and must save 
any predicates left in the cache. It also must stop all listeners. There are 
several different ways to shut down, but all of them should cause Program D to 
print information that looks like this:

[17:07:38] Program D is shutting down.
[17:07:38] Shutting down all ManagedProcesses.
[17:07:38] Finished shutting down ManagedProcesses.
[17:07:38] PredicateMaster saving all cached predicates (2)
[17:07:38] Shutdown complete.

"ManagedProcesses" are such things as the http server and listeners. Sometimes 
one or more of these may take a long time to shut down. Unless you are in a 
great hurry, you should allow this process to complete properly. The saving of 
cached predicates should be very fast.
3.2. Using the Shell

The simplest use of the shell is to talk to your bot(s). However, there are 
some other capabilities available to you. All shell commands in Program D are 
preceded by a / (slash) character. If you type "/help" at a shell prompt, you 
can see the list of available commands:

[17:10:55] All shell commands are preceded by a forward slash (/).
[17:10:55] The commands available are:
[17:10:55] /help             - prints this help
[17:10:55] /exit             - shuts down the bot server
[17:10:55] /load filename    - loads/reloads given filename for active bot
[17:10:55] /unload filename  - unloads given filename for active bot
[17:10:55] /bots             - lists loaded bots
[17:10:55] /talkto botid     - switches conversation to given bot
[17:10:55] /who              - prints the id of the current bot
[17:10:55] /files            - lists the files loaded by the current bot
[17:10:55] /roll chatlog     - rolls over chat log
[17:10:55] /roll targets     - rolls over saved targeting data
[17:10:55] /commandables     - lists available "shell commandables" (such as 
listeners)

3.2.0. Loading AIML

The /load command lets you load new AIML files, or reload files (useful if you 
have disabled the AIML Watcher.)
3.2.1. Unloading AIML

The /unload command lets you unload an AIML file that is currently in memory. 
You must unload using the pathname that was originally used to load it, which 
can be a little tricky to get right.
3.2.2. Listing Loaded Bots

You can see which bots are currently loaded with the /bots command.
3.2.3. Talking to a Different Bot

You can switch the bot with whom you are conversing by using the /talkto 
command. You must use a valid bot id as the argument. (The bot id is not 
necessarily the same as the value associated with its "name" property. To see 
bot ids, look at your startup.xml file or use the /bots command.

Each time you switch bots, the connect string will be sent to the bot.

Trying to talk to a botid that doesn't exist will result in an error message.
3.3. Connecting via the Web Interface

If you want to talk to your bot through a web server, use the example 
PROGRAMD/web-server configuration. Once your bot is started, you can interact 
with it through a web browser. If you have not changed the port number (see 
HTTP Server above, then you should get a response when you go to this address:

http://localhost:2001

Note that "localhost" is the name by which your computer knows itself—not only 
in Program D, but in any program that uses TCP/IP (the set of protocols that 
makes the Internet work). You cannot go to another computer and connect to 
yours using the name "localhost". You will need to know the IP address or name 
of the computer where the bot is running, in order to connect from another 
machine. On Windows 95 and 98, you can find this out by running the command:

winipcfg

On Windows NT, 2000 and XP, use:

ipconfig

On *nix, use:

host

Try using the hostname first (it's usually easier to remember); if that causes 
problems, use the IP address. Also note that if you are behind a firewall, or 
otherwise part of a network over which you do not have complete control, your 
bot may be inaccessible to users from other machines even if they know your IP 
address or hostname. In such cases, contact your system administrator or 
Internet service provider.

Once you do succeed to connect with the bot, you should see something like this:

You said:
	

CONNECT

YourBot said:
	

Connected to test case AIML set.

You are speaking with YourBot from emery.

YourBot's botmaster is unknown.

You are logged in.

You can:

    *

      log out.

If you have configured more than one bot, you can request the one you want by 
appending a botid parameter to your request. For example:

http://vasya:2001?botid=TestBot-2

would request the bot with id (not necessarily name!) TestBot-2 from the 
machine vasya.

You can also create different HTML templates for chat with your bot, by 
following the example provided in PROGRAMD /templates/html/chat.html. If you 
then want to request one of these alternate templates you can add a template 
parameter to the URL request. The value of this parameter should be the name of 
the file, minus whatever extension. So if you create a template file named 
fancy.html and store it in the templates/html directory, then you can use a 
request like this:

http://localhost:2001?botid=TestBot-1&template=fancy
3.4. Connecting via a Chat Interface

If you have correctly configured one or more listeners (see Configuring 
Listeners above, then you should be able to start the appropriate chat client 
and communicate with your bot.
3.5. Shutting Down

If you start Program D with the shell enabled, you can shut down the bot from 
the console by typing /exit command.

If the shell is disabled, you can shut down the bot by pressing the interrupt 
key sequence for your operating system; this is often Ctrl-C. Or, on *nix 
systems, you can send the main JVM process a SIGINT signal with the kill 
command.

Valid XHTML 1.0!
