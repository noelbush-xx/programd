
  Getting Started With Program D

author: Noel Bush <http://alicebot.org/bios/noelbush.html>
revised from a document by Kim Sullivan
<http://alicebot.org/bios/kimsullivan.html>, with contributions from
Zayn Blore and Dennis Daniels.
Last updated: 30 April 2002

Please check http://alicebot.org/documentation
<http://alicebot.org/documentation/> for more Alicebot/AIML
documentation, including updates to this document.

    * 0. Preparation <#preparation>
    * 1. Installation <#installation>
    * 2. Configuration <#configuration>
    * 3. First Startup <#startup>
    * 4. Miscellaneous Common Problems and Questions <#misc-faq>


    0. Preparation


      0.0. Download Program D

Go to http://alicebot.org/downloads/foundation.php and get the latest
Program D distribution. If you've never downloaded Program D before,
you'll need to register with your email address. You'll receive a
confirmation email with a password allowing you access to the download
page.

As it says on the download page, you don't necessarily need the
distribution that includes source code--you can get a pre-compiled
version that will work on any platform for which there's a compatible
Java 2 JRE or SDK. So unless you want to modify and rebuild the project,
we suggest downloading d-bin-current.tar.gz or d-bin-current.zip.


      0.1. Get the Java Runtime (or SDK)

You need to download and install a Java 2 version 1.4 compatible JVM.
Examples are the Sun JRE (Java Runtime Edition) or SDK (Software
Development Kit). The JRE is much smaller than the SDK (~9MB as compared
with ~40!!!). You only need the SDK if you want to rebuild the program,
or want to develop Java programs of your own. You can download the
necessary software and find installation instructions at
http://java.sun.com/j2se/1.4/download.html.


      0.2. Get Some AIML

This step is optional but highly recommended. If you don't use some
pre-existing AIML, you'll have to write your own before your bot knows
anything. Also at http://alicebot.org/downloads/foundation.php, you'll
find several choices of freely-available AIML. Pick one and download it.


    1. Installation


      1.0. Java

We won't cover installation instructions for the JRE/Java SDK here.
We'll just assume that you've followed the instructions. In the
following, we'll refer to the directory where you installed Java as
JAVA_HOME. (If you use Windows, this might be something like
C:\j2sdk1.4.0. If you're using Linux, this might be /usr/java/j2sdk1.4.0.

You should at least be able to type "java -version" at a command line
(syntax may vary) and get a response something like:

java version "1.4.0"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.4.0-b92)
Java HotSpot(TM) Client VM (build 1.4.0-b92, mixed mode)

If you get some kind of error like "command not found", check that your
system path settings are correct. Or, just try "JAVA_HOME/java -version"
(Linux) or "JAVA_HOME\java.exe -version" (Windows), substituting your
actual JAVA_HOME path.


      1.1. Program D

If you're upgrading from a previous version, please check the release
notes <release-notes.html>, since some configuration files may have
changed.

Unzip/untar the Program D download in a convenient location. On Linux,
we suggest /usr/alice; on Windows, the root directory (C:\, perhaps) is
as good as anything. The unzipping/untarring process will create a
directory called ProgramD that will contain all the program files.

Linux users may type:

tar xvzf d-bin-current.tar.gz

Windows users could try a free unzip utility such as 7-Zip
<http://www.7-zip.org>.

We'll refer to the root directory created by the unzip/untar (such as
/usr/alice/ProgramD) as PROGRAMD.


      1.2. AIML

If you downloaded some AIML files, now is a good time to put them
somewhere. We suggest creating a subdirectory called "aiml" in your
PROGRAMD directory, and then arranging your AIML files there in a way
that makes sense to you. You'll need to unzip/untar as above.

Earlier convention called this the "bots" directory and put AIML files
for different bots in different subdirectories, but we think that
practice obscures the fact that a "bot" is really defined by more than
just the AIML it uses, as well as the fact that multiple bots can share
AIML. If it helps you to stay organized, you can create subdirectories
(for instance, to manage copies of different releases of AIML files that
you create or download), but this isn't required.


    2. Configuration

There's a lot to configure with Program D. What's needed for you really
depends on how you want to use the program. If you don't care about
running a web server, you don't need to bother with the "*HTTP Server*
<#config-http-server>" section. If you don't care about using Program D
with chat interfaces like AOL Instant Messenger or IRC, you won't be
interested in the information about "*Chat Listeners*
<#config-listeners>". If you don't care about high-volume applications,
or don't really want to do database-based log analysis, you can skip the
"*Database* <#config-database>" section.


      2.0. Bots

Program D lets you configure an unlimited number of bots that can run at
the same time. The bots are configured in the PROGRAMD/conf/startup.xml
file.

Open the startup.xml file. Notice that the root element is called
<programd-startup>, and that it contains exactly one child element
called <bots>. (It is possible to put other elements here like
<substitutions> and so on, but in the example configuration we keep
those in separate files.)

Inside <bots>, we place one or more <bot> elements. These <bot> elements
are not the same as the AIML tag of the same name.

Each <bot> element has two important attributes: id and enabled. The
first one assigns an identifier, which should be unique, for the bot.
The identifier will be used internally by the engine and will be written
to some log resources. The enabled attribute should have either of the
values "true" or "false". If the value is "true", then Program D will
try to load that bot when the server starts up. Switching enabled to
"false" is an easy way to quickly turn off a bot configuration that you
don't want to use (although a restart is required).

