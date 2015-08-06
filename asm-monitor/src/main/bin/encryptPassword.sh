#!/bin/sh
#
# ASMCtrl.sh
# Control script for running the Introscope EP Agent
# as a Unix service via an easy-to-use command line interface.
# Usage:
# ASMCtrl.sh start
# ASMCtrl.sh status

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

# the command to start the EPAgent
EpaCmd="java -cp lib/EPAgent.jar:lib/ca.apm.swat.asm-monitor.jar com.ca.apm.swat.epaplugins.utils.CryptoUtils $1"
# ||||||||||||||||||||   END CONFIGURATION SECTION  ||||||||||||||||||||

cd "${WILYHOME}"

ARGV="$@"
if [ "x$ARGV" = "x" ] ; then
	echo "usage: $0 <password>"
	exit 2
fi

$EpaCmd
