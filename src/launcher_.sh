#!/bin/bash

PROG=Main
netid=kxv170930
PROGRAM_PATH=$(pwd)
PROJDIR=/home/010/k/kx/kxv170930/AOS_proj_3
CONFIGLOCAL=/home/010/k/kx/kxv170930/AOS_proj_3/src/config.txt


# Directory where the config file is located on your local system
#CONFIGNAME=config_grade.txt
#CONFIGNAME=config_20.txt
#CONFIGNAME=config_10nodes.txt
CONFIGNAME=config.txt

CONFIGREMOTE=$PROJDIR/src/$CONFIGNAME

# Directory your java classes are in
BINDIR=$PROJDIR/bin

#Compile
./Compile.sh
echo 'Compiled'

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read line
    i=$( echo $line | awk '{ print $1 }' )
    echo $i
    p=0
    while [[ $n -lt $i ]]
    do
    	read line
	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )

	ssh -o StrictHostKeyChecking=no $netid@$host "java -cp $BINDIR:$PROJDIR/lib/* $PROG $p $CONFIGLOCAL "&
        echo Started : $p - $host
        n=$(( n + 1 ))
    done
)


