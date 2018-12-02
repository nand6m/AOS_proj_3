
// Import required packages
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.lang.Math;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Application extends Thread implements MsgListener{
	private static long throughPut_startTime = 0;
	private static long throughPut_endTime = 0;
	private static long response_startTime = 0;
	private static long response_endTime = 0;
	private static long systemThroughput = 0;
	int num_iteration=0;
	int nodes;
	int nodeId;
	Sender coordinator;
	int reportsReceived ;
	double msgComplexity, totalResponseTime;
	int c_mean,d_mean;
	boolean finished = false;
	
  	ExponentialDistribution d_ed, c_ed;
	RCMutex rcm;
	File f; //Give file path in DC machine
    	HashMap<Integer, Sender> senders;

	public synchronized void addSender(int neighbor, Sender s) {
		senders.put(neighbor, s);
	}

	//Constructor
	public Application (int nodes, int d_mean, int c_mean, int num_iteration, RCMutex rcminput, int nodeId){
		RandomGenerator d_randomGenerator = new Well19937c(System.currentTimeMillis() + nodeId);
		RandomGenerator c_randomGenerator = new Well19937c(System.currentTimeMillis() + nodeId);
		d_ed = new ExponentialDistribution(d_randomGenerator, d_mean);
  		c_ed = new ExponentialDistribution(c_randomGenerator, c_mean);
  	 	this.num_iteration=num_iteration;
		this.rcm = rcminput;	
		this.nodes = nodes;
		this.nodeId = nodeId;
		this.c_mean = c_mean;
		this.d_mean = d_mean;
		this.rcm = rcminput;
		f = new File("output.txt"); //Give file path in DC machine
		senders = new HashMap<Integer, Sender>();
	}

	void recordMetrics(double messageComplexity, double responseTime)
	{
		reportsReceived++;
		msgComplexity += messageComplexity; 
		totalResponseTime += responseTime;
		if(reportsReceived == nodes)
		{
			msgComplexity /= nodes;
			totalResponseTime /= nodes;
			throughPut_endTime = System.currentTimeMillis();
			systemThroughput = throughPut_endTime - throughPut_startTime;
			try{
				DecimalFormat df = new DecimalFormat("0.00");
			
				//Writing results to file (i.e. output.txt)
				FileWriter fw = new FileWriter(f.getAbsoluteFile(), true); // Here 'true' indicates that new data would be appended to file		
				
				//System.out.println("About to write " + nodes + ", " + c_mean + ", " + d_mean 
							//+ ", " + df.format(msgComplexity) + ", " + df.format(totalResponseTime) + ", " + systemThroughput);
				fw.write( nodes + ", " + c_mean + ", " + d_mean + ", " 
						+ df.format(msgComplexity) + ", " + df.format(totalResponseTime) + ", " + systemThroughput + "\n");
				
				fw.flush();
				fw.close();
			}
			catch(IOException ie){
				ie.printStackTrace();
			}
			MessageManager.terminateAll();
		}
	}

	@Override
	public synchronized boolean receive(StreamMsg m)
	{
		if(m.type == MsgType.initiateApplication) //Application
		{
			started = true;
			notifyAll();
		}

		if(m.type == MsgType.metricReport) // Initiate Application to StreamMsg
		{
			String[] msgContent = m.message.split(","); // Message contains msgComplexity & totalResponseTime separated by ","
			recordMetrics(Double.parseDouble(msgContent[0]), Double.parseDouble(msgContent[1]));
		}
		return false;
	}

	public void setCoordinator(Sender s) // setCoordinator - copied from MutexTest.java
	{
		this.coordinator = s;
	}

	boolean started = false;
    	
	@Override
	public synchronized void run(){
		//System.out.println(nodes);
		long d = Math.round(d_ed.sample());
		long c = Math.round(c_ed.sample());
		System.out.println("c, d sampled = " + c + ", " + d);
		//throughPut_startTime = System.currentTimeMillis();
		long total_response_time = 0;
		if(nodeId == 0)
		{
			throughPut_startTime = System.currentTimeMillis();
			StreamMsg msg = new StreamMsg();
			msg.type = MsgType.initiateApplication;
			msg.sourceNodeId = nodeId;
			for(Sender sender : senders.values())
			{
				 sender.send(msg);
			}
		}
		try{
			if(nodeId != 0)
			{ 
				while(!started)
				{
					 wait();
				}
			}
        	for(int i=0; i < num_iteration; i++)
			{
        		        		
        		if(i % 1 == 0)
    			{
    				System.out.print("Running application at iteration# " + (i + 1) + "\r");
    			}
       			//System.out.println(nodes+" Requesting to enter Critical section");
       	 		response_startTime = System.currentTimeMillis();
    			rcm.cs_enter();
				//System.out.println(nodes+" Starting Critical section");
				//Consume some resource
				Thread.sleep(c);
         		//System.out.println(nodes+" Ending Critical section");
				rcm.cs_leave();
    			response_endTime = System.currentTimeMillis();
    			total_response_time += response_endTime - response_startTime;
    			Thread.sleep(d);
			}
        	System.out.println("Running application at iteration# " + num_iteration + " finished");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(nodeId == 0)
		{
			recordMetrics(rcm.getAverageMessageComplexity(), total_response_time);
		}
		else
		{
			StreamMsg msg = new StreamMsg();
			msg.sourceNodeId = nodeId;
			msg.message = rcm.getAverageMessageComplexity() +","+ (((double)total_response_time)/num_iteration);
			msg.type = MsgType.metricReport;
			this.coordinator.send(msg); // msg with msgComplexity + totalResponseTime
			MessageManager.terminateAll();
		}
		System.out.println(rcm.getAverageMessageComplexity() + " message complexity computed");
	}

	@Override
	public boolean isTerminated() {
		return false;
	}
}

