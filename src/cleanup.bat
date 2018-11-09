@rem !/bin/bash

@rem Change this to your netid
SET netid=sxd167930

@rem Root directory of your project
SET DCDIR=/people/cs/s/$netid
SET RELDIR=RCMutex/AOS_proj_3
SET PROJDIR=$DCDIR/$RELDIR

@rem Directory where the config file is located on your local system
@rem SET CONFIGNAME=config_grade.txt
@rem SET CONFIGNAME=config_20.txt
@rem SET CONFIGNAME=config_10nodes.txt
SET CONFIGNAME=config.txt
SET CONFIGLOCAL=$HOME/Desktop/$RELDIR/src/$CONFIGNAME
SET CONFIGREMOTE=$PROJDIR/src/$CONFIGNAME

SET n=0

for /F "tokens=1 usebackq delims=#" %A in (`TYPE config.txt ^| findstr /B /V /C:#`) DO @echo %A

cat $CONFIGLOCAL | sed -e "s/@rem.*//" | sed -e "/^\s*$/d" |
(
    read line
    i=$( echo $line | awk '{ print $1 }' )
    echo $i

    while [[ $n -lt $i ]]
    do
    	read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        gnome-terminal -- ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid &
        sleep 1

        n=$(( n + 1 ))
    done
   
)

for i in `ps aux | grep bash | cut -s  -d' ' -f6`; do if [[ $i != $$ ]]; then kill $i; fi; done

echo "Cleanup complete"