Within the <bot> element we define *bot properties*, *listeners*,
*default predicates*, *substitutions*, *sentence-splitters* and *learn
directives*.


        2.0.0. Bot Properties

*Bot properties* are predicates that cannot be changed during the
runtime life of the bot, but which /can/ be included in AIML patterns
for matching. A common property to define for a bot is "name". Bot
properties are defined in individual <property> elements inside a bot's
<properties> element, as in the example:

<property name="master" value="A.L.I.C.E. AI Foundation"/>

This associates the bot property name "master" with the value
"A.L.I.C.E. AI Foundation". All your base are belong to us.

(You can also define bot properties in a separate file, and point to
that file using an "href" attribute on the <properties> element. See
other elements inside <bot> in the example startup.xml to understand how
this is done.)

Properties don't mean anything unless your AIML uses them. You can
display the value of a bot property inside an AIML template by using the
form <bot name="property-name"/>.


        (2.0.1. Listeners)

Listeners are discussed in more detail below <#config-listeners>.


        2.0.2. Default Predicates

*Default predicates* can be thought of as your bot's "assumptions" about
new users. While AIML allows you to use <set
name="predicate-name">...something...</set> and <get
name="predicate-name"/> forms without any sort of "declaration", Program
D does allow you to set default values to associate with any predicate
names you wish, so that you have more control over what is returned by
<get name="predicate-name"/> if a corresponding <set> has not yet
happened for a given user.

The <predicates> section, if it exists, may directly include one or more
<predicate> children, or it may use an "href" attribute, as in the
example, that points to another file where the predicates are defined.

You can also use <predicate> elements to mark a predicate as
return-name-when-set
<http://alicebot.org/TR/2001/WD-aiml/#section-aiml-predicate-behaviors>.
For instance, this predicate definition is included in a sample file:

<predicate name="he" default="somebody" set-return="name"/>

This means that when <set name="he">...</set> is included in a template,
the name of the predicate, "he", will be displayed, rather than whatever
value is associated with the name by the <set>.


        2.0.3. Substitutions

*Substitutions* have several different purposes, depending on their
type. *Input* substitutions contribute to the process of input
normalization
<http://alicebot.org/TR/2001/WD-aiml/#section-input-normalization>.
*Person* substitutions provide macros for transformations by the
<person> <http://alicebot.org/TR/2001/WD-aiml/#section-person> tag;
likewise *person2* and *gender* apply to the <person2>
<http://alicebot.org/TR/2001/WD-aiml/#section-person2> and <gender>
<http://alicebot.org/TR/2001/WD-aiml/#section-gender> tags, respectively.

Each individual substitution specification, regardless of whether it is
inside an <input>, <gender>, <person> or <person2>, takes the same form
as this example from conf/substitutions.xml:

<substitute find=" becasue " replace=" because "/>

This means that, when this substitution is applied, each instance of the
separate word "becasue" will be replaced with "because". (This is an
example of typo correction in Program D.)

Note the spaces that pad the values of the "find" and "replace"
attributes. These spaces are, approximately, indications that we want to
match only on "word boundaries". Omitting one or both padding spaces in
the "find" string would mean that its contents should be matched even if
they occur as part of a word. This is generally not desirable.


        2.0.4. Sentence-Splitters

*Sentence-splitters*, as described in the AIML spec
<http://alicebot.org/TR/2001/WD-aiml/#section-sentence-splitting-normalizations>,
are:

...heuristics applied to an input that attempt to break it into
"sentences". The notion of "sentence", however, is ill-defined for many
languages, so the heuristics for division into sentences are left up to
the developer.

Since sentence-splitters are applied to the input /after/ substitution
normalizations, they can be more general rules. The entire collection of
example sentence-splitters shipped with Program D is:

<sentence-splitters>
    <splitter value="."/>
    <splitter value="!"/>
    <splitter value="?"/>
    <splitter value=";"/>
</sentence-splitters>

As with some other examples, these are defined in a separate file that
is pointed to by the "href" attribute on the <sentence-splitters> element.


        2.0.5. Learn Directives

*Learn directives* function identically to the <learn>
<http://alicebot.org/TR/2001/WD-aiml/#section-learn> element in AIML.

You can use simple "glob"-like wildcard patterns for specifying files to
load. For example, the shipped startup.xml file has this single learn
directive:

