@echo off
rem This is a batch file for starting Alicebot Program D. Currently,this
rem script must be run from the server directory containing the
rem server.properties file.  See http://alicebot.org for more information.
rem
rem You may need to edit the JAVA_HOME setting below if you do not already
rem have this environment variable defined.
rem
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

echo Starting Alicebot Program D Targeting Utility.
set ALICE_HOME=.
set ALICE_LIB=lib/aliceserver.jar

java.exe -classpath %ALICE_LIB% -Xms64m -Xmx64m org.alicebot.server.core.targeting.TargetingTool %1
