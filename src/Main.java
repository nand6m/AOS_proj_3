import java.io.IOException;

public class Main {
	static NodeInfo NIobj=new NodeInfo();

	public static void main(String[] args) throws IOException, InterruptedException {	
		NIobj.id = Integer.parseInt(args[0]);
		NIobj = ReadConfigFile.readConfigFile(args[1],NIobj.id);
		
		
		for(int i=0;i<NIobj.nodes.size();i++){
			NIobj.nodeInfo.put(NIobj.nodes.get(i).nodeId, NIobj.nodes.get(i));
		}
		
             //spanningTreeNode stn = new spanningTreeNode(NIobj.id);
            //MessageManager.setSpanningTreeNode(stn);
		 
		TCPServer server = new TCPServer(NIobj);		
		new TCPClient(NIobj, NIobj.id);
		server.listenforinput();
		System.out.println("All connections done!");
		if(NIobj.id == 1){
			Thread.sleep(2000);
			System.out.println("Initiating Spanning Tree construction");
			//stn.initiateConstruction();
		}
		
		
		
		
//		while(!stn.isTerminated());
//		Broadcast b = new Broadcast(stn);
//		MessageManager.setBroadcast(b);
//		Thread.sleep(2000);
//
//		new BroadcastTester(b, NIobj.numOfNodes);
//		
//		//Producer reads standard I/O and puts in queue
//		Producer p = new Producer(b);
//		//Consumer reads from queue and does broadcast
//		Consumer c = new Consumer(b); 
//		p.start();
//	    c.start();
//		p.join();
//		c.join();
//		MessageManager.joinAllThreads();
	}
}
