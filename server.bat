@echo off
rem This is a shell script for starting Alicebot Server. Currently,this
rem script must be run from the server directory containing the
rem SERVER.properties file.  See http://www.alicebot.org for more information.

@set ALICE_HOME=.
@set XML_LIB=lib/xml.jar
@set SQL_LIB=lib/sql.jar
@set MAIL_LIB=lib/mail.jar
@set SERVLET_LIB=lib/servlet.jar
@set ALICE_LIB=lib/aliceserver.jar
@set JS_LIB=lib/js.jar
@set JXTA_LIB=lib/jxta.jar
@set JXTASHELL_LIB=lib/jxtashell.jar
@set SPEECH_LIB=lib/speech.jar
@set NQL_LIB=lib/nql.jar
@set JSSE_LIB=lib/jsse.jar
@set MAIL_LIB=lib/mail.jar
@set POP3_LIB=lib/pop3.jar
@set ACTIVATION_LIB=lib/activation.jar
@set SMTP_LIB=lib/smtp.jar

@set CLASSPATH=%ALICE_HOME%;%XML_LIB%;%SQL_LIB%;%MAIL_LIB%;%SERVLET_LIB%;%ALICE_LIB%;%JS_LIB%;%JXTA_LIB%;%JXTASHELL_LIB%;%SPEECH_LIB%;%NQL_LIB%;%JSSE_LIB%;%MAIL_LIB%;%POP3_LIB%;%SMTP_LIB%;%IMAP_LIB%;%ACTIVATION_LIB%;

java.exe -classpath %CLASSPATH% -Xms64m -Xmx64m -Djava.library.path=./bin org.alicebot.server.net.AliceServer
