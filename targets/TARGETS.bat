@echo off
@echo Starting Alicebot.Net 4.0 Beta Targeting Utility ...

@set ALICEHOME=../.
@set XMLLIB=../lib/xml.jar
@set SQLLIB=../lib/sql.jar
@set MAILLIB=../lib/mail.jar
@set SERVLETLIB=../lib/servlet.jar
@set ALICELIB=../lib/aliceserver.jar
@set JSLIB=../lib/js.jar
@set JXTALIB=../lib/jxta.jar
@set JXTASHELLLIB=../lib/jxtashell.jar
@set SPEECHLIB=../lib/speech.jar
@set NQLLIB=../lib/nql.jar
@set JSSELIB=../lib/jsse.jar

@set CLASSPATH=%ALICEHOME%;%XMLLIB%;%SQLLIB%;%MAILLIB%;%SERVLETLIB%;%ALICELIB%;%JSLIB%;%JXTALIB%;%JXTASHELLLIB%;%SPEECHLIB%;%NQLLIB%;%JSSELIB%;

java -classpath %CLASSPATH% -Xms64m -Xmx64m -Djava.library.path=./bin org.alicebot.server.core.util.Targets %1 %2 %3 %4 %5 %6 %7 %8 %9

