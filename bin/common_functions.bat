rem ==========================================================================
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

rem This contains functions used by batch scripts.
rem This file itself is not meant to be executed.
rem You must pass a parameter that corresponds to a labeled section of the batch file.

goto %1

rem Checks for needed environment space and increases it if necessary.
:check_env
  set ENVTEST=This just checks whether there is additional environment space.
  if "%ENVTEST%"=="" goto increase_env
  set ENVTEST=
  goto end

rem Increases environment space.
:increase_env
  echo Increasing environment space
  if not exist %comspec% goto nocomspec

:comspec
  rem %comspec% points to an existing command interpreter
  %comspec% /E:4096 /C %2 %3 %4 %5
  goto end

:nocomspec
  rem %comspec% is not set, trying command.com
  command /E:4096 /C %2 %3 %4 %5
goto end

rem Sets the BASE variable to the directory above the bin directory.
:set_base
  pushd %~p0\..
  if %OS%'==Windows_NT' (for %%d in (.) do set BASE=%%~fd&goto done)
  echo @prompt set BASE=$P$_>%TEMP%.\#ETCD1.BAT
  %comspec% /c %temp%.\#etcd1.bat > %temp%.\#etcd2.bat
  call %temp%.\#etcd2.bat
  del %temp%.\#etcd?.bat
  :done
  popd
goto end

rem Starts Program D using a given main class.
:start_programd

  rem Set up the Program D variables.
  if "%quit%"=="" call %0 setup_programd

  rem Set up the Java environment.
  if "%quit%"=="" call %0 setup_java

  rem Set up other paths to other needed jars and check their existence.
  if "%quit%"=="" call %0 check_programd_lib

  if not "%quit%"=="" goto end

  rem Set SQL_LIB to the location of your database driver.
  rem No warning is provided if it cannot be found.
  set SQL_LIB=%LIBS%\mysql_comp.jar

  rem Concatenate all paths into the classpath to be used.
  set PROGRAMD_CLASSPATH=%SERVLET_LIB%;%PROGRAMD_LIB%;%JS_LIB%;%SQL_LIB%;%HTTP_SERVER_LIB%

  rem Change to the Program D bin directory and start the main class.
  pushd %BASE%\bin
  %JVM_COMMAND% -classpath %PROGRAMD_CLASSPATH% -Xms64m -Xmx%3m %2 %4

  rem On exit, leave the base directory.
  popd
goto end


rem Sets up the lib directory.
:setup_lib_dir
  set LIBS=%BASE%\lib
goto end


rem Checks that the programd.jar exists.
:check_programd_lib

  rem Set lib directory (jars)
  call %0 setup_lib_dir
  
  set PROGRAMD_LIB=%LIBS%\programd.jar
  if exist "%PROGRAMD_LIB%" goto end
  echo I can\'t find your programd.jar file.  Have you compiled it?
  echo If you downloaded the source-only version but don\'t have
  echo a Java compiler, you can download a pre-compiled version from
  echo http://aitools.org/downloads/
  echo
  set quit=yes
goto end


rem Sets up some variables used to run/build Program D.
rem First argument should be "building" or just blank;
rem will affect some messages.
:setup_programd

  rem Set lib directory (jars)
  call %0 setup_lib_dir
  
  set SERVLET_LIB=%LIBS%\servlet.jar
  if exist %SERVLET_LIB% goto check_js_lib

  echo.
  echo I can't find the servlet.jar that ships with Program D.
  echo Please see http://aitools.org/downloads/.
  set quit=yes
  goto end

  :check_js_lib
  set JS_LIB=%LIBS%\js.jar
  if exist %JS_LIB% goto check_http_server_lib

  echo.
  echo I can't find the js.jar that ships with Program D.
  if /i "%2%"=="building" (echo You must exclude RhinoInterpreter.java in order to successfully build.&echo.) else (echo Your server-side javascript functions may not work.&echo.)

  :check_http_server_lib
  set HTTP_SERVER_LIB=%LIBS%\org.mortbay.jetty.jar
  if exist %HTTP_SERVER_LIB% goto end

  echo.
  echo I can't find the org.mortbay.jetty.jar that ships with Program D.
  if /i "%2%"=="building" (echo You must exclude JettyWrapper.java in order to successfully build.&echo.) else (echo You may not be able to use the Jetty http server.&echo.)
goto end


rem Sets up a Java 2 v1.4 - compatible execution environment
rem (or fails informatively).
:setup_java
  set quit=
  if "%quit%"=="" call %0 set_java_vars
  if "%quit%"=="" call %0 check_java_home
  if "%quit%"=="" call %0 set_jvm_command
goto end


rem Tries to find/set JAVA_HOME.
:set_java_vars
  rem Try to find JAVA_HOME if it isn't already set.
  if defined JAVA_HOME goto end

  echo JAVA_HOME is not set in your environment.

  rem Try the standard J2SDK install location.
  if not exist c:\j2sdk1.4.0\bin\java.exe goto seek_known_javas

  set JAVA_HOME=c:\j2sdk1.4.0\
  set JVM_COMMAND=%JAVA_HOME%\bin\java.exe
  goto successful

  :seek_known_javas
  rem Common paths for compatible Java SDKs (or JREs) should go here.
  if exist d:\j2sdk1.4.0\bin\java.exe set JAVA_HOME=d:\j2sdk1.4.0
  if exist c:\j2sdk1.4.0_01\bin\java.exe set JAVA_HOME=c:\j2sdk1.4.0_01
  if exist d:\j2sdk1.4.0_01\bin\java.exe set JAVA_HOME=d:\j2sdk1.4.0_01
  if not defined JAVA_HOME goto cannot_find

  :successful
  echo I have temporarily set JAVA_HOME to "%JAVA_HOME%".
  echo Please consider setting your JAVA_HOME variable globally.
  goto end

  :cannot_find
  echo I cannot find a java executable in your path.
  echo Please check that you hava a Java 2 v1.4 compatible SDK installed.
  set quit=yes
goto end


rem Checks that JAVA_HOME points to a real directory.
:check_java_home
  if exist "%JAVA_HOME%" goto end
  
  echo I can't find your JAVA_HOME directory.
  echo ("%JAVA_HOME%" doesn't seem to exist.)
  echo Please be sure that a Java 2 v1.4 compatible SDK is installed
  echo and (even better) set the JAVA_HOME environment variable to point to
  echo the directory where it is installed.
  echo.
  echo (Note: If you are not going to build Program D, but
  echo only run it, you can install the Java 2 JRE instead of
  echo the whole SDK.)
  echo.
  set quit=yes
goto end

rem Sets the JVM launcher command.
:set_jvm_command
  set JVM_COMMAND=%JAVA_HOME%\bin\java.exe
goto end

:end
