# ==========================================================================
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

# This contains functions used by shell scripts.
# This file itself is not executable.

# Starts Program D using a given main class.
function start_programd()
{
  # Set up the Program D variables.
  setup_programd

  # Set up the Java environment.
  setup_java

  # Set up other paths to other needed jars and check their existence.
  check_programd_lib

  # Set SQL_LIB to the location of your database driver.
  # No warning is provided if it cannot be found.
  SQL_LIB=$LIBS/mysql_comp.jar

  # Concatenate all paths into the classpath to be used.
  PROGRAMD_CLASSPATH=$SERVLET_LIB:$PROGRAMD_LIB:$JS_LIB:$SQL_LIB:$HTTP_SERVER_LIB

  # Change to the Program D bin directory and start the main class.
  cd $BASE/bin
  $JVM_COMMAND -classpath $PROGRAMD_CLASSPATH -Xms256m -Xmx512m $1 $3
}

# Checks that the programd.jar exists.
function check_programd_lib()
{
  # Set lib directory (jars)
  setup_lib_dir
  
  PROGRAMD_LIB=$LIBS/programd.jar
  if [ ! -r $PROGRAMD_LIB ]
  then
    echo I can\'t find your programd.jar file.  Have you compiled it?
    echo If you downloaded the source-only version but don\'t have
    echo a Java compiler, you can download a pre-compiled version from
    echo http://aitools.org/downloads/
    echo
    exit 1
  fi
}

# Sets up the lib directory.
function setup_lib_dir()
{
  LIBS=$BASE/lib
}

# Sets up some variables used to run/build Program D.
# First argument should be "building" or just blank;
# will affect some messages.
function setup_programd()
{
  # Set lib directory (jars)
  setup_lib_dir
  
  SERVLET_LIB=$LIBS/servlet.jar
  if [ ! -r $SERVLET_LIB ]
  then
    echo
    echo I can\'t find the servlet.jar that ships with Program D.
    echo Please see http://aitools.org/downloads/.
    echo
    exit 1
  fi

  JS_LIB=$LIBS/js.jar
  if [ ! -r $JS_LIB ]
  then
    echo
    echo I can\'t find the js.jar that ships with Program D.
    if [ $1 == "building" ]
    then
      echo You must exclude RhinoInterpreter.java in order to successfully build.
    else
      echo Your server-side javascript functions may not work.
    fi
  fi

  HTTP_SERVER_LIB=$LIBS/org.mortbay.jetty.jar
  if [ ! -r $HTTP_SERVER_LIB ]
  then
    echo
    echo I can\'t find the org.mortbay.jetty.jar that ships with Program D.
    if [ $1 == "building" ]
    then
      echo You must exclude JettyWrapper.java in order to successfully build.
    else
      echo You may not be able to use the Jetty http server.
    fi
  fi
}

# Sets up a Java execution environment
# (or fails informatively).
function setup_java()
{
  set_java_vars
  check_java_home
  set_jvm_command
  check_jvm_version
}


# Tries to find/set JAVA_HOME.
function set_java_vars()
{
  # Try to find JAVA_HOME if it isn't already set.
  if [ -z "$JAVA_HOME" ]
  then
    echo
    echo JAVA_HOME is not set in your environment.
  
    # Try the standard JDK 5.0 install location.
    if [ -x /usr/java/jdk1.5.0/bin/java ]
    then
      export JAVA_HOME=/usr/java/jdk1.5.0
      JVM_COMMAND=$JAVA_HOME/bin/java
      echo I have set JAVA_HOME to \"/usr/java/jdk1.5.0\".
    else 
      # See if any java command exists.
      JVM_COMMAND=`which java 2>&1 | sed -e 's/.*which: no java in.*//'`
      if [ -z "$JVM_COMMAND" ]
      then
        echo I cannot find a java executable in your path.
        echo Please check that you hava a JDK 5.0 compatible SDK installed.
        echo
        exit 1
      else
        # This may be usable but we don't know yet.
        JAVA_BIN=`dirname $JVM_COMMAND`
        echo I found a java executable in \"$JAVA_BIN\".
        export JAVA_HOME=`echo $JAVA_BIN | sed -e 's/\/bin$//'`
        echo I have set JAVA_HOME to \"$JAVA_HOME\".
      fi
    fi
    echo Please consider setting your JAVA_HOME environment variable.
  fi
  echo
}

# Checks that JAVA_HOME points to a real directory.
function check_java_home()
{
  if [ ! -d $JAVA_HOME ]
  then
    echo I can\'t find your JAVA_HOME directory.
    echo \(\"$JAVA_HOME\" doesn\'t seem to exist.\)
    echo Please be sure that a JDK 5.0 compatible SDK is installed
    echo and \(even better\) set the \$JAVA_HOME environment variable to point to
    echo the directory where it is installed.
    echo
    echo \(Note: If you are not going to build Program D, but
    echo only run it, you can install the JRE instead of
    echo the whole JDK.\)
    echo
    exit 1
  fi
}

# Sets the JVM launcher command.
function set_jvm_command()
{
  JVM_COMMAND=$JAVA_HOME/bin/java
}

# Checks the version of the JVM command.
function check_jvm_version()
{
  JVM_VERSION=`$JVM_COMMAND -version 2>&1 | grep version | cut -f 3 -d " " | sed -e 's/\"//g'`
  case "$JVM_VERSION" in (1.5.*)
    # Version is okay; no need to say anything.
    ;; (*)
    echo Your JVM is apparently version $JVM_VERSION.
    echo This may not be compatible with our needs.
    echo Please install a JDK 5.0 compatible JVM.
    echo
    exit 1
  esac
}

