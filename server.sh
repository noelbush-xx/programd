#!/bin/sh
#
# This is a shell script for starting Alicebot.Net Server. Currently, this
# script must be run from the server directory containing the
# SERVER.properties file.  See <http://www.alicebot.net> for more information.
#
echo "Starting Alicebot.Net 4.1.0 Reference Server ..."

ALICE_HOME="."

XML_LIB=./lib/xml.jar
SQL_LIB=./lib/sql.jar
MAIL_LIB=./lib/mail.jar
POP3_LIB=./lib/pop3.jar
SMTP_LIB=./lib/smtp.jar
IMAP_LIB=./lib/imap.jar
ACTIVATION_LIB=./lib/activation.jar
SERVLET_LIB=./lib/servlet.jar
ALICE_LIB=./lib/aliceserver.jar
JS_LIB=./lib/js.jar
JXTA_LIB=./lib/jxta.jar
JXTA_SHELL_LIB=./lib/jxtashell.jar
JAVA_NQL_LIB=./lib/nql.jar

CLASSPATH=$ALICE_HOME:$XML_LIB:$SQL_LIB:$MAIL_LIB:$POP3_LIB:$SMTP_LIB:$IMAP_LIB:$ACTIVATION_LIB:$SERVLET_LIB:$ALICE_LIB:$JS_LIB:$JXTA_LIB:$JXTA_SHELL_LIB:$JAVA_NQL_LIB

java -classpath $CLASSPATH -Xms64m -Xmx64m org.alicebot.server.net.AliceServer
