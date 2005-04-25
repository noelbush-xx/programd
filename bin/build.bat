@echo off
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

rem This script launches the build process for Program D.

rem Reset the quit variable.
set quit=

rem Enter the bin directory.
pushd %~p0

rem Check for needed environment space.
call common_functions.bat check_env %1 %2 %3 %4

rem Get "base" directory (root of Program D installation)
if "%quit%"=="" call common_functions.bat set_base

rem Set up the third party libraries.
if "%quit%"=="" call common_functions.bat setup_other_libs

rem Set up the Java environment.
if "%quit%"=="" call common_functions.bat setup_java

if not "%quit%"=="" goto end

rem Set up other paths to needed jars and check their existence.
set ANT_MAIN_LIB=%LIBS%\ant.jar
set ANT_LAUNCHER_LIB=%LIBS%\ant-launcher.jar
if exist %ANT_MAIN_LIB% goto check_ant_launcher_lib

echo.
echo I can't find ant.jar
echo This is necessary for the build process.
goto end

:check_ant_launcher_lib
if exist %ANT_LAUNCHER_LIB% goto set_ant_path

echo.
echo I can't find ant-launcher.jar
echo This is necessary for the build process.
goto end

:set_ant_path
set ANT_LIBS=%ANT_MAIN_LIB%;%ANT_LAUNCHER_LIB%

:check_java_tools
set JAVA_TOOLS=%JAVA_HOME%\lib\tools.jar
if exist %JAVA_TOOLS% goto concat_class_path

echo.
echo I can't find the tools.jar.
echo This is necessary for the build process.
goto end

:concat_class_path
rem Concatenate all paths into the classpath to be used.
set BUILD_CLASSPATH=%JAVA_TOOLS%;%ANT_LIBS%;%OTHER_LIBS%

%JVM_COMMAND% -Dant.home=%BASE% -classpath %BUILD_CLASSPATH% org.apache.tools.ant.Main -buildfile %BASE%\conf\build.xml %1 %2 %3 %4

:end
rem On exit, go back to the original directory.
popd