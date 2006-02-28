                         Getting Started With Program D

   author: [1]Noel Bush
   revised from a document by Kim Sullivan, with contributions from Zayn
   Blore and Dennis Daniels.
   Last updated: 28 February 2006

   Please check [2]http://aitools.org for more documentation, including
   updates to this document.

     * [3]0. Preparation
     * [4]1. Installation
     * [5]2. Configuration/Deployment
     * [6]3. First Startup

0. Preparation

  0.0. Download Program D

   Go to [7]http://aitools.org/downloads and get the latest Program D
   distribution. Grab the package that best fits your needs.

   Program D offers several choices for how it interacts with users.
   Depending upon your needs, you may or may not need to download additional
   software. Here`s a quick guide:

   If you want to...                  Then you need...
   Just talk to the bot yourself via    * The Program D "binary" download
   a command-line style interface       * A Java 5 Runtime Environment
   Let users talk to the bot via        * The Program D "binary" download
   instant messaging programs or IRC    * One or more "listeners"
                                        * A Java 5 Runtime Environment
   Let users talk to the bot through    * The Program D "webapp" download
   a web page                           * A web application server (such as
                                          [8]Tomcat, [9]JBoss, or [10]Jetty)
   Experiment with changes to the       * The Program D "source" download
   Program D source code                * A Java 5 SDK (JDK 1.5.0)

  0.1. Get the Java Runtime (or SDK)

   You need to download and install a Java 2 version 1.5 compatible JVM.
   Examples are the Sun JRE (Java Runtime Edition) or SDK (Software
   Development Kit). The JRE is much smaller than the SDK (~16MB as compared
   with ~44!!!). You only need the SDK if you want to rebuild the program, or
   want to develop Java programs of your own. You can download the necessary
   software and find installation instructions at
   [11]http://java.sun.com/j2se/1.5.0/download.jsp.

   If you want to recompile the program, build the documentation, or rebuild
   a .war file, you will also need [12]Ant.

  0.2. Get Some AIML

   The Program D download includes a test suite for verifying AIML
   compatibility, which is enough to check that your installation is working,
   but doesn`t provide for much of a conversation. At
   [13]http://aitools.org/aiml-sets, you`ll find several choices of
   freely-available AIML. Pick one and download it.

