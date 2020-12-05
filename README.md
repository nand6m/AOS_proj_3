# AOS_proj_3
Roucairol &amp; Carvalho's Mutual exclusion

Steps to run the program
1. Unzip the contents of the archive.    
2. Make required modifications to the configuration file which is available in src folder.  
3. Make required changes in launcher & cleanup scripts such as netid and folder path. These scripts are available in src folder.
4. Run launcher script, It would compile java files and display results on terminal.  
	`./launcher.sh`  
or	`./launcher.sh config.txt`
5. To evaluate performance metrics  
	`./launcher.sh config_perf.txt launcher_config.txt`
6. To test safety property of mutual exclusion we created `MutexTest.java`.  
To run comment `app.start()` & `app.join()` (i.e. lines 36, 37) and uncomment `mt.run()` (line 35) inside `Main.java` 
7. To view stdout  
	`cat launcher_result.txt`



Steps to test with different config file 
1. Create the config file in src folder  
2. To launch `./launch.sh <Your_config_file>`

`Note`: Cleanup happens automatically in launcher script
