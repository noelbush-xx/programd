#!/bin/sh

echo Starting Alicebot.Net 4.0 Beta Targeting Utility ...

ALICEHOME=../
XMLLIB=../lib/xml.jar
SQLLIB=../lib/sql.jar
MAILLIB=../lib/mail.jar
SERVLETLIB=../lib/servlet.jar
ALICELIB=../lib/aliceserver.jar
JSLIB=../lib/js.jar
JXTALIB=../lib/jxta.jar
JXTASHELLLIB=../lib/jxtashell.jar
SPEECHLIB=../lib/speech.jar
NQLLIB=../lib/nql.jar
JSSELIB=../lib/jsse.jar

CLASSPATH=$ALICEHOME:$XMLLIB:$SQLLIB:$MAILLIB:$SERVLETLIB:$ALICELIB:$JSLIB:$JXTALIB:$JXTASHELLLIB:$SPEECHLIB:$NQLLIB:$JSSELIB

java -classpath $CLASSPATH -Xms64m -Xmx64m -Djava.library.path=./bin org.alicebot.server.core.util.Targets $1 $2 $3 $4

