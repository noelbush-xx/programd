#!/bin/sh
# ==========================================================================
# Alice Program D
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

JAVA_TOOLS=$JAVA_HOME/lib/tools.jar
XML_LIB=../lib/xml.jar
SERVLET_LIB=../lib/servlet.jar
ALICE_LIB=../lib/aliceserver.jar
JS_LIB=../lib/js.jar
ANT_LIB=../lib/ant.jar
HTTP_SERVER_LIBS=../lib/org.mortbay.jetty.jar:../lib/com.sun.net.ssl.jar:../lib/javax.servlet.jar:../lib/javax.xml.jaxp.jar:../lib/org.apache.crimson.jar

BUILD_CLASSPATH=$JAVA_TOOLS:$ANT_LIB:$XML_LIB:$SQL_LIB:$SERVLET_LIB:$ALICE_LIB:$JS_LIB:$HTTP_SERVER_LIBS

ANT_HOME=./

echo Starting Ant...

java -Dant.home=$ANT_HOME -classpath $BUILD_CLASSPATH org.apache.tools.ant.Main $1 $2 $3 $4
