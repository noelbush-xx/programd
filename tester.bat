@echo off
rem This batch file feeds the testcase.txt file to the server, after erasing
rem the chat log.
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

erase .\logs\chat.log
erase .\logs\chat.xml
server < testcase.txt