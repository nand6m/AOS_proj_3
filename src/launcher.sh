#!/bin/bash
./cleanup.sh $1 &> /dev/null

# Change this to your netid
netid=sxd167930

# Root directory of your project
DCDIR=/people/cs/s/$netid
RELDIR=RCMutex/AOS_proj_3
PROJDIR=$DCDIR/$RELDIR

if [[ $# -ge 1 ]]; then
	CONFIGNAME=$(basename $1)
else
	#Directory where the config file is located on your local system
	CONFIGNAME=config.txt
	#CONFIGNAME=config_grade.txt
	#CONFIGNAME=config_20.txt
	#CONFIGNAME=config_10nodes.txt
fi
CONFIGLOCAL=$HOME/Desktop/$RELDIR/src/$CONFIGNAME
CONFIGREMOTE=$PROJDIR/src/$CONFIGNAME
CONFIGLAUNCHER=$2

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
PROG=Main

#Compile
./Compile.sh #&> /dev/null
echo 'Compiled'

{
#Move
ssh $netid@dc01.utdallas.edu "mkdir -p $RELDIR/src; mkdir -p $RELDIR/bin; mkdir -p $RELDIR/lib"
scp ../lib/* $netid@dc01.utdallas.edu:$RELDIR/lib/
scp Compile.sh $netid@dc01.utdallas.edu:$RELDIR/src/
scp *.java $netid@dc01.utdallas.edu:$RELDIR/src/
scp *.txt $netid@dc01.utdallas.edu:$RELDIR/src/
ssh $netid@dc01.utdallas.edu "./$RELDIR/src/Compile.sh"
} &> /dev/null
echo 'Deployment complete'
i=0

function outputlines () {
	ssh dc01 'wc -l < output.txt'
}

function runLauncher ()
{
	i=0
	oldoutputlines=$( outputlines )
	n=$1
	cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
	(
    		read line
		if [[ $# -ge 3 ]]; then
			n=$1
			d_mean=$2
			c_mean=$3
		else
			n=$( echo $line | awk '{ print $1 }' )
			d_mean=$( echo $line | awk '{ print $2 }' )
			c_mean=$( echo $line | awk '{ print $3 }' )
		fi
    		p=0
		#echo $n $d_mean $c_mean
		./cleanup.sh $CONFIGNAME $n &> /dev/null
		sleep 1s
    		while [[ $i -lt $n ]]
    		do
	    		read line
			p=$( echo $line | awk '{ print $1 }' )
	        	host=$( echo $line | awk '{ print $2 }' )
			
			#gnome-terminal -- bash -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR:$RELDIR/lib/* $PROG $p $CONFIGREMOTE $n $d_mean $c_mean;" &
			#gnome-terminal -- bash -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR:$RELDIR/lib/* $PROG $p $CONFIGREMOTE $n $d_mean $c_mean; exec bash" &
			ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host "nohup java -cp $BINDIR:$RELDIR/lib/* $PROG $p $CONFIGREMOTE $n $d_mean $c_mean" &>> launcher_result.txt &
	
       			i=$(( i + 1 ))
    		done
	)
	newoutputlines=$oldoutputlines
	while [[ $newoutputlines -le $oldoutputlines ]]
	do
		sleep 5s
		newoutputlines=$( outputlines )
	done
	echo 'Completed'
	./cleanup.sh $CONFIGNAME $n &> /dev/null
}

c_echo(){
    RED="\033[0;31m"
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    NC='\033[0m' # No Color

    printf "${!1}${2} ${NC}\n"
}

all_ns=()
all_ds=()
all_cs=()
all_initials=()
all_finals=()
all_steps=()

if [[ $# -ge 2 ]]; then
	while IFS=' ' read n d_mean c_mean initial final steps
	do
		all_ns+=( "$n")
		all_ds+=( "$d_mean" )
		all_cs+=( "$c_mean" )
		all_initials+=( "$initial" )
		all_finals+=( "$final" )
		all_steps+=( "$steps" )
	done < <(sed -e "/^\s*$/d" <(sed -e "s/#.*//" <(cat $CONFIGLAUNCHER)))

	#echo "${#all_ns[@]}"
	for i in ${!all_ns[@]}; do
  		n=${all_ns[$i]}
  		d_mean=${all_ds[$i]}
  		c_mean=${all_cs[$i]}
  		initial=${all_initials[$i]}
  		final=${all_finals[$i]}
		steps=${all_steps[$i]}

		#echo $initial $final $steps
		if [[ $n = '?' ]]; then looper='n'
		elif [[ $d_mean = '?' ]]; then looper='d'
		elif [[ $c_mean = '?' ]]; then looper='c'
		fi
		#echo $looper
		for((j=initial;j<=final;j+=steps))
		do
			if [[ $looper = 'n' ]]; then n=$j
			elif [[ $looper = 'd' ]]; then d_mean=$j
			elif [[ $looper = 'c' ]]; then c_mean=$j
			fi
			c_echo "RED" "Running with combination $n $c_mean $d_mean"
			wait -n; runLauncher $n $d_mean $c_mean
		done	
	done
else
	runLauncher
fi
