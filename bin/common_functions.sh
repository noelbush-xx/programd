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

# Starts Program D using several parameters.
# Parameters:
# $1: jar file (with a main class specified internally by manifest)
# $2: starting memory allocation
# $3: maximum memory allocation
# $4: configuration file
function start_programd()
{
  # Set up Program D variables
  setup_programd
  
  # Set up the Java environment.
  setup_java

  # Change to the Program D directory and launch the given jar file.
  cd $BASE
  $JVM_COMMAND -Xms$2 -Xmx$3 -jar $1 -c $4
}

# Sets up some variables used to run Program D.
function setup_programd()
{
  LIBS=$BASE/lib
  DISTRIB=$BASE/distrib
  WEBLIBS=$BASE/WebContent/WEB-INF/lib
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
    if [ -x /usr/java/jdk1.5.0_06/bin/java ]
    then
      export JAVA_HOME=/usr/java/jdk1.5.0_06
      JVM_COMMAND=$JAVA_HOME/bin/java
      echo I have set JAVA_HOME to \"/usr/java/jdk1.5.0_06\".
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
    echo Please be sure that a JDK 5.0 compatible JRE is installed
    echo and \(even better\) set the \$JAVA_HOME environment variable to point to
    echo the directory where it is installed.
    echo
    exit 1
  fi
}

# Sets the JVM launcher command.
function set_jvm_command()
{
  JVM_COMMAND="$JAVA_HOME/bin/java -Dlog4j.configuration=file:$BASE/conf/log4j.xml"
}

# Checks the version of the JVM command.
function check_jvm_version()
{
  JVM_VERSION=`$JVM_COMMAND -version 2>&1 | grep version | cut -f 3 -d " " | sed -e 's/\"//g'`
  case "$JVM_VERSION" in 1.5.*|1.6.*)
    # Version is okay; no need to say anything.
    ;; (*)
    echo Your JVM is apparently version $JVM_VERSION.
    echo This may not be compatible with our needs.
    echo Please install a JDK 5.0+ compatible JVM.
    echo
    exit 1
  esac
}