1. Installation

  1.0. Java

   We won`t cover installation instructions for the JRE/Java SDK here. We`ll
   just assume that you`ve followed the instructions. In the following, we`ll
   refer to the directory where you installed Java as JAVA_HOME. (If you use
   Windows, this might be something like C:\jdk1.5.0_06. If you`re using
   Linux, this might be /usr/java/jdk1.5.0_06.)

   You should at least be able to type "java -version" at a command line
   (syntax may vary) and get a response something like:

 java version "1.5.0_06"
 Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_06-b05)
 Java HotSpot(TM) Client VM (build 1.5.0_06-b05, mixed mode, sharing)

   If you get some kind of error like "command not found", check that your
   system path settings are correct. Or, just try "JAVA_HOME/java -version"
   (Linux) or "JAVA_HOME\java.exe -version" (Windows), substituting your
   actual JAVA_HOME path.

  1.1. Program D

   If you`re upgrading from a previous version, please check the [14]release
   notes, since some configuration files may have changed.

   Unzip/untar the Program D download in a convenient location. On Linux, we
   suggest /usr/local/ProgramD; on Windows, the root directory (C:\, perhaps)
   is as good as anything. The unzipping/untarring process will create a
   directory called ProgramD that will contain all the program files.

   Linux users may type:

   tar xvjf programd-4.6-bin.tar.bz2

   Windows 95-2000 users could try a free unzip utility such as [15]7-Zip.
   Windows XP users will find that XP is able to open the zip file and guide
   you through extracting it using a "wizard".

   We`ll refer to the root directory created by the unzip/untar (such as
   /usr/alice/ProgramD) as PROGRAMD.

    1.1.1 Compile (source downloads only)

   If you downloaded the binary version of Program D
   (programd-4.6-bin.tar.bz2 or programd-4.6-bin.zip), you can skip this step
   and go on to [16]1.1.2 Verify Installation.

   If you downloaded the source version of Program D
   (programd-4.6-src.tar.bz2 or programd-4.6-src.zip), you will need to
   compile the code before you can run it. As mentioned above, you will need
   to have installed [17]Ant in order to compile the code. The default target
   in the build.xml file included with Program D will compile the jars:

 [noel@vasya ProgramD]$ ant
 Buildfile: build.xml

 init:

 prepare:
     [mkdir] Created dir: /home/noel/ProgramD/build.tmp

 prepare-src:

 compile:
     [javac] Compiling 174 source files to /home/noel/ProgramD/build.tmp

 jars:
       [jar] Building jar: /home/noel/ProgramD/distrib/programd-main.jar
       [jar] Building jar: /home/noel/ProgramD/distrib/programd-rhino.jar

 BUILD SUCCESSFUL
 Total time: 7 seconds

   NOTE: If you have a release of JDK 1.5 that is older than Update 6
   ("1.5.0_06"), then you will see a few warning messages like this:

     [javac] /home/noel/ProgramD/JavaSource/org/aitools/programd/util/ClassRegistry.java:58: warning: [unchecked] unchecked cast
     [javac] found   : java.lang.Class<capture of ?>
     [javac] required: java.lang.Class<? extends B>
     [javac]             classToRegister = (Class< ? extends B>) Class.forName(classname);
     [javac]                                                                  ^

   However, as you can see from annotations in the source code, all these
   warnings have been evaluated and are known. Prior to Update 6 of JDK 1.5,
   javac did not pay attention to @SuppressWarnings annotations (see SDN
   [18]Bug 4986256). If you use Update 6 or later, you should not see any of
   these warnings. In any case, the compile should proceed without errors.

    1.1.2 Verify Installation

   The Program D distribution includes the set of test cases used to check
   AIML interpreter functionality, and as shipped, it is configured to load
   this small AIML set. Therefore, you can verify the installation "out of
   the box" if you wish, by running the simple console immediately. See
   [19]First Startup for tips on running the simple console. If all is well,
   you should see something like this:

 [noel@vasya ProgramD]$ bin/simple-console

 JAVA_HOME is not set in your environment.
 I found a java executable in "/usr/bin".
 I have set JAVA_HOME to "/usr".
 Please consider setting your JAVA_HOME environment variable.

 [2006-02-23 16:13:20,994] INFO: Starting Program D version 4.6rc1.
 [2006-02-23 16:13:21,022] INFO: Using Java VM 1.5.0_06-b05 from Sun Microsystems Inc.
 [2006-02-23 16:13:21,023] INFO: On Linux version 2.6.15-1.1831_FC4 (i386) with 1 processor(s) available.
 [2006-02-23 16:13:21,085] INFO: 119.1 MB of memory free out of 127.1 MB total in JVM.  Configured maximum: 254.1 MB.
 [2006-02-23 16:13:21,085] INFO: Predicates with no values defined will return: "undefined".
 [2006-02-23 16:13:21,109] INFO: Initializing FlatFileMultiplexor.
 [2006-02-23 16:13:21,110] INFO: Initializing org.aitools.programd.interpreter.RhinoInterpreter.
 [2006-02-23 16:13:21,113] INFO: The AIML Watcher is not active.
 [2006-02-23 16:13:21,146] INFO: Starting up the Graphmaster.
 [2006-02-23 16:13:21,178] INFO: Configuring bot "SampleBot".
 [2006-02-23 16:13:21,679] INFO: Loaded 287 input substitutions.
 [2006-02-23 16:13:21,683] INFO: Loaded 19 gender substitutions.
 [2006-02-23 16:13:21,719] INFO: Loaded 9 person substitutions.
 [2006-02-23 16:13:21,763] INFO: Loaded 60 person2 substitutions.
 [2006-02-23 16:13:21,796] INFO: Loaded 4 sentence-splitters.
 [2006-02-23 16:13:21,826] INFO: Configured testing.
 [2006-02-23 16:13:21,827] INFO: Loading file:/home/noel/ProgramD/resources/testing/AIML.aiml....
 [2006-02-23 16:13:22,207] INFO: 100 unique categories loaded in 1.029 seconds.
 [2006-02-23 16:13:22,208] WARN: 1 path-identical categories were encountered, and handled according to the COMBINE merge policy.
 [2006-02-23 16:13:22,301] INFO: 125.7 MB of memory free out of 127.1 MB total in JVM.  (Configured maximum: 254.1 MB.)
 [2006-02-23 16:13:22,438] INFO: vasya -> SampleBot: "CONNECT"; SampleBot -> vasya: "Connected to test case AIML set."
 Interactive shell: type "/exit" to shut down; "/help" for help.
 [YourBot] user>

   The third line from the bottom indicates that a category from the
   AIML.aiml testsuite has been matched. If you want, you can now run the
   entire test suite to verify functionality by typing /test at the prompt. A
   lot of information will scroll by quickly, ending with a report of the
   number of successful test cases. All cases should succeed:

 [YourBot] user> /test
 [2006-02-23 16:17:49,655] INFO: Loading tests from "file:/home/noel/ProgramD/resources/testing/AIML.xml".
 ...
 [2006-02-23 16:17:52,782] INFO: 109/109 tests succeeded.
 [2006-02-23 16:17:52,836] INFO: Created new test report "/var/log/programd/test-reports/test-report-2006-02-23-16-17-52.xml".

   Please note that on Windows platforms, and possibly others (but not
   Linux), you will need to adjust this item in core.xml in order for the
   test of the <system/> element to succeed:

     <!--The string to prepend to all <system> calls (platform-specific). [String: ]
          * Windows requires something like "cmd /c "; Linux doesn't (just comment out)-->
     <entry key="programd.system-interpreter.prefix"></entry>

   You should also be able to exit the program smoothly:

 [YourBot] user> /exit
 [2006-02-23 16:24:55,543] INFO: Exiting at user request.
 [2006-02-23 16:24:55,544] INFO: Program D is shutting down.
 [2006-02-23 16:24:55,544] INFO: Shutting down all ManagedProcesses.
 [2006-02-23 16:24:55,544] INFO: Finished shutting down ManagedProcesses.
 [2006-02-23 16:24:55,544] INFO: PredicateMaster saving all cached predicates (278)
 [2006-02-23 16:24:55,614] INFO: Shutdown complete.

   Now that you know you have a working installation, please skim through the
   rest of these notes and decide what else you want to configure.

  1.2. AIML

   We suggest creating a subdirectory called "aiml" in your PROGRAMD
   directory, and then arranging your AIML files there in a way that makes
   sense to you. You`ll need to unzip/untar as above.

   Note that, starting with version 4.5 of Program D, your AIML must declare
   the AIML namespace URI. The AIML set you download may not yet have been
   properly formatted in this way; if this is so, you should make the changes
   yourself. The namespace declaration is made on the opening tag of the root
   element of the document (the <aiml> tag). At minimum, you should add this
   declaration:

   xmlns="http://alicebot.org/2001/AIML-1.0.1"

   Also, if your AIML contains HTML markup (which is common in many AIML
   sets), you will need to do two things:

    1. Declare the HTML or XHTML namespace and assign it to a prefix. This
       should also be done on the root element. This may look like:

       xmlns:html="http://www.w3.org/1999/xhtml"

    2. Add the prefix you associated with the HTML/XHTML namespace to all
       HTML/XHTML elements in the document. (This is less onerous than it
       might sound if you use a shell script or batch file.)

       For instance, every instance of

       <br />

       should become

       <html:br/>

   Additionally, please note that your HTML/XHTML markup (all of the markup,
   in fact) must be valid XML--in other words, an opening tag must have a
   corresponding closing tag (<some-element></some-element>), and an empty
   element ("atomic tag") must be properly notated (<some-element/>).

   This will all be obvious to you if you are using contemporary standards
   and standards-based tools, but several years ago the notion of well-formed
   XML was not something universally familiar, and so some AIML sets still
   include orphaned tags and other detritus that needs to be cleaned up.

   So, starting from a root element that looks simply like:

   <aiml>

   you will wind up with something like:

 <aiml version="1.0.1" xmlns="http://alicebot.org/2001/AIML-1.0.1"
       xmlns:html="http://www.w3.org/1999/xhtml">

   --------------------------------------------------------------------------

    optional step

   For purposes of editing AIML locally, you would be well advised to include
   an xsi:schemaLocation notation pointing to a local copy of the AIML
   schema. If you do not, then your XML editing application will attempt to
   connect to the aitools.org server to grab the schema whenever it needs to
   validate AIML, which is probably undesirable (for you and for us). (Note
   that Program D itself will not connect to the aitools.org server--it knows
   to look at the local copy of the schema.) Just as you would keep a copy of
   the XHTML schema locally for validating XHTML, you want to keep and point
   to a copy of the AIML schema. A copy of the AIML schema is included in the
   Program D distribution. There are two ways of pointing to it. The most
   direct approach is to add the following, again to the root element:

 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://alicebot.org/2001/AIML-1.0.1 resources/schema/AIML.xsd"

   However, this has the disadvantage that it hard-codes a local file path in
   your AIML file. (It is also technically incorrect, since the second part
   of the schemaLocation is supposed to be a "canonical" URI.) So, if you are
   using an XML system that supports the use of XML Catalogs, you can enter
   the following:

   xsi:schemaLocation="http://alicebot.org/2001/AIML-1.0.1
   http://aitools.org/aiml/schema/AIML.xsd"

   and then, in your catalog file, include an entry like this:

   <system systemId="http://aitools.org/aiml/schema/AIML.xsd"
   uri="./schema/AIML.xsd"/>

   Again, an example catalog file is included in the Program D distribution.

   --------------------------------------------------------------------------

2. Configuration/Deployment

   There`s a lot to configure with Program D. What`s needed for you really
   depends on how you want to use the program. If you want to use Program D
   with chat interfaces like AOL Instant Messenger or IRC, you will be
   interested in the information about "[20]Chat Listeners". If you are
   putting together a high-volume application, and/or want to do
   database-based log analysis, you will want to refer to the "[21]Database"
   section. If you want to deploy Program D as a web application, the section
   "[22]Deploying the War File" will be important to you. And in any case,
   you will be interested in the first section, about bot configuration:

  2.0. Bots

   Program D lets you configure an unlimited^[23]* number of bots that can
   run at the same time. The bots are configured in the
   PROGRAMD/conf/bots.xml file. (In previous releases, this file was called
   startup.xml.)

   Open the bots.xml file. Notice that the root element is called <bots/>.
   Inside <bots/>, we place one or more <bot/> elements. These <bot/>
   elements are not the same as the AIML tag of the same name.

   Each <bot/> element has two important attributes: id and enabled. The
   first one assigns an identifier, which should be unique, for the bot. The
   identifier will be used internally by the engine and will be written to
   some log resources. The enabled attribute should have either of the values
   "true" or "false". If the value is "true", then Program D will try to load
   that bot when the server starts up. Switching enabled to "false" is an
   easy way to quickly turn off a bot configuration that you don`t want to
   use (although a restart is required).

   Within the <bot> element we define bot properties, listeners, default
   predicates, substitutions, sentence-splitters and learn directives.

    2.0.0. Bot Properties

   Bot properties are predicates that cannot be changed during the runtime
   life of the bot, but which can be included in AIML patterns for matching.
   A common property to define for a bot is "name". Bot properties are
   defined in individual <property> elements inside a bot`s <properties>
   element, as in this example:

 <property name="master" value="Joe Schmoe"/>

   This associates the bot property name "master" with the value "Joe
   Schmoe".

   The included bots.xml file uses a <properties/> element with a href
   attribute that points to a separate file, properties.xml. This approach,
   which can also be used for predicates, sentence splitters, and
   substitutions, allows easier maintenance of these items, and makes it
   possible for multiple bots to use the same sets of these elements. You
   can, if you wish, also define these items in the same file, as children of
   the <properties/> (or other) element, omitting the href attribute.

   Properties don`t mean anything unless your AIML uses them. You can display
   the value of a bot property inside an AIML template by using the form <bot
   name="property-name"/>.

    (2.0.1. Listeners)

   Listeners are discussed in more detail [24]below.

    2.0.2. Default Predicates

   Default predicates can be thought of as your bot`s "assumptions" about new
   users. While AIML allows you to use <set
   name="predicate-name">...something...</set> and <get
   name="predicate-name"/> forms without any sort of "declaration", Program D
   does allow you to set default values to associate with any predicate names
   you wish, so that you have more control over what is returned by <get
   name="predicate-name"/> if a corresponding <set> has not yet happened for
   a given user.

   The <predicates/> element, if it exists, may directly include one or more
   <predicate/> children, or it may use an "href" attribute, as in the
   provided example, that points to another file where the predicates are
   defined.

   You can also use <predicate/> elements to mark a predicate as
   [25]return-name-when-set. For instance, this predicate definition is
   included in a sample file:

   <predicate name"he" default="somebody" set-return="name"/>

   This means that when <set name="he">...</set> is included in a template,
   the name of the predicate, "he", will be displayed, rather than whatever
   value is associated with the name by the <set/>.

    2.0.3. Substitutions

   Substitutions have several different purposes, depending on their type.
   Input substitutions contribute to the process of [26]input normalization.
   Person substitutions provide macros for transformations by the
   [27]<person/> element; likewise Person2 and Gender apply to the
   [28]<person2/> and [29]<gender/> elements, respectively.

   Starting in version 4.5, the find attributes of substitutions are parsed
   as regular expressions. This lends a great deal more power and precision
   to the input normalization step, and to the Gender, Person and Person2
   processors. For a guide to the regular expression syntax that is
   available, see the [30]JDK documentation.

   Each individual substitution specification, regardless of whether it is
   inside an <input/>, <gender/>, <person/> or <person2/>, takes the same
   form as this example from conf/substitutions.xml:

 <substitute find="\bbecasue\b" replace="because"/>

   This means that, when this substitution is applied, each instance of the
   separate word "becasue" will be replaced with "because". (This is an
   example of typo correction in Program D.)

   Note the use of the \b marker, which is regular expression notation for
   "word boundary". In previous versions of Program D, you were advised to
   pad find and replace strings with spaces, as a way of approximating word
   boundary matching. This is no longer advised, and in fact will not work
   properly now that regular expression support is included.

    2.0.4. Sentence-Splitters

   Sentence-splitters, as described in the [31]AIML spec, are:

   ...heuristics applied to an input that attempt to break it into
   "sentences". The notion of "sentence", however, is ill-defined for many
   languages, so the heuristics for division into sentences are left up to
   the developer.

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
   pointed to by the href attribute on the <sentence-splitters/> element.

    2.0.5. Testing Configuration

   A bot can be configured with a test suite--a set of test cases--designed
   to test that a bot gives appropriate responses to particular inputs.
   Within the <testing/> element, you specify a <test-suite-path/, which
   should point to a file somewhere that matches the test suite schema
   (documented elsewhere), and a <report-directory/>, which is where reports
   of individual test runs will be written. As shipped, Program D is
   configured to use its standard AIML test suite, and to write reports to a
   directory /var/log/programd/test-reports (On Windows this will be
   translated to C:\var\log\programd\test-reports, substituting the current
   drive letter for C:.)

    2.0.6. Learn Directives

   Learn directives function identically to the [32]<learn/> element in AIML,
   but in this context are processed immediately upon startup of Program D.

   You can use simple "glob"-like wildcard patterns for specifying files to
   load. For example, the shipped startup.xml file has this single learn
   directive:

 <learn>../aiml/standard/*.aiml</learn>

   The path specification is relative to the current file, so this indicates
   that within a directory reached by going one level up (to the main
   PROGRAMD directory), and then into a directory called aiml, and from there
   into a subdirectory called standard, the program should load all files
   that end in .aiml. Note that files containing AIML are not required to
   have this or any particular suffix; also, if you direct the program to
   learn files that do not contain any AIML, you will receive a warning but
   nothing will be loaded from that file.

  2.1. Listeners

   In current Program D nomenclature, a "listener" is a program that knows
   how to speak a particular protocol and interpret messages for the bot. For
   instance, the IRCListener understands the popular IRC protocol and allows
   you to connect your bot to any IRC server.

   You can enable one or more listeners for each bot. Each listener has its
   own unique configuration parameters, which are specific to the
   requirements of the protocol for which it`s designed. You`ll find examples
   of each currently-available listener in the PROGRAMD/conf/bots.xml file.

   Listeners for a given bot are specified within the <listeners/> element,
   in individual <listener/> elements. Each <listener> element must have two
   attributes: class, and enabled. The value for class must be the
   fully-qualified class name of a listener that is somewhere in the Java
   classpath for Program D. The example bots.xml includes an example of each
   type currently known. The value of enabled must be "true" or "false",
   allowing the listener to be switched on or off with ease.

   The parameters for a listener are specifed in individual <parameter/>
   elements within the <listener/>. For example:

 <parameter name="host" value="irc.freenode.net"/>
 <parameter name="port" value="6667"/>
 <parameter name="nick" value="your-nick"/>
 <parameter name="channel" value="#some-channel"/>

   These are the parameters set for the example use of the IRCListener, whose
   class is "org.aitools.programd.listener.IRCListener".

   When you enable a chat listener, you will see its output mixed in with the
   console output. You can also interact with some listeners if they
   implement the "commandable" interface (see [33]below).

  2.2. Database

   You are not required to install, configure or use a database in order to
   use Program D. The default configuration is entirely based on text files
   in order to make setup quick, painless, and no more resource-intensive
   than necessary.

   However, for heavy-volume situations, and/or for cases where you want to
   collect dialogue in a form that can perhaps be more easily mined, you may
   wish to use a database.

   We have done the most extensive testing with [34]MySQL; however, other
   users have connected Program D with various other DBMSes. The major point
   is that you must have a JDBC driver for the database you wish to use. We
   include the MySQL JDBC driver with the Program D distribution for
   convenience.

   If you do want to use a database, you need to configure it properly in the
   PROGRAMD/core.xml file. The relevant part of the file is this:

     <!--
     DATABASE CONFIGURATION
          * This is only meaningful if you are using a database-enabled Multiplexor
          * and/or the database-based chat logging.
     -->

     <!--Typical mySQL configuration-->

     <!--The URL of the database to use. [String: jdbc:mysql:///programdbot]-->
     <entry key="programd.database.url">jdbc:mysql:///programdbot</entry>

     <!--The database driver to use. [String: com.mysql.jdbc.Driver]-->
     <entry key="programd.database.driver">com.mysql.jdbc.Driver</entry>

     <!--The maximum number of simultaneous connections to the database. [int: 25]-->
     <entry key="programd.database.connections">25</entry>

     <!--The username which with to access the database. [String: programd]-->
     <entry key="programd.database.user">programd</entry>

     <!--The password for the database. [String: yourpassword]-->
     <entry key="programd.database.password">yourpassword</entry>

   You should set these values to match your database installation. Remember
   that the driver class must be available from the Java classpath.

    2.2.0. DBMultiplexor

   If you want to store predicates in the database, you must enable the
   DBMultiplexor. This can be done by switching the comment marks in
   PROGRAMD/core.xml so that this:

     <!--The Multiplexor to use. [String: org.aitools.programd.multiplexor.FlatFileMultiplexor]-->
     <entry key="programd.multiplexor-classname">org.aitools.programd.multiplexor.FlatFileMultiplexor</entry>
     <!--<entry key="programd.multiplexor">org.aitools.programd.multiplexor.DBMultiplexor</entry>-->

   becomes this:

     <!--The Multiplexor to use. [String: org.aitools.programd.multiplexor.FlatFileMultiplexor]-->
     <!--<entry key="programd.multiplexor-classname">org.aitools.programd.multiplexor.FlatFileMultiplexor</entry>-->
     <entry key="programd.multiplexor">org.aitools.programd.multiplexor.DBMultiplexor</entry>

   If DBMultiplexor is enabled, Program D will expect to find two tables
   called users and predicates in the configured database. The SQL commands
   for creating these tables are in PROGRAMD/database/db-multiplexor.script.

    2.2.1. Database Chat Logging

   Database chat logging stores each exchange with the bot in a database.
   This is useful for monitoring conversations, data mining, etc. To enable
   this feature, you must uncomment this item in conf/log4j.xml:

     <!--Enable chat logging to the database? [boolean: false]
         * Be sure that the database configuration (later in this file) is valid.-->
     <entry key="programd.logging.to-database.chat">true</entry>

   (in PROGRAMD /core.xml). If this is enabled, Program D will expect to find
   a table called chatlog in the database. The command for creating this
   table is in PROGRAMD/database/db-chatlog.script. Note that if you are
   logging chats to a database, you might want to disable the redundant
   logging to XML files. You can do this in the core.xml file.

  2.3. Shell and Console

   In its usual configuration, Program D displays information about what it
   is doing while it starts, runs, and shuts down. We call this the
   "console". Also, you can interact with Program D via a simple shell, if
   desired. Sometimes it is simpler to use this shell than to open a web
   browser or IM program. Both the console and shell can be configured to
   suit your needs. Configuration for the format of messages to the console
   is done in the PROGRAMD/conf/log4j.xml file.

   If you want to run Program D as a background process, you should turn off
   the interactive shell. Otherwise, depending upon your operating system,
   the program may be "suspended" or may not run properly. To turn off the
   shell, set the programd.console.use-shell property in core.xml to false.

  2.5. AIMLWatcher

   The AIML Watcher is a useful feature that watches to see if you change
   your AIML files. This allows you to update AIML without restarting the
   bot.

   As a security feature, the AIML Watcher only watches files that were
   loaded by a <learn/> directive. If you put a new file into a directory
   with other AIML files, that file will not be automatically loaded (but you
   can load it with the /load shell command if you have access to the console
   and have not disabled the shell).

   You can enable the AIML Watcher by setting programd.use-watcher to true in
   core.xml.

  2.6 Deploying to a Web Application Server

   Program D can be deployed as a .war file to a J2EE web application server.
   If you download the programd-4.6-war.tar.bz2 (or .zip) file, then you get
   a ready-to-go .war file, along with two other directories that need to be
   placed on your application server's filesystem.

   The conf directory contains configuration files needed to run Program D.
   These could be included directly in the .war file, but for your
   convenience, they are provided separately so you do not need to recreate
   the .war file. (The web.xml file included in the .war file expects to find
   this directory at /var/programd/conf/core.xml. If you are unable to place
   the directory there, or do not wish to, you must recreate the .war file.)

   The resources directory is only a subset of the full resources directory
   from the source download, and contains only the test AIML. You do not, of
   course, need to use it if you are providing your own AIML. It is
   referenced by conf/bots.xml.

   So, in the simplest example, using Tomcat, you would copy the conf and
   resources directories to /var/programd, and then upload the .war file
   using the Tomcat manager application:

   [35]Deploying the Program D war file on Tomcat

   You may, however, prefer to download the "source" distribution so that you
   can easily see the whole set of files included in Program D, and make
   changes. If you wish to rebuild the .war file, you can type ant war from
   within the main Program D directory. Like all other items, the building of
   the .war file is controlled by the build.xml file.

3. First Startup

  3.0. Starting Program D

    3.0.0. Running from the command line

   There are two sample "configurations" provided for running Program D
   directly from a command line (or batch/shell script). Additional
   configurations can easily be created, to customize the environment in
   which you run Program D.

   To start Program D in the simplest way, run PROGRAMD/bin/simple-console
   (*nix) or PROGRAMD\bin\simple-console.bat (Windows).

   If you`re using Windows and you launch the batch file by double-clicking
   it, but there are some configuration problems, the console window might
   disappear before you have time to read the messages. You are advised in
   this case to open a command prompt and run the batch file from there.

   If you`re using Linux, you may need to make the
   PROGRAMD/bin/simple-console file executable by issuing the command:

   chmod +x simple-console

   Starting the server script should look something like this:

 [noel@vasya ProgramD]$ bin/simple-console

 JAVA_HOME is not set in your environment.
 I found a java executable in "/usr/bin".
 I have set JAVA_HOME to "/usr".
 Please consider setting your JAVA_HOME environment variable.

 [2006-02-27 14:11:33,254] INFO: Starting Program D version 4.6rc1.
 [2006-02-27 14:11:33,305] INFO: Using Java VM 1.5.0_06-b05 from Sun Microsystems Inc..
 [2006-02-27 14:11:33,307] INFO: Running on Linux version 2.6.15-1.1831_FC4 (i386) with 1 processor available.
 [2006-02-27 14:11:33,356] INFO: 126.0 MB of memory free out of 127.1 MB total in JVM.  Configured maximum: 254.1 MB.
 [2006-02-27 14:11:33,357] INFO: Predicates with no values defined will return: "undefined".
 [2006-02-27 14:11:33,357] INFO: Initializing FlatFileMultiplexor.
 [2006-02-27 14:11:33,359] INFO: Initializing org.aitools.programd.interpreter.RhinoInterpreter.
 [2006-02-27 14:11:33,457] INFO: The AIML Watcher is active.
 [2006-02-27 14:11:33,457] INFO: Starting up the Graphmaster.
 [2006-02-27 14:11:33,463] INFO: Loading "file:/home/noel/workspace/ProgramD/conf/bots.xml".
 [2006-02-27 14:11:33,563] INFO: Configuring bot "SampleBot".
 [2006-02-27 14:11:33,611] INFO: Loading "file:/home/noel/workspace/ProgramD/conf/properties.xml".
 [2006-02-27 14:11:33,778] INFO: Loading "file:/home/noel/workspace/ProgramD/conf/predicates.xml".
 [2006-02-27 14:11:33,836] INFO: Loading "file:/home/noel/workspace/ProgramD/conf/substitutions.xml".
 [2006-02-27 14:11:34,009] INFO: Loaded 287 input substitutions.
 [2006-02-27 14:11:34,011] INFO: Loaded 19 gender substitutions.
 [2006-02-27 14:11:34,058] INFO: Loaded 9 person substitutions.
 [2006-02-27 14:11:34,118] INFO: Loaded 60 person2 substitutions.
 [2006-02-27 14:11:34,119] INFO: Loading "file:/home/noel/workspace/ProgramD/conf/sentence-splitters.xml".
 [2006-02-27 14:11:34,177] INFO: Loaded 4 sentence-splitters.
 [2006-02-27 14:11:34,179] INFO: Loading "file:/home/noel/workspace/ProgramD/conf/listeners.xml".
 [2006-02-27 14:11:34,280] INFO: Configured testing.
 [2006-02-27 14:11:34,281] INFO: Loading file:/home/noel/workspace/ProgramD/resources/testing/AIML.aiml....
 [2006-02-27 14:11:34,841] INFO: 135 categories loaded in 1.2740 seconds.
 [2006-02-27 14:11:34,841] INFO: 135 total categories currently loaded.
 [2006-02-27 14:11:34,842] WARN: 1 path-identical categories were encountered, and handled according to the COMBINE merge policy.
 [2006-02-27 14:11:34,961] INFO: 125.7 MB of memory free out of 127.1 MB total in JVM.  Configured maximum: 254.1 MB.
 [2006-02-27 14:11:35,058] INFO: vasya -> SampleBot: "CONNECT"; SampleBot -> vasya: "Connected to test case AIML set."
 Interactive shell: type "/exit" to shut down; "/help" for help.
 [YourBot] user>

   If there are any errors while starting up, an explanation will be printed
   and the server will shut down. If you encounter a confusing error message,
   please [36]file a bug report!!!

    3.0.1. Running from a web server

   Every application server will be different in how it allows you to start
   and stop Program D. For instance, using [37]Tomcat` web application
   manager you would start the application by clicking the "Start" link:

   [38]Starting Program D on Tomcat

   When the application has started, you can click the link from the Tomcat
   manager to interact with Program D through the default web interface,
   where you can try various test inputs:

   [39]The default Program D web interface

   Note that the example page provided demonstrates the use of the Ajax
   capabilities now provided--inputs are sent to the bot, and answers
   retrieved, without requiring a page refresh. This makes for a much more
   pleasing user experience. Take a look at the file TalkToBot.jspx and
   programd.css files to see how this works.

   The interactive shell is not available when you run Program D as a web
   application.

  3.1. Understanding the Console

   While the bot is running, information will be printed to the console to
   tell you what`s going on.

    3.1.1. Shutdown Messages

   Before the bot server can shut down, it must stop the http server and must
   save any predicates left in the cache. It also must stop all listeners.
   There are several different [40]ways to shut down, but all of them should
   cause Program D to print information that looks like this:

 [2006-02-27 14:12:28,713] INFO: Exiting at user request.
 [2006-02-27 14:12:28,714] INFO: Program D is shutting down.
 [2006-02-27 14:12:28,714] INFO: Shutting down all ManagedProcesses.
 [2006-02-27 14:12:28,714] INFO: Finished shutting down ManagedProcesses.
 [2006-02-27 14:12:28,714] INFO: PredicateMaster saving all cached predicates (2)
 [2006-02-27 14:12:28,824] INFO: Shutdown complete.

   "ManagedProcesses" are such things as the http server and listeners.
   Sometimes one or more of these may take a long time to shut down. Unless
   you are in a great hurry, you should allow this process to complete
   properly. The saving of cached predicates should be very fast.

  3.2. Using the Shell

   The simplest use of the shell is to talk to your bot(s). However, there
   are some other capabilities available to you. All shell commands in
   Program D are preceded by a / (slash) character. If you type "/help" at a
   shell prompt, you can see the list of available commands:

 [YourBot] user> /help
 Shell commands are preceded by a "/".  Available commands:
 /memory                       shows statistics on free/available memory
 /categories                   lists the number of categories currently loaded
 /bots                         lists loaded bots
 /test suite [run-count]       runs specified test suite on current bot
 /who                          prints the id of the current bot
 /aiml aiml-fragment           tries to process a fragment of template-side AIML
 /files                        lists the files loaded by the current bot
 /load filename                loads/reloads given filename for active bot
 /help                         prints this help
 /commandables                 lists available "shell commandables" (such as some listeners)
 /unload filename              unloads given filename for active bot
 /talkto botid                 switches conversation to given bot

    3.2.0. Loading AIML

   The /load</> command lets you load new AIML files, or reload files (useful
   if you have disabled the [41]AIML Watcher.)

    3.2.1. Unloading AIML

   The /unload command lets you unload an AIML file that is currently in
   memory. You must unload using the pathname that was originally used to
   load it, which can be a little tricky to get right.

    3.2.2. Listing Loaded Bots

   You can see which bots are currently loaded with the /bots command.

    3.2.3. Talking to a Different Bot

   You can switch the bot with whom you are conversing by using the /talkto
   command. You must use a valid bot id as the argument. (The bot id is not
   necessarily the same as the value associated with its "name" property. To
   see bot ids, look at your startup.xml file or use the /bots command.

   Each time you switch bots, the connect string will be sent to the bot.

   Trying to talk to a botid that doesn`t exist will result in an error
   message.

  3.3. Connecting via a Chat Interface

   If you have correctly configured one or more listeners (see
   [42]Configuring Listeners above, then you should be able to start the
   appropriate chat client and communicate with your bot. Add your bot to
   your IM client`s contact list so you can see when the bot successfully
   comes online.

  3.4. Shutting Down

   If you start Program D with the shell enabled, you can shut down the bot
   from the console by typing /exit command.

   If the shell is disabled, you can shut down the bot by pressing the
   interrupt key sequence for your operating system; this is often Ctrl-C.
   Or, on *nix systems, you can send the main JVM process a SIGINT signal
   with the kill command.

   --------------------------------------------------------------------------

   ^[43]*The number of bots you can run with Program D is not limited by any
   aspect of the program, but will of course be constrained by resources.
   There is no simple metric to use when determining requirements--for
   instance, how many kilobytes per category--since categories themselves can
   vary in size (according to the template size), and since the degree of
   compression of one category in the Graphmaster depends upon all the other
   categories that are present. Roughly speaking, the more categories and/or
   AIML files that your bots have in common, the more bots you can load with
   a given amount of memory; and, of course, the more memory you have, the
   more bots you can load.

   [44]Valid XHTML 1.0!

References

   Visible links
   1. mailto:noel@aitools.org
   2. http://aitools.org/
   3. file:///home/noel/workspace/ProgramD/docs/readme.html#preparation
   4. file:///home/noel/workspace/ProgramD/docs/readme.html#installation
   5. file:///home/noel/workspace/ProgramD/docs/readme.html#configuration
   6. file:///home/noel/workspace/ProgramD/docs/readme.html#startup
   7. http://aitools.org/downloads
   8. http://tomcat.apache.org/
   9. http://www.jboss.com/developers/index
  10. http://jetty.mortbay.org/jetty/index.html
  11. http://java.sun.com/j2se/1.5.0/download.jsp
  12. http://ant.apache.org/
  13. http://aitools.org/aiml-sets
  14. file:///home/noel/workspace/ProgramD/docs/release-notes.html
  15. http://www.7-zip.org/
  16. file:///home/noel/workspace/ProgramD/docs/readme.html#1.1.2-verify-installation
  17. http://ant.apache.org/
  18. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4986256
  19. file:///home/noel/workspace/ProgramD/docs/readme.html#startup
  20. file:///home/noel/workspace/ProgramD/docs/readme.html#config-listeners
  21. file:///home/noel/workspace/ProgramD/docs/readme.html#config-database
  22. file:///home/noel/workspace/ProgramD/docs/readme.html#config-war
  23. file:///home/noel/workspace/ProgramD/docs/readme.html#footnote-unlimited-bots
  24. file:///home/noel/workspace/ProgramD/docs/readme.html#config-listeners
  25. http://aitools.org/aiml/spec#section-aiml-predicate-behaviors
  26. http://aitools.org/aiml/spec#section-input-normalization
  27. http://aitools.org/aiml/spec#section-person
  28. http://aitools.org/aiml/spec#section-person2
  29. http://aitools.org/aiml/spec#section-gender
  30. http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Pattern.html#sum
  31. http://aitools.org/aiml/spec#section-sentence-splitting-normalizations
  32. http://aitools.org/aiml/spec#section-learn
  33. file:///home/noel/workspace/ProgramD/docs/readme.html#commandables
  34. http://www.mysql.com/
  36. http://bugs.aitools.org/
  37. http://tomcat.apache.org/
  40. file:///home/noel/workspace/ProgramD/docs/readme.html#shutting-down
  41. file:///home/noel/workspace/ProgramD/docs/readme.html#aiml-watcher
  42. file:///home/noel/workspace/ProgramD/docs/readme.html#config-listeners
  43. file:///home/noel/workspace/ProgramD/docs/readme.html#footnote-unlimited-bots-backlink
  44. http://validator.w3.org/check/referer
