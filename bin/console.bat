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

rem This script launches a server-based version of
rem Program D, with a very simple Swing GUI
rem that supports most character sets and automates a
rem few interaction tasks.

rem Reset the quit variable.
set quit=

rem Enter the bin directory.
pushd %~p0

rem Check for needed environment space.
call common_functions.bat check_env %1 %2 %3 %4

rem Get "base" directory (root of Program D installation)
if "%quit%"=="" call common_functions.bat set_base

rem Start Program D using the SimpleConsole main class.
rem You can change the second argument to set the maximum memory
rem (in MB) allocated to the JVM.
if "%quit%"=="" call common_functions.bat start_programd org.aitools.programd.gui.SimpleConsole 64 %1