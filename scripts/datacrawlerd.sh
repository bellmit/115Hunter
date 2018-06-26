#!/bin/sh
#
# A SUSE Linux start/stop script for Data Crawler daemon.
#


### BEGIN INIT INFO
# Provides:                   dataCrawlerd
# Required-Start:             $network $local_fs $remote_fs
# X-UnitedLinux-Should-Start: $named sendmail
# Required-Stop:              $network $local_fs $remote_fs
# X-UnitedLinux-Should-Stop:  $named sendmail
# Default-Start:              3 5
# Default-Stop:               0 1 2 6
# Short-Description:          dataCrawlerd
# Description:                data crawler Java daemon service.
### END INIT INFO

# Source LSB init functions 
# providing start_daemon, killproc, pidofproc, 
# log_success_msg, log_failure_msg and log_warning_msg. 
# This is currently not used by UnitedLinux based distributions and 
# not needed for init scripts for UnitedLinux only. If it is used, 
# the functions from rc.status should not be sourced or used. 
. /lib/lsb/init-functions 

# Shell functions sourced from /etc/rc.status: 
#      rc_check         check and set local and overall rc status 
#      rc_status        check and set local and overall rc status 
#      rc_status -v     be verbose in local rc status and clear it afterwards 
#      rc_status -v -r  ditto and clear both the local and overall rc status 
#      rc_status -s     display "skipped" and exit with status 3 
#      rc_status -u     display "unused" and exit with status 3 
#      rc_failed        set local and overall rc status to failed 
#      rc_failed <num>  set local and overall rc status to <num> 
#      rc_reset         clear both the local and overall rc status 
#      rc_exit          exit appropriate to overall rc status 
#      rc_active        checks whether a service is activated by symlinks 
. /etc/rc.status                                           # load start/stop script "rc" definitions

# check root user
function checkUser()
{
  if [ `/usr/bin/whoami` != root ]; then
    logError "Error: Must run with root privilege."
    exit 1
  fi
}  

# set JAVA_HOME
function setJavaHome()
{
  # Set JAVA_HOME to working JDK or JRE
  # If not set we'll try to guess the JAVA_HOME
  # from java binary if on the PATH
  #
  # JAVA_HOME maybe set improperly, unset it first
  unset JAVA_HOME
  if [ -z "$JAVA_HOME" ]; then
    JAVA_BIN="`which java 2>/dev/null || type java 2>&1`"
    test -x "$JAVA_BIN" && JAVA_HOME="`dirname $JAVA_BIN`"
    test ".$JAVA_HOME" != . && JAVA_HOME=`cd "$JAVA_HOME/.." >/dev/null; pwd`
  else
    JAVA_BIN="$JAVA_HOME/bin/java"
  fi

  if [ -z "$JAVA_HOME" ]; then 
    logError "Unable to set JAVA_HOME environment variable"; exit 1; 
  else
    logInfo "Set JAVA_HOME : $JAVA_HOME"
  fi
}

# set CLASS_PATH
function setClassPath()
{ 
  if [ -z "$CLASSPATH" ]; then
     CLASSPATH="."
  else
     CLASSPATH=$CLASSPATH
  fi
  # make sure to append gearman jar files in the end, so that slf4j binding is correct with log4j 2.0 and not be disturbed by logBack
  for i in `ls $applDir/lib/*.jar|grep -v gearman`
  do
     CLASSPATH=${CLASSPATH}:${i}
  done
  for i in `ls $applDir/lib/*.jar|grep gearman`
  do
     CLASSPATH=${CLASSPATH}:${i}
  done
  CLASSPATH=${CLASSPATH}:$applDir/$jarFile
}

