#!/bin/sh
#
# ASMCtrl.sh
# Control script for running the Introscope EP Agent
# as a Unix service via an easy-to-use command line interface.
# Usage:
# ASMCtrl.sh start
# ASMCtrl.sh status
# ASMCtrl.sh stop
# ASMCtrl.sh help
#
# With specifying memory values:
# ASMCtrl.sh start 64 1024
#
# The exit codes returned are:
#	0 - operation completed successfully
#	1 -
#	2 - usage error
#	3 - EPAgent could not be started
#	4 - EPAgent could not be stopped
#	8 - configuration syntax error
#
# When multiple arguments are given, only the error from the _last_
# argument is reported.
# Run "ASMCtrl.sh help" for usage info

# |||||||||||||||||||| START CONFIGURATION SECTION  ||||||||||||||||||||
# Set the home directory if it is unset.
# Different OSes require different test statements
ERROR=0

THIS_OS=`uname -a | awk '{print $1}'`
case $THIS_OS in
HP-UX)
    if ! [ "$WILYHOME" ] ; then
        WILYHOME="`pwd`/.."; export WILYHOME
    fi
    ;;
*)
    if [ -z "$WILYHOME" ]; then
        WILYHOME="`pwd`/.."; export WILYHOME
    fi
    ;;
esac

if [ ! -d "${WILYHOME}/logs" ]; then
    mkdir -p "${WILYHOME}/logs";
fi;

# The logfile
LOGFILE="${WILYHOME}/logs/asm.log"
# the path to your PID file
PIDFILE="${WILYHOME}/logs/asm.pid"


# changes for passing heap values in arguments
MIN_HEAP_VAL_IN_MB=16
MAX_HEAP_VAL_IN_MB=256

MIN_ARG_PRESENT=true
if [ -z "$2" ]
  then
    MIN_ARG_PRESENT=false
fi

MAX_ARG_PRESENT=true
if [ -z "$3" ]
  then
    MAX_ARG_PRESENT=false
fi

if [ "$MIN_ARG_PRESENT" == "true" ]
   then
   	#checking whether the input is a number
   	echo $2 | grep "[^0-9]" > /dev/null 2>&1
   	if [ "$?" -eq "0" ]; then  # If the grep found something other than 0-9  # then it's not an integer.
   	  echo "Invalid value: $2. Please specify a numeric value for minimum java heap memory"
   	  ERROR=2
   	else
   	  if [ $2 -gt ${MIN_HEAP_VAL_IN_MB} ]
    	    then
    	      MIN_HEAP_VAL_IN_MB=$2
    	      #echo Min Heap is: $MIN_HEAP_VAL_IN_MB
    	  fi
    	fi
fi

if [ "$MAX_ARG_PRESENT" == "true" ]
   then
   	#checking whether the input is a number
   	echo $3 | grep "[^0-9]" > /dev/null 2>&1
   	if [ "$?" -eq "0" ]; then  # If the grep found something other than 0-9  # then it's not an integer.
   	  echo "Invalid value: $3. Please specify a numeric value for maximum java heap memory"
   	  ERROR=2
   	else
       	  if [ $3 -ge ${MIN_HEAP_VAL_IN_MB} ]
            then
              MAX_HEAP_VAL_IN_MB=$3
              #echo Min Heap is: $MIN_HEAP_VAL_IN_MB,          	Min Heap is: $MAX_HEAP_VAL_IN_MB
    	  fi
    	fi
fi

if [ ${MIN_HEAP_VAL_IN_MB} -gt ${MAX_HEAP_VAL_IN_MB} ]
  then
    MAX_HEAP_VAL_IN_MB=$MIN_HEAP_VAL_IN_MB
fi

# the command to start the EPAgent
#-Xdebug
EpaCmd="java -Xms${MIN_HEAP_VAL_IN_MB}m -Xmx${MAX_HEAP_VAL_IN_MB}m -Dcom.wily.introscope.epagent.properties=./config/AppSyntheticMonitor.properties -cp lib/EPAgent.jar:lib/com.ca.apm.extensions.epagent.asm-monitor.jar:lib/commons-codec-1.10.jar:lib/json-20131018.jar com.wily.introscope.api.IntroscopeEPAgent"
echo $EpaCmd
# ||||||||||||||||||||   END CONFIGURATION SECTION  ||||||||||||||||||||

cd "${WILYHOME}"

ARGV="$@"
if [ "x$ARGV" = "x" ] ; then
    ARGS="help"
fi

for ARG_RAW in $@ $ARGS
do
    # check for pidfile
    if [ -f "$PIDFILE" ] ; then
	PID=`cat "$PIDFILE"`
	if [ "x$PID" != "x" ] && kill -0 $PID 2>/dev/null ; then
	    STATUS="EPAgent (pid $PID) running"
	    RUNNING=1
	else
	    STATUS="EPAgent (pid $PID?) not running"
	    RUNNING=0
	fi
    else
	STATUS="EPAgent (no pid file) not running"
	RUNNING=0
    fi

    if [ $ERROR -eq 2 ]
      then
      	ARG="help"
      	#echo  VALUE CHANGED to help: $ARG
      else
      	ARG=${ARG_RAW}
      	#echo  VALUE CHANGED to actual: $ARG
    fi

    case $ARG in
      status)
    	if [ $RUNNING -eq 1 ]; then
    		echo "$0 $ARG:  Agent is running"
    	else
    		echo "$0 $ARG:  Agent is stopped"
    	fi
    	;;
    start)
    	if [ $RUNNING -eq 1 ]; then
	    echo "$0 $ARG: EPAgent (pid $PID) already running"
	    continue
	fi
	nohup $EpaCmd >> "$LOGFILE" 2>&1 &
	if [ "x$!" != "x" ] ; then
	    echo "$!" > "$PIDFILE"
	    echo "$0 $ARG: EPAgent started"
	    break;
	else
	    echo "$0 $ARG: EPAgent could not be started"
	    ERROR=3
	fi
	;;
    stop)
	if [ $RUNNING -eq 0 ]; then
	    echo "$0 $ARG: $STATUS"
	    continue
	fi
	if kill $PID ; then
	    rm "$PIDFILE"
	    echo "$0 $ARG: EPAgent stopped"
	else
	    echo "$0 $ARG: EPAgent could not be stopped"
	    ERROR=4
	fi
	;;
    *)
	echo "usage: $0 (start|stop|status|help) [min java heap] [max java heap]"
	cat <<EOF
where
     start      		- start EPAgent
     stop     	 		- stop EPAgent
     status    			- status of EPAgent
     help      			- this screen
     min java heap    		- minimum java heap memory in MB, default is 16
     max java heap		- maximum java heap memory in MB, default is 256

EOF
	ERROR=2
    ;;

    esac
    break
done

exit $ERROR
