@echo off

set JAVA_TOOLS=%JAVA_HOME%\lib\tools.jar;
set LOCALCLASSPATH=%JAVA_TOOLS%;../lib/mail.jar;../lib/activation.jar;../lib/pop3.jar;../lib/smtp.jar;../lib/imap.jar;../lib/nql.jar;../lib/speech.jar;../lib/jxta.jar;../lib/jxtashell.jar;../lib/aliceserver.jar;../lib/js.jar;../lib/xml.jar;../lib/servlet.jar;../lib/mail.jar;../lib/ant.jar;../lib/sql.jar;
set ANT_HOME=./

echo Building with classpath %LOCALCLASSPATH%

echo Starting Ant...

%JAVA_HOME%\bin\java.exe -Dant.home="%ANT_HOME%" -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5


