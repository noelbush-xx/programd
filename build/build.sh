#!/bin/sh

JAVA_TOOLS=$JAVA_HOME/lib/tools.jar;
LOCALCLASSPATH=$JAVA_TOOLS:../lib/mail.jar:../lib/activation.jar:../lib/pop3.jar:../lib/smtp.jar:../lib/imap.jar:../lib/nql.jar:../lib/speech.jar:../lib/jxta.jar:../lib/jxtashell.jar:../lib/aliceserver.jar:../lib/js.jar:../lib/xml.jar:../lib/servlet.jar:../lib/mail.jar:../lib/ant.jar:../lib/sql.jar:
ANT_HOME=./

echo Building with classpath $LOCALCLASSPATH

echo Starting Ant...

$JAVA_HOME/bin/java -Dant.home="$ANT_HOME" -classpath "$LOCALCLASSPATH" org.apache.tools.ant.Main $1 $2 $3 $4


