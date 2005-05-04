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

:concat_class_path
rem Concatenate all paths into the classpath to be used.
set BUILD_CLASSPATH=%OTHER_LIBS%

ant -buildfile %BASE%\conf\build.xml %1

:end
rem On exit, go back to the original directory.
popd