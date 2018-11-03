import java.io.IOException;

public class Main {
	static NodeInfo NIobj=new NodeInfo();

	public static void main(String[] args) throws IOException, InterruptedException {	
		NIobj.id = Integer.parseInt(args[0]);
		NIobj = ReadConfigFile.readConfigFile(args[1],NIobj.id);


		for(int i=0;i<NIobj.nodes.size();i++){
			NIobj.nodeInfo.put(NIobj.nodes.get(i).nodeId, NIobj.nodes.get(i));
		}

		RCMutex rcm = new RCMutex(NIobj);
		MessageManager.setRCMutex(rcm);

		TCPServer server = new TCPServer(NIobj);		
		new TCPClient(NIobj, NIobj.id);
		server.listenforinput();
		System.out.println("All connections done!");
		Thread.sleep(2000);
		Application app = new Application(NIobj.id, NIobj.d_mean, NIobj.c_mean, NIobj.num_iteration, rcm);
		app.run();
	}
}
