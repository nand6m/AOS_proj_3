
// Import required packages


public class Application extends Thread {
	private static long throughPut_startTime = 0;
	private static long throughPut_endTime = 0;
	private static long response_startTime = 0;
	private static long response_endTime = 0;
    
    //Constructor
    public Application (int nodes, int d_mean, int c_mean, int num_iteration){
        ExponentialDistribution d_ed(d_mean), c_ed(c_mean) ; //doubt ?

    }

    @Override
    public void run(){
        throughPut_startTime = System.currentTimeMillis();
        for(int i=0; i < num_iteration; i++){
            System.out.println("Requesting to enter Critical section");
            response_startTime = System.currentTimeMillis();
            csEnter();
            System.out.println("Starting Critical section");
            wait(c_ed.sample());
            System.out.println("Ending Critical section");
            csLeave();
            response_endTime = System.currentTimeMillis();
            System.out.println("Response Time = " + (response_endTime - response_startTime) +"ms\n");
            wait(d_ed.sample());
        }
        throughPut_endTime = System.currentTimeMillis();
        System.out.println("Throughput = " + (throughPut_endTime - throughPut_startTime) +"ms\n");
    }
}

