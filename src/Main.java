import java.io.IOException;

public class Main {
	static NodeInfo NIobj=new NodeInfo();

	public static void main(String[] args) throws IOException, InterruptedException {	
		NIobj.id = Integer.parseInt(args[0]);

		if(args.length >= 5)
		{
			NIobj = ReadConfigFile.readConfigFile(args[1],NIobj.id, 
					Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
		}
		else
		{
			NIobj = ReadConfigFile.readConfigFile(args[1],NIobj.id);
		}
		
		for(int i=0;i<NIobj.nodes.size();i++){
			NIobj.nodeInfo.put(NIobj.nodes.get(i).nodeId, NIobj.nodes.get(i));
		}
		
		RCMutex rcm = new RCMutex(NIobj);
		MutexTest mt = new MutexTest(NIobj.id, NIobj.num_iteration,rcm);
		Application app = new Application(NIobj.numOfNodes, NIobj.d_mean, NIobj.c_mean, NIobj.num_iteration, rcm, NIobj.id);
		MessageManager.setRCMutex(rcm);
		MessageManager.setRCMTester(mt);
		MessageManager.setApplication(app);
		TCPServer server = new TCPServer(NIobj);		
		new TCPClient(NIobj, NIobj.id);
		server.listenforinput();
		System.out.println(NIobj.id+ " All connections done!");
		MessageManager.waitForAllInitiate();
		//System.out.println(NIobj.id +" :id");
		//mt.run();
		app.run();
		
		MessageManager.joinAllThreads();
		
		//Thread.sleep(10000);
	}
}
	
