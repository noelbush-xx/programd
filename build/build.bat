@echo off
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

rem @set JAVA_HOME=c:\jdk1.3
rem edit the above line to point to your java JDK directory, and remove the "rem"

@set JAVA_TOOLS=%JAVA_HOME%\lib\tools.jar;
@set LOCALCLASSPATH=%JAVA_TOOLS%;../lib/activation.jar;../lib/jxta.jar;../lib/jxtashell.jar;../lib/aliceserver.jar;../lib/js.jar;../lib/xml.jar;../lib/servlet.jar;../lib/ant.jar;../lib/sql.jar;
@set ANT_HOME=./
@echo Building with classpath %LOCALCLASSPATH%
@echo Starting Ant...
@%JAVA_HOME%\bin\java.exe -Dant.home="%ANT_HOME%" -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
