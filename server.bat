@echo off
@rem This is a shell script for starting Alicebot.Net Server. Currently,this
@rem script must be run from the server directory containing the
@rem SERVER.properties file.  See http://www.alicebot.net for more information.
@rem ==========================================================================
@rem Alice Program D
@rem Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
@rem 
@rem This program is free software; you can redistribute it and/or
@rem modify it under the terms of the GNU General Public License
@rem as published by the Free Software Foundation; either version 2
@rem of the License, or (at your option) any later version.
@rem
@rem You should have received a copy of the GNU General Public License
@rem along with this program; if not, write to the Free Software
@rem Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
@rem USA.
@rem
@rem @author  Richard Wallace
@rem @author  Jon Baer
@rem @author  Thomas Ringate/Pedro Colla
@rem @version 4.1.2
@rem ==========================================================================

@echo Starting Alicebot 4.1.2 Server.
@set ALICE_HOME=.
@set XML_LIB=lib/xml.jar
@set SQL_LIB=lib/sql.jar
@set SERVLET_LIB=lib/servlet.jar
@set ALICE_LIB=lib/aliceserver.jar
@set JS_LIB=lib/js.jar
@set JXTA_LIB=lib/jxta.jar
@set JXTASHELL_LIB=lib/jxtashell.jar
@set ACTIVATION_LIB=lib/activation.jar

@set CLASSPATH=%ALICE_HOME%;%XML_LIB%;%SQL_LIB%;%SERVLET_LIB%;%ALICE_LIB%;%JS_LIB%;%JXTA_LIB%;%JXTASHELL_LIB%;%ACTIVATION_LIB%;
@java.exe -classpath %CLASSPATH% -Xms64m -Xmx64m -Djava.library.path=./bin org.alicebot.server.net.AliceServer
