
// Import required packages
import org.apache.commons.math3.distribution.*;
import java.lang.Math;

public class Application extends Thread {
	private static long throughPut_startTime = 0;
	private static long throughPut_endTime = 0;
	private static long response_startTime = 0;
	private static long response_endTime = 0;
	int num_iteration=0;
    	ExponentialDistribution d_ed, c_ed;
	RCMutex rcm;
    	
	//Constructor
    	public Application (int nodes, int d_mean, int c_mean, int num_iteration, RCMutex rcminput){
        	d_ed = new ExponentialDistribution(d_mean);
      	  	c_ed = new ExponentialDistribution(c_mean);
       		this.num_iteration=num_iteration;
		this.rcm = rcminput;
	}

    	@Override
    	public void run(){
		long d = Math.round(d_ed.sample());
		long c = Math.round(c_ed.sample());
		throughPut_startTime = System.currentTimeMillis();
		long total_response_time = 0;
        	for(int i=0; i < num_iteration; i++){
            		System.out.println("Requesting to enter Critical section");
           	 	response_startTime = System.currentTimeMillis();
			try {
        			rcm.cs_enter();
				System.out.println("Starting Critical section");
				//Consume some resource
				Thread.sleep(c);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            		System.out.println("Ending Critical section");
          		rcm.cs_leave();
            		response_endTime = System.currentTimeMillis();
			total_response_time += response_endTime - response_startTime;
            		try {
				Thread.sleep(d);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	}
        	throughPut_endTime = System.currentTimeMillis();
            	System.out.println("Average Response Time = " + (total_response_time/num_iteration) +"ms\n");
        	System.out.println("Throughput = " + (throughPut_endTime - throughPut_startTime) +"ms for CS time of " + c "ms\n");
    	}
}

