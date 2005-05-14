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
  
  # Concatenate all paths into the classpath to be used.
  PROGRAMD_CLASSPATH=$PROGRAMD_LIBS:$JS_LIB:$SQL_LIB:$HTTP_SERVER_LIBS

  # Change to the Program D directory and start the main class.
  cd $BASE
  if [ -z "$6" ]
  then
    $JVM_COMMAND -classpath $PROGRAMD_CLASSPATH -Xms$2 -Xmx$3 $1 -c $4 -n $5
  else
    $JVM_COMMAND -classpath $PROGRAMD_CLASSPATH -Xms$2 -Xmx$3 $1 -c $4 -n $5 -w $6
  fi
}

# Sets up some variables used to run Program D.
function setup_programd()
{
  # Set lib directory (jars)
  setup_lib_dir
  
  PROGRAMD_MAIN_LIB=$LIBS/programd-main.jar
  if [ ! -r $PROGRAMD_MAIN_LIB ]
  then
    echo I can\'t find your programd-main.jar file.  Have you compiled it?
    echo If you downloaded the source-only version but don\'t have
    echo a Java compiler, you can download a pre-compiled version from
    echo http://aitools.org/downloads/
    echo
    exit 1
  fi
  
  # Define the other programd jars, but don't worry if they don't exist.
  PROGRAMD_JETTY_LIB=$LIBS/programd-jetty.jar
  PROGRAMD_JS_LIB=$LIBS/programd-rhino.jar
  
  # Set up external jars.
  setup_other_libs

  PROGRAMD_LIBS=$PROGRAMD_MAIN_LIB:$PROGRAMD_JETTY_LIB:$PROGRAMD_JS_LIB:$OTHER_LIBS
}

# Sets up other required included libs.
function setup_other_libs()
{
  # Set lib directory (jars)
  setup_lib_dir
  
  GETOPT_LIB=$LIBS/gnu.getopt-1.0.10.jar
  if [ ! -r $GETOPT_LIB ]
  then
    echo
    echo I can\'t find the gnu.getopt-1.0.10.jar that ships with Program D.
    exit 1
  fi

  # Optional components:

  # Set LISTENER_LIBS to the location of your listener jars.
  # No warning is provided if they cannot be found (since they are optional).
  ICQ_AIM_LISTENER_LIBS=$LIBS/icq-aim-listener/icq-aim-listener.jar:$LIBS/icq-aim-listener/daim.jar:$LIBS/icq-aim-listener/log4j-1.2.9.jar
  IRC_LISTENER_LIBS=$LIBS/irc-listener/irc-listener.jar
  YAHOO_LISTENER_LIBS=$LIBS/yahoo-listener/yahoo-listener.jar:$LIBS/yahoo-listener/ymsg_network_v0_6.jar
  LISTENER_LIBS=$ICQ_AIM_LISTENER_LIBS:$IRC_LISTENER_LIBS:$YAHOO_LISTENER_LIBS

  # Set SQL_LIB to the location of your database driver.
  # No warning is provided if it cannot be found (since it is optional).
  SQL_LIB=$LIBS/mysql_comp.jar

  # Set JS_LIB to the location of the Rhino JavaScript interpreter.
  # No warning is provided if it cannot be found (since it is optional).
  JS_LIB=$LIBS/js.jar

  # Set JETTY_LIBS to the location of the Jetty jars.
  # No warning is provided if they cannot be found (since they are optional).
  JETTY_LIBS=$LIBS/commons-logging.jar:$LIBS/org.mortbay.jetty.jar:$LIBS/javax.servlet.jar
  
  OTHER_LIBS=$GETOPT_LIB:$LISTENER_LIBS:$SQL_LIB:$JS_LIB:$JETTY_LIBS
}

# Sets up the lib directory.
function setup_lib_dir()
{
  LIBS=$BASE/lib
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
    if [ -x /usr/java/jdk1.5.0_02/bin/java ]
    then
      export JAVA_HOME=/usr/java/jdk1.5.0_02
      JVM_COMMAND=$JAVA_HOME/bin/java
      echo I have set JAVA_HOME to \"/usr/java/jdk1.5.0_02\".
    else 
      # See if any java command exists.
      JVM_COMMAND=`which java 2>&1 | sed -e 's/.*which: no java in.*//'`
      if [ -z "$JVM_COMMAND" ]
      then
        echo I cannot find a java executable in your path.
        echo Please check that you have a JDK 5.0 compatible SDK installed.
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

