@echo off
rem This batch file checks your environment settings to try to avoid errors
rem when starting Program D.
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

if not exist %comspec% goto nocomspec

:comspec
rem %comspec% points to an existing command interpreter
%comspec% /E:4096 /C server.bat %1
goto end

:nocomspec
rem %comspec% is not set, trying command.com
command /E:4096 /C server.bat %1

goto end

:end
