#!/bin/bash

# Change this to your netid
netid=sxd167930

# Root directory of your project
DCDIR=/people/cs/s/$netid
RELDIR=RCMutex/AOS_proj_3
PROJDIR=$DCDIR/$RELDIR

if [[ $# -ge 1 ]]; then
	CONFIGNAME=$1
else
	#Directory where the config file is located on your local system
	CONFIGNAME=config.txt
	#CONFIGNAME=config_grade.txt
	#CONFIGNAME=config_20.txt
	#CONFIGNAME=config_10nodes.txt
fi
CONFIGLOCAL=$HOME/Desktop/$RELDIR/src/$CONFIGNAME
CONFIGREMOTE=$PROJDIR/src/$CONFIGNAME

#echo $$
#echo $BASHPID

function cleanUp ()
{
	i=0
	cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
	(
	    	read line
		if [[ $# -ge 1 ]]; then	n=$1
		else n=$( echo $line | awk '{ print $1 }' )
		fi
    		
    		while [[ $i -lt $n ]]
    		do
    			read line
			#echo $line
        		host=$( echo $line | awk '{ print $2 }' )
       		 	#echo $host 
       	 		#gnome-terminal -- ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid &
        		ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host "killall -u $netid" &
			pids[${i}]=$!
        		#sleep 1
        		i=$(( i + 1 ))
    		done
		# wait for all pids
		echo "${pids[@]}" >> cleanup_pids.txt
		for pid in ${pids[*]}; do
    			wait $pid
		done
	)
}

if [[ $# -ge 2 ]]; then cleanUp $2
else cleanUp
fi
echo "Cleanup complete"
