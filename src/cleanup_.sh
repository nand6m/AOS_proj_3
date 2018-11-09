#!/bin/bash


# Change this to your netid
netid=kxv170930

#
# Root directory of your project
PROJDIR=/home/010/k/kx/kxv170930/AOS_proj_3

#
# Directory where the config file is located on your local system
CONFIGLOCAL=/home/010/k/kx/kxv170930/AOS_proj_3/src/config.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
 read line
    i=$( echo $line | awk '{ print $1 }' )
    echo $i

    while [[ $n -lt $i ]]
    do
    	read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid &
        sleep 1

        n=$(( n + 1 ))
    done   
)


echo "Cleanup complete"