# Init runtime
function init()
{
  scriptFile=$(readlink -fn $(type -p $0))                   # the absolute, dereferenced path of this script file
  scriptDir=$(dirname $scriptFile)                           # absolute path of the script directory
  applDir="$scriptDir"                                       # home directory of the service application
  serviceName="DataCrawler"                                  # service name
  serviceNameLo="datacrawler"                                # service name with the first letter in lowercase
  LOG_TIME=`date '+%Y.%m.%d %H:%M:%S'`
  LOG_FILE="/var/log/$serviceNameLo.log"
  makeFileWritable $LOG_FILE || exit 1

  if [ -z "$DATA_CRAWLER_APP_HOME" ]; then
    DATA_CRAWLER_APP_HOME="$applDir"
  fi
  cd $DATA_CRAWLER_APP_HOME
  
  jarFile=`ls  $applDir/lib | grep "com.sap.cisp.xhna.data.*\.jar$" | awk '{if (NR==1){printf"%s",$0}}'`
  if [ -z "$jarFile" ]; then
     jarFile=`ls  $applDir | grep "com.sap.cisp.xhna.data.*\.jar$" | awk '{if (NR==1){printf"%s",$0}}'`
  fi
     
  if [ ! -f $applDir/$jarFile ]; then
     if [ -f $applDir/lib/$jarFile ]; then
        /bin/mv $applDir/lib/$jarFile $applDir
     else
        logError "Missing data crawler jar file. Exiting..."
        exit 1
     fi
  fi

  setJavaHome
  
  setClassPath
  
  maxShutdownTime=15                                         # maximum number of seconds to wait for the daemon to terminate normally
  pidFile="/var/run/$serviceNameLo.pid"                      # name of PID file (PID = process ID number)
  tmpPidFile="/var/run/tmpworker.pid"                        # name of worker tmp pid file
  javaCommand="java"                                         # name of the Java launcher without the path
  javaExe="$JAVA_HOME/bin/$javaCommand"                      # file name of the Java application launcher executable
  javaArgs="-cp $CLASSPATH com.sap.cisp.xhna.data.Main"                   # arguments for Java launcher
  javaCommandLine="$javaExe $javaArgs"                       # command line to start the Java service application
  javaCommandLineKeyword="com.sap.cisp.xhna.data"            # a keyword that occurs on the commandline, used to detect an already running service process and to distinguish it from others
  rcFileBaseName="rc$serviceNameLo"                          # basename of the "rc" symlink file for this script
  rcFileName="/usr/local/sbin/$rcFileBaseName"               # full path of the "rc" symlink file for this script
  etcInitDFile="/etc/init.d/$serviceNameLo"                  # symlink to this script from /etc/init.d
  runningFlag="/var/run/start.txt"                               # data crawler process running flag file for graceful shutdown

  # export proxy setting
  if [ -f $scriptDir/setenv.sh ]; then chmod +x $scriptDir/setenv.sh; fi
  . $scriptDir/setenv.sh
}  

# ------------------------------------------------------------------------------
# write log on console and log file
# Format : LOG_TYPE\t LOG_TIME   	SHELL_NAME    LOG_CONTENT
function log()
{
  LOG_TYPE=$1
  LOG_CONTENT=$2
  echo "${LOG_TYPE}	${LOG_TIME}  	${SHNAME}    ${LOG_CONTENT}"|tee -a ${LOG_FILE}
}

# ------------------------------------------------------------------------------
# write log for info information
function logInfo()
{
  LOG_CONTENT=$1
  log "INFO" "${LOG_CONTENT}"
}

# ------------------------------------------------------------------------------
# write log for error information
function logError()
{
  LOG_CONTENT=$1
  log "ERROR" "${LOG_CONTENT}"
}

# Makes the file $1 writable by the group $serviceGroup.
function makeFileWritable {
   local filename="$1"
   touch $filename || return 1
   chgrp root $filename || return 1
   chmod g+w $filename || return 1
   return 0; }

# Returns 0 if the process with PID $1 is running.
function checkProcessIsRunning {
   local pid="$1"
   if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
   if [ ! -e /proc/$pid ]; then 
      logInfo "The data crawler process is NOT running."
      return 1
   fi
   logInfo "The data crawler process is running with pid $pid."
   return 0; }

