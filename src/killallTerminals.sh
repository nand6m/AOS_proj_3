for i in `ps aux | grep 'bash' | cut -s  -d' ' -f2`; do if [[ $i != $$ ]] || [[ $i != $BASHPID ]]; then kill $i; fi; done
