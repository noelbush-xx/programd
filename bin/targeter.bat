@echo off
rem ==========================================================================
rem Alicebot Program D
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

rem This script launches the Program D targeting utility.

rem Reset the quit variable.
set quit=

rem Enter the bin directory.
pushd %~p0

rem Check for needed environment space.
call common_functions.bat check_env %1 %2 %3 %4

rem Get "base" directory (root of Program D installation)
if "%quit%"=="" call common_functions.bat set_base

rem Set up the Java environment.
if "%quit%"=="" call common_functions.bat setup_java

rem Check for the alice lib.
if "%quit%"=="" call common_functions.bat check_alice_lib

rem Launch the tool.
if "%quit%"=="" %JVM_COMMAND% -classpath %ALICE_LIB% -Xms64m -Xmx64m org.alicebot.targeting.TargetingTool %1

rem On exit, go back to the original directory.
popd
