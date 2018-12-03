#!/bin/bash
./cleanup.sh $1

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

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
PROG=Main

#Compile
./Compile.sh
echo 'Compiled'

#Move
ssh $netid@dc01.utdallas.edu "mkdir -p $RELDIR/src; mkdir -p $RELDIR/bin; mkdir -p $RELDIR/lib"
scp ../lib/* $netid@dc01.utdallas.edu:$RELDIR/lib/
scp Compile.sh $netid@dc01.utdallas.edu:$RELDIR/src/
scp *.java $netid@dc01.utdallas.edu:$RELDIR/src/
scp *.txt $netid@dc01.utdallas.edu:$RELDIR/src/
ssh $netid@dc01.utdallas.edu "./$RELDIR/src/Compile.sh"
echo 'Deployment complete'

i=0

oldoutputlines=$(ssh dc01 'wc -l < output.txt')
echo $oldoutputlines
sleep 5s
cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read line
    n=$( echo $line | awk '{ print $1 }' )
    #echo $n
    #sleep 5s
    p=0
    while [[ $i -lt $n ]]
    do
    	read line
	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
	
	#gnome-terminal -- bash -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR:$RELDIR/lib/* $PROG $p $CONFIGREMOTE;" &
	#gnome-terminal -- bash -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR:$RELDIR/lib/* $PROG $p $CONFIGREMOTE; exec bash" &
	ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host "java -cp $BINDIR:$RELDIR/lib/* $PROG $p $CONFIGREMOTE" &

       i=$(( i + 1 ))
    done
)
newoutputlines=$oldoutputlines
while [[ $newoutputlines -le $oldoutputlines ]]
do
	sleep 20s
	newoutputlines=$(ssh dc01 'wc -l <output.txt')
done
#sleep 2m