<learn>../aiml/standard/*.aiml</learn>

The path specification is relative to the current file, so this
indicates that within a directory reached by going one level up (to the
main PROGRAMD directory), and then into a directory called "aiml", and
from there into a subdirectory called "standard", the program should
load all files that end in ".aiml". Note that files containing AIML are
/not/ required to have this or any particular suffix; also, if you
direct the program to learn files that do not contain any AIML, you will
receive a warning but nothing will be loaded from that file.


      2.1. HTTP Server

If you want to be able to talk to your bot through a web interface,
you'll be interested to know that Program D is designed to be able to
work as a "servlet"--that's an application that runs on a server but
interacts with users via the web. As an example we use Program D with
Jetty, an open source HTTP server and servlet container. Future versions
of Program D will hopefully be easier to integrate with other servlet
containers, but for now in discussion about configuration we'll stick
with customizing the Jetty setup.

The main file you'll be interested in is PROGRAMD/conf/jetty.xml. This
is the file that is read by the Jetty server when it is invoked by
Program D.

The parameter that will probably be of most interest here is the port
number. This is specified in the place that looks like:

<Set name="Port">2001</Set>

Most web sites use the port numbered 80, which is the port assumed by a
web browser when you type a URL without a port specification, so you
might never have even known that there was such a thing as a port in a
URL! Having different ports allows different server programs running on
the same machine to answer requests without bothering each other.

For testing purposes, it is probably fine to use the default value of
2001, or whatever you prefer (although on some operating systems you
can't use a number lower than 1024 without special privileges). But for
serious applications you'll want to deal with this differently, since
many firewalls don't allow people to access arbitrary ports. The easiest
solution to the port problem is to use a proxy server (or the proxy
feature of a good web server like Apache <http://httpd.apache.org>) to
allow external users to access your bot using a regular web address that
doesn't specify a port (i.e., through port 80).

You probably won't care to change the rest of the Jetty configuration
file, unless you intend to use Jetty to serve content or run other
applications besides Program D (not recommended, given the current
Program D distribution format).


      2.2. Chat Listeners

In current Program D nomenclature, a "listener" is a program that knows
how to speak a particular protocol and interpret messages for the bot.
For instance, the AliceIRCListener understands the popular IRC protocol
and allows you to connect your bot to any IRC server.

You can enable one or more listeners for each bot. Each listener has its
own unique configuration parameters, which are specific to the
requirements of the protocol for which it's designed. You'll find
examples of each currently-available listener in the
PROGRAMD/conf/startup.xml file.

Listeners for a given bot are specified within the <listeners> element,
in individual <listener> elements. Each <listener> element must have two
attributes: "type", and "enabled". The value for "type" must correspond
with a value assigned by the developer to a listener that is somewhere
in the Java classpath for Program D. The example startup.xml includes an
example of each type currently known. The value of "enabled" must be
"true" or "false", allowing the listener to be switched on or off with
ease.

The parameters for a listener are specifed in individual <parameter>
elements within the <listener>. For example:

<parameter name="host" value="ar.chatjunkies.org"/>
<parameter name="port" value="6667"/>
<parameter name="nick" value="programd"/>
<parameter name="channel" value="#bots"/>

These are the parameters set for the example use of the
AliceIRCListener, whose type string is "AliceIRC".

When you enable a chat listener, you will see its output mixed in with
the console output. You can also interact with some listeners if they
implement the "commandable" interface (see below <#commandables>).


      2.3. Database

You are not required to install, configure or use a database in order to
use Program D. The default configuration is entirely based on text files
in order to make setup quick, painless, and no more resource-intensive
than necessary.

However, for heavy-volume situations, and/or for cases where you want to
collect dialogue in a form that can perhaps be more easily mined, you
may wish to use a database.

We have done the most extensive testing with MySQL
<http://www.mysql.com>; however, other users have connected Program D
with various other DBMSes. The major point is that you must have a JDBC
driver for the database you wish to use. We include the MySQL JDBC
driver with the Program D distribution for convenience.

If you do want to use a database, you need to configure it properly in
the server.properties file. The relevant part of the file is this:

#
-----------------------------------------------------------------------------
# DATABASE CONFIGURATION
# * This is only meaningful if you are using a database-enabled Multiplexor
# * and/or the database-based chat logging.
#
-----------------------------------------------------------------------------

# Typical mySQL configuration
programd.database.url=jdbc:mysql:///alicebot
programd.database.driver=org.gjt.mm.mysql.Driver

# The maximum number of simultaneous connections to the database
programd.database.connections=25

# The username to access the database
programd.database.user=alicebot

# The password for the database
programd.database.password=yourpassword

You should set these values to match your database installation.
Remember that the driver class must be available from the Java classpath.


        2.3.0. DBMultiplexor

If you want to store predicates in the database, you must enable the
DBMultiplexor. This can be done by switching the # comment mark in
server.properties so that this:

# Multiplexor to use
#programd.multiplexor=org.alicebot.server.core.DBMultiplexor
programd.multiplexor=org.alicebot.server.core.FlatFileMultiplexor

becomes this:

# Multiplexor to use
programd.multiplexor=org.alicebot.server.core.DBMultiplexor
#programd.multiplexor=org.alicebot.server.core.FlatFileMultiplexor

If DBMultiplexor is enabled, Program D will expect to find two tables
called users and predicates in the configured database. The SQL commands
for creating these tables are in PROGRAMD/database/db-multiplexor.script.


        2.3.1. Database-based Chat Logging

Database-based chat logging stores each exchange with the bot in a
database. This is useful for monitoring conversations, data mining, etc.
To enable this feature, you must set:

programd.logging.to-database.chat=true

(in server.properties). If this is enabled, Program D will expect to
find a table called chatlog in the database. The command for creating
this table is in PROGRAMD/database/db-chatlog.script. Note that if you
are logging chats to a database, you might want to disable the redundant
logging to XML files. You can do this by setting:

programd.logging.to-xml.chat=false


      2.4. Shell and Console

In its usual configuration, Program D displays information about what it
is doing while it starts, runs, and shuts down. We call this the
"console". Also, you can interact with Program D via a simple shell, if
desired. Sometimes it is simpler to use this shell than to open a web
browser or IM program. Both the console and shell can be configured to
suit your needs. Configuration for both is done in the server.properties
file. Following are notes about some specific configuration properties.

programd.console=true

This tells the program to print information to the console. If you set
this to false, almost nothing (except the copyleft notice) will be
displayed when the program is run.

programd.console.developer=false
programd.console.developer.method-names-always=false

Unless you are interested in debugging the code, you should leave these
set to false.

programd.console.match-trace=false

Setting this property to true can help you understand what is happening
when matching occurs. See Understanding the Match Trace
<#understanding-match-trace> (below) for more details on what
information is produced.

programd.console.message-flags=false

Some people may find it useful to set this to true. Doing so will prefix
each line of the console output with a flag indicating what type of
message is on the line. The flags are:

    *

      [u]: An informational user message.

    *

      [U]: A message regarding a user error.

    *

      [d]: An informational message for a developer.

    *

      [D]: A message regarding a developer error.

    *

      [s]: Text from the interactive shell.

    *

      [!]: A message that cannot be supressed.

programd.console.bot-name-predicate=name
programd.console.client-name-predicate=name

If you want to store the bots' and/or users' names in
properties/predicates with names other than "name", then here's where
you can change that. This property exists for the purpose of tuning the
chat log functionality.

programd.console.warn-non-aiml=true

It's a good idea to leave this on; it will warn you when you use
non-AIML elements outside of a template.

programd.console.category-load-notify-interval=6000

If you're using a very slow computer, you can adjust this so that it
produces comforting messages while categories are loading. Most
computers will load the AIML so quickly that this is just something to
let you save screen real estate.

programd.console.timestamp-format=H:mm:ss

You can choose how the timestamp looks in the console output. If you
want the date, no problem. Want 12-hour US-style time, no problem. Just
see
http://java.sun.com/j2se/1.4/docs/api/java/text/SimpleDateFormat.html
for the formatting codes to use.

programd.shell=true

In some situations you may wish to disable the interactive shell. Set
this parameter to false to do so.


      2.5. AIMLWatcher

The AIML Watcher is a nifty feature that watches to see if you change
your AIML files. This allows you to update AIML without restarting the bot.

As a security feature, the AIML Watcher only watches files that were
loaded by a *learn* directive. If you put a new file into a directory
with other AIML files, that file will /not/ be automatically loaded (but
you can load it with the /load shell command if you have access to the
console and have not disabled the shell).

You can enable the AIML Watcher by setting:

programd.watcher=true

And you can set the frequency with which the AIML Watcher checks your
files for modification by changing:

programd.watcher.timer=2000

(The units are milliseconds. Don't be unreasonable in setting this too
low.)


        2.6. Other server.properties Configuration Items

The server.properties file is documented inline, so here we don't
exhaustively enumerate every property. Nonetheless, following are
descriptions of some other interesting parameters.

programd.startup=conf/startup.xml

This parameter tells Program D where to find the startup.xml file. Note
that the path is relative to the *working directory /when Program D
starts/*. This may not necessarily be the same as your PROGRAMD
directory. You should control this by adjusting your startup
script/batchfile as desired.

programd.emptydefault=undefined

Any predicates not given default values in a bot-specific <predicate>
element will have this value as their default value. Note that if you
use AIML <condition> elements to match a predicate value against a
particular emptydefault value, you introduce a server.properties
dependency.

programd.response-timeout=1000

If your AIML contains infinite loops, or otherwise results in long
processing times, this will cancel the operation by checking (constantly
throughout the matching process) to see if the timeout you specify (in
milliseconds) has passed. If it has, a "no match" event will occur. 1000
milliseconds (one second) is an extremely generous and safe value. It is
possible that actual problem match/response loops will run a little
longer than this value, but not much longer.

programd.infinite-loop-input=INFINITE LOOP

Program D tries to detect some simple kinds of infinite loops. If it
finds one, it substitutes whatever input you specify here in place of
the original input that caused the loop. If the original input was the
same as this value, then a "no match" event occurs.

programd.os-access-allowed=false

If you plan to let your bot use AIML from strangers, consider leaving
this off. The <system> tag is very powerful, but also dangerous.
Malicious AIML could potentially wreak havoc with your filesystem using
this tag. Disabling it will cause all <system> elements to return
nothing, and a message will appear in the console and logs.

programd.javascript-allowed=false

Allowing server-side JavaScript has similar security implications,
although you are a little more protected. Still, because the Rhino
JavaScript engine included as an example with Program D will permit use
of any Java public methods, someone could potentially access your
database, filesystem, etc. using this mechanism. Consider leaving it
disabled if you are going to use unknown AIML.

programd.connect-string=CONNECT

When a new connection is detected, this string will be sent to the bot
as though the user had typed it. If you create a category whose pattern
matches this, you can make the bot say something special when the user
first connects.

programd.inactivity-string=INACTIVITY

Certain responders or listeners may characterize some user activity,
such as a blank input or a long delay without input, as "inactivity".
You can set this string to tell them what input to send the bot in such
cases.

programd.deprecated-tags-support=true

The so-called "AIML 0.9" tag set includes some forms that are now
deprecated. If you choose to support these, be forewarned that the
template parser will be somewhat slower since parsing of deprecated tags
is done using a less efficient parser.

programd.deprecated-tags-warn=true

You can choose to enabled support of deprecated tags, but still receive
warnings, to assist you in getting rid of them.

programd.heart.enabled=false
programd.heart.pulserate=5

If you enable the "heart", you can set the bot's pulse rate to something
reasonable that will let you know that the bot is still alive. Right now
this consists of the message "I'm alive!" being printed to the console,
which is not particularly useful unless you're experiencing serious
problems with deadlock in the bot server (in which case you should
immediately file a bug report <http://alicebot.org/bugzilla>!!!). Future
enhancements to this may include the facility to write health monitoring
information to a file/database.

programd.predicate-cache.max=500

Predicates are not immediately saved to the flat file or database, in
the interest of improving performance. However, there is a tradeoff
between performance and reliability: if your bot server goes down for
any reason, any unsaved predicates will be lost.

But consider that the <that> and <input predicates, which are indexed up
to five (5) values each, are updated each time a user has an exchange
with the bot. Writing these to disk or database on each turn can become
very expensive. If you are willing to risk the possibility of losing
context, you can set this value to a reasonable number. "Reasonable"
will depend on your average client load and average number of custom
predicates. You can also set this number to 1 if you want the bot to
save every predicate every time it's updated, but be forewarned of the
potential performance hit.

programd.interpreter.system.prefix=cmd /c

If <system> elements don't seem to be working right, you may need to
adjust this. Linux users, for instance, can just comment it out.


    3. First Startup


      3.0. Starting Program D

To start Program D, run server.sh (*nix) or run.bat (Windows). The
run.bat file is provided for Windows users so that common problems with
the DOS environment can be handled before launching the server.bat file
(which is started by run.bat.

If you're using Windows and you launch the batch file by double-clicking
it, but there are some configuration problems, the console window might
disappear before you have time to read the messages. You are advised in
this case to open a command prompt and run the batch file from there (cd
to the right directory, type "run").

If you're using Linux, you may need to make the server.sh file
executable by issuing the command:

chmod +x server.sh

Starting the server script should look something like this:

[noel@vasya ProgramD]$ ./server.sh
Starting Alicebot Program D.
[12:10:21] Starting Alicebot Program D version 4.1.5
[12:10:21] Using Java VM 1.4.0-b92 from Sun Microsystems Inc.
[12:10:22] On Linux version 2.4.2-2 (i386)
[12:10:22] Predicates with no values defined will return: "undefined".
[12:10:22] Initializing Multiplexor.
[12:10:22] Loading Graphmaster.
[12:10:22] Starting up with
"/home/noel/eclipse/workspace/ProgramD/conf/startup.xml".
[12:10:22] Configuring bot "TestBot-1".
[12:10:22] Loaded 287 input substitutions.
[12:10:22] Loaded 19 gender substitutions.
[12:10:23] Loaded 9 person substitutions.
[12:10:23] Loaded 60 person2 substitutions.
[12:10:23] Loaded 4 sentence-splitters.
[12:10:24] 6000 categories loaded so far.
[12:10:25] 12000 categories loaded so far.
[12:10:27] 18000 categories loaded so far.
[12:10:28] 1 bots thinking with 23879 categories.
[12:10:28] Alicebot Program D (c) 1995-2002 A.L.I.C.E. AI Foundation
[12:10:28] All Rights Reserved.
[12:10:28] This program is free software; you can redistribute it and/or
[12:10:28] modify it under the terms of the GNU General Public License
[12:10:28] as published by the Free Software Foundation; either version 2
[12:10:28] of the License, or (at your option) any later version.
[12:10:28] Alicebot Program D version 4.1.5 Build [00]
[12:10:28] 23879 categories loaded in 6.133 seconds.
[12:10:28] The AIML Watcher is not active.
[12:10:28] HTTP server listening at http://vasya:2001
[12:10:29] Interactive shell: type "/exit" to shut down; "/help" for help.
[12:10:29] user> CONNECT : Hello there user and thanks for connecting :
* : TestBot-1
[12:10:29] Match: CONNECT : * : * : TestBot-1
[12:10:29] Filename:
"/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard//std-connect.aiml"
[12:10:29] Response 1 in 117 ms. (Average: 117.0 ms.)
[12:10:29] Testy 1> Hello there user and thanks for connecting!
[12:10:29] [Testy 1] user>

If there are any errors while starting up, an explanation will be
printed and the server will shut down. If you encounter a confusing
error message, please file a bug report <http://alicebot.org/bugzilla>!!!


      3.1. Understanding the Console

While the bot is running, information will be printed to the console to
tell you what's going on.


        3.1.0. Understanding the Match Trace

If you enable the match trace in server.properties you can understand
the details of what is going on inside the bot engine. Here is an
example of match trace output, and a detailed line-by-line explanation
of what it means:

[10:22:55] [Testy 1] Client> Who is the smartest bot in the world?
[10:23:13] Client> Who is the smartest bot in the world? : Does that
mean no : * : TestBot-1
[10:23:13] Match: _ IN THE WORLD : * : * : TestBot-1
[10:23:13] Filename:
"/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard//std-suffixes.aiml"
[10:23:13] Symbolic Reduction:
[10:23:13] Client> Who is the smartest bot : Does that mean no : * :
TestBot-1
[10:23:13] Match: WHO IS THE SMARTEST * : * : * : TestBot-1
[10:23:13] Filename:
"/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard//std-robot.aiml"
[10:23:13] Response 4 in 7 ms. (Average: 48.0 ms.)
[10:23:13] Testy 1> Testy 1 is the most intelligent robot in the world.
The whole world?

Line by line, this excerpt from the console output shows:

    *

      I said, "Who is the smartest bot in the world?" to the bot whose
      name is Testy 1.

    *

      The complete set of values for the input path generated by my
      input was:

          o

            *input:* Who is the smartest bot in the world?

          o

            *that:* Does that mean no

          o

            *topic:* (nothing)

          o

            *botid:* TestBot-1

    *

      The first category matched by my input had the following
      characteristics:

          o

            *pattern:* _ IN THE WORLD

          o

            *that:* *

          o

            *topic:* *

      (And it belonged to the bot with id "TestBot-1". Program D
      includes the bot id as the last part of the match path. This is
      not a requirement of the AIML pattern-matching process, just a
      particular way of implementing multi-bot functionality. Remember
      that pattern matching proceeds word by word, not category by
      category; see AIML Pattern Matching Simplified
      <http://alicebot.org/documentation/matching.html> for more
      information.)

    *

      This category was originally loaded from the file
      /home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard//std-suffixes.aiml.
      (Note that the pathname is not fully "canonicalized".)

    *

      This template triggered a symbolic reduction
      <http://alicebot.org/documentation/srai.html>.

    *

      The reduced input path had these values:

          o

            *input:* Who is the smartest bot

          o

            *that:* Does that mean no

          o

            *topic:* (nothing)

          o

            *botid:* TestBot-1

    *

      This (reduced) input matched the category with these values:

          o

            *pattern:* WHO IS THE SMARTEST *

          o

            *that:* *

          o

            *topic:* *

    *

      This category was originally loaded from the file
      /home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard//std-robot.aiml.


    *

      This was response number 4 since the bot was started, and it was
      produced in 7 milliseconds. The average response time for this bot
      during its run so far is 48.0 milliseconds.

    *

      The complete response produced was: "Testy 1 is the most
      intelligent robot in the world. The whole world?".


        3.1.1. Shutdown Messages

Before the bot server can shut down, it must stop the http server and
must save any predicates left in the cache. It also must stop all
listeners. There are several different ways to shut down
<#shutting-down>, but all of them should cause Program D to print
information that looks like this:

[12:26:14] AliceServer is shutting down.
[12:26:14] Shutting down all BotProcesses.
[12:26:14] Shutting down org.alicebot.server.net.JettyWrapper@861f24
[12:26:18] Finished shutting down BotProcesses.
[12:26:18] Saving all cached predicates (3)
[12:26:18] Shutdown complete.

"Bot Processes" are such things as the http server and listeners.
Sometimes one or more of these may take a long time to shut down. Unless
you are in a great hurry, you should allow this process to complete
properly. The saving of cached predicates should be very fast.


      3.2. Using the Shell

The simplest use of the shell is to talk to your bot(s). However, there
is a growing list of other capabilities available to you. All shell
commands in Program D are preceded by a / (slash) character. If you type
"/help" at a shell prompt, you can see the list of available commands:

[12:31:21] [Testy 1] user> /help
[12:31:23] All shell commands are preceded by a forward slash (/).
[12:31:23] The commands available are:
[12:31:23] /help - prints this help
[12:31:23] /exit - shuts down the bot server
[12:31:23] /load filename - loads/reloads given filename for active bot
[12:31:23] /unload filename - unloads given filename for active bot
[12:31:23] /bots - lists loaded bots
[12:31:23] /talkto botid - switches conversation to given bot
[12:31:23] /who - prints the id of the current bot
[12:31:23] /files - lists the files loaded by the current bot
[12:31:23] /roll chatlog - rolls over chat log
[12:31:23] /roll targets - rolls over saved targeting data
[12:31:23] /commandables - lists available "shell commandables" (such as
listeners)


        3.2.0. Loading AIML

The /load command lets you load new AIML files, or reload files (useful
if you have disabled the AIML Watcher <#aiml-watcher>.) The following
command, for example, will load the file aiml/new-file.aiml:

[12:31:23] [Testy 1] user> /load aiml/new-file.aiml

The path must be relative to the working directory for Program D. If the
file cannot be found, you will get a message like:

[12:33:58] Couldn't find "/home/noel/cvs/aiml-sets/new-file.aiml".
[12:33:58] 0 categories loaded from
"/home/noel/cvs/aiml-sets/new-file.aiml".


        3.2.1. Unloading AIML

The /unload command lets you unload an AIML file that is currently in
memory. You must unload using the pathname that was originally used to
load it, which can be a little tricky to get right. Please note that
this feature is still experimental and may not always behave as expected.


        3.2.2. Listing Loaded Bots

You can see which bots are currently loaded with the /bots command:

[12:46:09] [Testy 2] user> /bots
[12:46:12] Active bots: TestBot-2 TestBot-1


        3.2.3. Talking to a Different Bot

You can switch the bot with whom you are conversing by using the /talkto
command. You must use a valid bot id as the argument. (The bot id is not
necessarily the same as the value associated with its "name" property.
To see bot ids, look at your startup.xml file or use the /bots command.

Each time you switch bots, the connect string will be sent to the bot:

[12:46:12] [Testy 2] user> /talkto TestBot-1
[12:48:28] Switched to bot "TestBot-1" (name: "Testy 1").
[12:48:28] user> CONNECT : Hello there user and thanks for connecting :
* : TestBot-1
[12:48:28] Match: CONNECT : * : * : TestBot-1
[12:48:28] Filename:
"/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard//std-connect.aiml"
[12:48:28] Response 2 in 5 ms. (Average: 52.5 ms.)
[12:48:28] Testy 1> Hello there user and thanks for connecting!

Trying to talk to a botid that doesn't exist will result in an error
message:

[12:48:28] [Testy 1] user> /talkto non-existent-bot
[12:49:56] That bot id is not known. Check your startup files.


        3.2.4. Recalling Who You're Talking To

Just in case you aren't sure which bot you're speaking with, you can use
the /who command:

[12:49:56] [Testy 1] user> /who
[12:51:37] You are talking to "TestBot-1".

Notice how in this example the botid is different from the value
associated with its "name" predicate, which is what is displayed in the
square brackets at each input prompt.


        3.2.5. Listing Loaded Files

The /files command lists all files that are loaded by the active bot.

[12:51:37] [Testy 1] user> /files
[12:53:37] 40 files loaded by "TestBot-1":
[12:53:37]
/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard/std-srai.aiml
[12:53:37]
/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard/std-numbers.aiml
[12:53:37]
/home/noel/eclipse/workspace/ProgramD/conf/../aiml/standard/std-botmaster.aiml
...


        3.2.6. Rolling the XML Chatlog

If you are using the XML chat log feature, you may find that the file
gets too big for you to handle (this is especially a problem with
XSLT-based display of the logs). You can use the /roll chatlog command
to "roll over" the log file so that the current log file is renamed, and
a new one is created. The new one will contain a link to the previous
one, so over time you can have a chain of log files.

[13:01:34] [Testy 1] user> /roll chatlog
[13:01:37] Rolling over chat log for "TestBot-1".
[13:01:37] Finished rolling over chat log.

(This rollover can also be controlled to some degree by adjusting the
programd.logging.xml.rollover property in server.properties.


        3.2.7. Rolling the Targets File

Targets are currently written to an XML file; this can also become
enormous. You can roll over this file with the /roll targets command:

[20:06:54] [Testy 2] user> /roll targets
[20:07:28] Rolling over targeting data.
[20:07:28] Targeting data deleted (old file rolled over).


        3.2.8. Using Commandables

A *commandable* is a module that can be dropped in to Program D and
commanded from the shell. As of this writing, the only module that
implements the commandable interface is AliceIRC. You can see a list of
available commandables with the /commandables command:

[20:13:48] Available shell commandables:
[20:13:48] /irc - Alice IRC chat listener
[20:13:48] Commands after the shell commandable will be sent to the
commandable.
[20:13:48] Example: "/irc /JOIN #foo" tells the AliceIRC listener to
join channel "#foo".

As the program tells you, you send commands to the commandable by typing
its name followed by a command. For example:

[20:18:15] [Testy 2] user< /irc /names
[20:18:17] [Testy 2] user>
[20:18:18] AliceIRC: [server] Users on #bots: programd dos-bot FakeUser
search @ChanServ
[20:18:18] AliceIRC: [server] (366) End of /NAMES list.

IRC historically has a command-driven interface, so providing a
commandable interface for it was easy. IM clients or other modules may
need more work.


      3.3. Connecting via the Web Interface

Once your bot is started, you can interact with it through a web
browser. If you have not changed the port number (see HTTP Server
<#config-http-server> above, then you should get a response when you go
to this address:

http://localhost:2001

Note that "localhost" is the name by which your computer knows
itself--not only in Program D, but in any program that uses TCP/IP (the
set of protocols that makes the Internet work). You cannot go to another
computer and connect to yours using the name "localhost". You will need
to know the IP address or name of the computer where the bot is running,
in order to connect from another machine. On Windows 95 and 98, you can
find this out by running the command:

winipcfg

On Windows NT, 2000 and XP, use:

ipconfig

On *nix, use:

host

Try using the hostname first (it's usually easier to remember); if that
causes problems, use the IP address. Also note that if you are behind a
firewall, or otherwise part of a network over which you do not have
complete control, your bot may be inaccessible to users from other
machines even if they know your IP address or hostname. In such cases,
contact your system administrator or Internet service provider.

Once you do succeed to connect with the bot, you should see something
like this:

You said:

CONNECT

Testy 1 said:

Hello there user and thanks for connecting!

You are speaking with Testy 1 from vasya.

Testy 1's botmaster is A.L.I.C.E. AI Foundation.

You can:

    *

      log in <?login=yes>.

    *

      register a new username and password <?register=yes>.

If you have configured more than one bot, you can request the one you
want by appending a botid parameter to your request. For example:

http://vasya:2001?botid=TestBot-2

would request the bot with id (not necessarily name!) TestBot-2 from the
machine vasya.

You can also create different HTML templates for chat with your bot, by
following the example provided in PROGRAMD/templates/html/chat.html. If
you then want to request one of these alternate templates you can add a
template parameter to the URL request. The value of this parameter
should be the name of the file, minus whatever extension. So if you
create a template file named fancy.html and store it in the
templates/html directory, then you can use a request like this:

http://localhost:2001?botid=TestBot-1&template=fancy


      3.4. Connecting via a Chat Interface

If you have correctly configured one or more listeners (see Configuring
Listeners <#config-listeners> above, then you should be able to start
the appropriate chat client and communicate with your bot. Note that as
of this writing the ICQ listener has some problems (see release notes).


      3.5. Shutting Down

If you start Program D with the shell enabled, you can shut down the bot
from the console by typing /exit command:

[20:45:12] [Testy 1] user> /exit
[20:45:13] Exiting at user request.
[20:45:13] AliceServer is shutting down.
[20:45:13] Shutting down all BotProcesses.
[20:45:13] Shutting down org.alicebot.server.net.JettyWrapper@20d62b
[20:45:17] Finished shutting down BotProcesses.
[20:45:17] Saving all cached predicates (19)
[20:45:17] Shutdown complete

If the shell is disabled, you can shut down the bot by pressing the
interrupt key sequence for your operating system; this is often Ctrl-C.
Or, on *nix systems, you can send the main JVM process a SIGINT signal
with the kill command.


    4. Miscellaneous Common Problems & Questions

    * 4.0. I get a message saying "out of environmental space"
      <#faq-out-of-environment-space>
    * 4.1. Why doesn't the admin console (at port 2002) work?
      <#faq-admin-console>
    * 4.2. How do I change the HTML template?
      <#faq-changing-the-HTML-template>
    * 4.3. Installing to long-path name directory can cause late-load
      failure. <#faq-long-pathname-directory> (Windows)
    * 4.4. Error Message: "Bad command or filename" or "The system
      cannot find the path specified." <#faq-bad-command-or-filename>
      (Windows)
    * 4.5. Error Message: "page cannot be found" (Internet Explorer)
      <#faq-page-cannot-be-found>

    *

      4.0. I get a message saying "out of environment space" (Windows)

      You shouldn't get this message when running run.bat. If you do,
      edit it and increase the number after both of the the /E: parameters.

    *

      4.1. Why doesn't the admin console (at port 2002) work?

      It isn't implemented. Stay tuned to the Alicebot and AIML mailing
      list <http://alicebot.org/mailing-lists.html#general> for news.

    *

      4.2. How do I change the HTML template?

      The file templates/html/chat.html is used for constructing a web
      page with the bot's response. It is a plain HTML file, with a few
      important tags (these may change in the future):

          o <reply></reply>

            These tags separate the 'header' of the bot response from
            the 'main' part and the 'footer'. What comes before is the
            header, what is inside is the main part and what comes after
            is the footer. The main part is repeated for every sentence
            in the client input. These must be included, even though the
            'main' part may be empty.

          o <userinput/>

            The user input. This may be either the whole input (in the
            'header') or one sentence of it (in the 'main' part).

          o <response/>

            The bot output. This may be either whole ('footer') or one
            sentence ('main').

          o <bot name="xxx"/>

            Includes the value of the bot predicate 'xxx' in the page
            (such as the name).

    *

      4.3. Installing to long-path name directory can cause late-load
      failure (Windows)

      If you unzip the server build to a path with a directory with
      longer than 8 characters, and you change to that directory via its
      DOS 8.3 path name, you'll get a run-time failure that looks like
      this:

      D:\dev\ALICE2~1\server>server
      [loading notices deleted]
      java.io.FileNotFoundException:
      D:\dev\ALICE2~1\server\users\USERS.properties is alias of
      D:\dev\alice200105\server\users\USERS.properties

      The workaround is to shorten the pathname.

    *

      4.4. Error message: "Bad command or filename" or "The system
      cannot find the path specified." (Windows)

      Check again if you have a Java Runtime Environment (JRE) installed
      properly. Try typing java at the command prompt. You should get
      some information on how to use this command. If you get an error
      message, you need to install the JRE. You can get it e.g. from
      Sun's website <http://java.sun.com/j2se/1.4/jre/>.

      Make sure you run the server with the run.bat file.

      If all else fails, remove the line saying @echo off from
      server.bat and run run.bat. Post the results to the Alicebot and
      AIML mailing list
      <http://alicebot.org/mailing-lists.html#general>, possibly stating
      what computer you have (OS, memory) and what version of Program D
      you are running (this can be found in the file release-notes.html).

    *

      4.5. Error Message: "page cannot be found" error (Internet Explorer)

      One rather wacky aspect of IE is that if you enter a localhost
      address with a port number, you must precede it with http:// or IE
      will mysteriously transform it into "local:####" (where "####" is
      the port number you typed) and you won't see what you want. So be
      sure you type out: "http://localhost:2001". Make a bookmark.

Valid XHTML 1.0! <http://validator.w3.org/check/referer>