# Returns 0 if the process with PID $1 is our Java service process.
function checkProcessIsOurService {
   local pid="$1"
   local cmd="$(ps -p $pid --no-headers -o comm)"
   if [ "$cmd" != "$javaCommand" -a "$cmd" != "$javaCommand.bin" ]; then return 1; fi
   grep -q --binary -F "$javaCommandLineKeyword" /proc/$pid/cmdline
   if [ $? -ne 0 ]; then 
      logInfo "The process is NOT datacrawlerd service, missing keyword $javaCommandLineKeyword"
      return 1
   fi
   logInfo "The process is datacrawlerd service."
   return 0; }

# Returns 0 when the service is running and sets the variable $servicePid to the PID.
function getServicePid {
   if [ ! -f $pidFile ]; then return 1; fi
   servicePid="$(<$pidFile)"
   checkProcessIsRunning $servicePid || return 1
   checkProcessIsOurService $servicePid || return 1
   return 0; }
   
# Returns 0 when the worker is running and sets the variable $workerPid to the PID.
function getWorkerPid {
   workerPid=$1
   checkProcessIsRunning $1 || return 1
   checkProcessIsOurService $1 || return 1
   return 0; }

function startServiceProcess {
   cd $applDir || return 1
   rm -f $pidFile
   makeFileWritable $pidFile || return 1
   
   local cmd="setsid $javaCommandLine >/dev/null 2>&1 & echo \$! >$pidFile"
   echo "$cmd" > $scriptDir/startup.sh 2>&1
   logInfo "$cmd"
   sudo  $SHELL -c "$cmd" || return 1
   sleep 0.1
   if [ -f $pidFile ]; then
       chmod 777 $pidFile
   fi
   servicePid="$(<$pidFile)"
   if checkProcessIsRunning $servicePid; then :; else
      logError "$serviceName start failed, see logfile."
      return 1
   fi
   touch $runningFlag
   return 0; }
   
function startWorker {
   javaWorkerArgs="-cp $CLASSPATH com.sap.cisp.xhna.data.task.worker.main.TaskWorkerMain" 
   javaCommandLine="$javaExe $javaWorkerArgs"  
   local cmd="setsid $javaCommandLine >/dev/null 2>&1 & echo \$! >>$tmpPidFile"
   echo "$cmd" > $scriptDir/startworker.sh 2>&1
   logInfo "$cmd"
   sudo  $SHELL -c "$cmd" || return 1
   sleep 0.1
   if [ -f $tmpPidFile ]; then
       chmod 777 $tmpPidFile
   fi
   workerPid=`tail -1 $tmpPidFile`
   if checkProcessIsRunning $workerPid; then :; else
      logError "Data crawling worker start failed, see logfile."
      return 1
   fi
   return 0; }

function stopWorker {
   if [ ! -f $tmpPidFile ]; then return 1; fi
   local result=0
   for i in `cat $tmpPidFile`
   do
     stopOneWorker ${i}
     let result=$result+$?
   done
   return $result; }

function stopOneWorker {
   getWorkerPid $1
   if [ $? -ne 0 ]; then logInfo "Data crawling worker is not running"; rc_failed 0; rc_status -v; return 0; fi
   logInfo "Stopping Data crawling worker ...   "
   kill $workerPid || return 1

   for ((i=0; i<maxShutdownTime*10; i++)); do
      checkProcessIsRunning $workerPid >/dev/null 2>&1
      if [ $? -ne 0 ]; then
         logInfo "Stop Data crawling worker successfully."
         rm -f $tmpPidFile
         return 0
         fi
      sleep 0.1
      done
   
   logInfo "Data crawling worker did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
   
   kill -s KILL $workerPid || return 1
   local killWaitTime=60
   for ((i=0; i<killWaitTime*10; i++)); do
      checkProcessIsRunning $workerPid
      if [ $? -ne 0 ]; then
         rm -f $tmpPidFile
         return 0
         fi
      sleep 0.1
      done
   logError "Error: Data crawling worker could not be stopped within $maxShutdownTime+$killWaitTime seconds!"
   return 1; }


