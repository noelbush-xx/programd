@echo off
rem ==========================================================================
rem Alice Program D
rem Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
rem 
rem This program is free software; you can redistribute it and/or
rem modify it under the terms of the GNU General Public License
rem as published by the Free Software Foundation; either version 2
rem of the License, or (at your option) any later version.
rem
rem You should have received a copy of the GNU General Public License
rem along with this program; if not, write to the Free Software
rem Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
rem USA.
rem ==========================================================================

set ENVTEST=This just checks whether there is additional environment space.
if "%ENVTEST%"=="" goto increase_env
set ENVTEST=

rem Edit the following line to point to your java JDK directory, and remove the "rem"
rem set JAVA_HOME=c:\jdk1.3

if not "%JAVA_HOME%"=="" goto java_home

:nojava_home
rem Try to guess the location (the default install dirs for the JDK)
if exist c:\jdk1.3\bin\javac.exe set JAVA_HOME=c:\jdk1.3
if exist c:\jdk1.3.1_0\bin\javac.exe set JAVA_HOME=c:\jdk1.3.1_0
if exist c:\jdk1.4.0\bin\javac.exe set JAVA_HOME=c:\jdk1.4.0
if exist c:\j2sdk1.4.0\bin\javac.exe set JAVA_HOME=c:\j2sdk1.4.0
if exist c:\jdk1.4.1\bin\javac.exe set JAVA_HOME=c:\jdk1.4.1
if exist c:\j2se1.4.1\bin\javac.exe set JAVA_HOME=c:\j2se1.4.1
if not "%JAVA_HOME%"=="" goto java_home

echo For the Ant buildsystem to work, you must set the JAVA_HOME environment
echo variable. You can edit build.bat file to set it.
pause
goto end

:java_home
set JAVA_TOOLS=%JAVA_HOME%/lib/tools.jar
set SERVLET_LIB=../lib/servlet.jar
set ALICE_LIB=../lib/aliceserver.jar
set JS_LIB=../lib/js.jar
set ANT_LIB=../lib/ant.jar

rem These are for Jetty; you will want to change these if you are using a different http server.
set HTTP_SERVER_LIBS=../lib/org.mortbay.jetty.jar;../lib/com.sun.net.ssl.jar;../lib/javax.servlet.jar;../lib/javax.xml.jaxp.jar;../lib/org.apache.crimson.jar

set BUILD_CLASSPATH=%JAVA_TOOLS%;%ANT_LIB%;%SERVLET_LIB%;%ALICE_LIB%;%JS_LIB%;%HTTP_SERVER_LIBS%
if "%BUILD_CLASSPATH%"=="" goto increase_env

set ANT_HOME=./
if "%ANT_HOME%"=="" goto increase_env

rem This makes sure build.bat and build.sh are modifiable, since Ant likes to touch them.
attrib -r build.bat
attrib -r build.sh

echo Starting Ant...
java.exe -Dant.home=%ANT_HOME% -classpath %BUILD_CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5
goto end

:increase_env
echo Increasing environment space
if not exist %comspec% goto nocomspec

:comspec
rem %comspec% points to an existing command interpreter
%comspec% /E:4096 /C build.bat %1 %2 %3 %4 %5
goto end

:nocomspec
rem %comspec% is not set, trying command.com
command /E:4096 /C build.bat %1 %2 %3 %4 %5
goto end

:end