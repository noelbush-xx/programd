#!/bin/sh
# This is a shell script for starting the Targeting GUI. Currently,this
# script must be run from the server directory containing the
# server.properties file.  See http://alicebot.org for more information.
#
# ==========================================================================
# Alicebot Program D
# Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
# USA.
# ==========================================================================
#
echo Starting Alicebot Program D Targeting Utility.
ALICE_HOME=.
ALICE_LIB=lib/aliceserver.jar

java -classpath $ALICE_LIB -Xms64m -Xmx64m org.alicebot.server.core.targeting.TargetingTool $1