function stopServiceProcess {
  # Try to terminate the data crawler gracefully first.
   if [ -f $runningFlag ]; then 
      logInfo "Remove the running flag start.txt for graceful shutdown. It will take a while..."
      /bin/rm -f $runningFlag
   else
      kill $servicePid || return 1
   fi
   for ((i=0; i<maxShutdownTime*10; i++)); do
      checkProcessIsRunning $servicePid >/dev/null 2>&1
      if [ $? -ne 0 ]; then
         logInfo "Stop $serviceName successfully."
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   
   logInfo "$serviceName did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
   
   kill -s KILL $servicePid || return 1
   local killWaitTime=60
   for ((i=0; i<killWaitTime*10; i++)); do
      checkProcessIsRunning $servicePid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   logError "Error: $serviceName could not be stopped within $maxShutdownTime+$killWaitTime seconds!"
   return 1; }

function runInConsoleMode {
   getServicePid
   if [ $? -eq 0 ]; then logInfo "$serviceName is already running"; return 1; fi
   cd $applDir || return 1
   sudo  $javaCommandLine || return 1
   if [ $? -eq 0 ]; then return 1; fi
   return 0; }

function startService {
   getServicePid
   if [ $? -eq 0 ]; then logInfo "$serviceName is already running"; rc_failed 0; rc_status -v; return 0; fi
   logInfo "Starting $serviceName   "
   startServiceProcess
   if [ $? -ne 0 ]; then rc_failed 1; rc_status -v; return 1; fi
   rc_failed 0
   rc_status -v
   return 0; }

function stopService {
   getServicePid
   if [ $? -ne 0 ]; then logInfo "$serviceName is not running"; rc_failed 0; rc_status -v; return 0; fi
   logInfo "Stopping $serviceName   "
   stopServiceProcess
   if [ $? -ne 0 ]; then rc_failed 1; rc_status -v; return 1; fi
   rc_failed 0
   rc_status -v
   return 0; }

function checkServiceStatus {
   logInfo "Checking for $serviceName..."
   if getServicePid; then
      rc_failed 0
    else
      rc_failed 3
      fi
   rc_status -v
   return 0; }

function installService {
   if [ -f $rcFileName ] || [ -f $etcInitDFile ]; then
      logInfo "There are symbolic links already existed. Uninstall it first..."
      uninstallService
   fi
   ln -s $scriptFile $rcFileName || return 1
   ln -s $scriptFile $etcInitDFile || return 1
   insserv $serviceNameLo || return 1
   logInfo "$serviceName installed".
   logInfo "You may now use $rcFileBaseName to call this script".
   return 0; }

function uninstallService {
   insserv -r $serviceNameLo || return 1
   rm -f $rcFileName
   rm -f $etcInitDFile
   logInfo "$serviceName uninstalled."
   return 0; }

function main {
   rc_reset
   case "$1" in
      console)                                             # runs the Java program in console mode
         runInConsoleMode
         ;;
      startworker)
         logInfo "start Data crawling worker process..."
         startWorker
         ;;
      stopworker)
         logInfo "stop Data crawling worker process..."
         stopWorker
         ;;
      start)                                               # starts the Java program as a Linux service
         $0 status &>/dev/null
         ret=$?
         if [ $ret = 0 ]; then
            logInfo "dataCrawlerd is already running"
            rc_failed $ret
            rc_status -v1
            rc_exit
         fi
         startService
         ;;
      stop)                                                # stops the Java program service
         stopService
         ;;
      restart)                                             # stops and restarts the service
         stopService && startService
         ;;
      status)                                              # displays the service status
         checkServiceStatus
         ;;
      install)                                             # installs the service in the OS
         installService
         ;;
      uninstall)                                           # uninstalls the service in the OS
         uninstallService
         ;;
      *)
         echo "Usage: $0 {console|start|stop|restart|status|install|uninstall}"
         exit 1
         ;;
      esac
   rc_exit; }

#=====================================================================
#                           MAIN
#=====================================================================
checkUser

init

main $1
