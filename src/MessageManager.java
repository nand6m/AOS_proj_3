import java.util.ArrayList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.io.EOFException;
import java.io.ObjectOutputStream;

public class MessageManager extends Thread implements MsgListener, Sender{
	Socket socket;
	NodeInfo NIobj;
	MsgListener l;
	//static spanningTreeNode stn;
	//static Broadcast broadcast;
	static RCMutex rcm;
	static MutexTest mt;
	static Application app;
	boolean isRunning;
	ObjectOutputStream oos;
	Integer neighborId;

//	static void setSpanningTreeNode(spanningTreeNode stninput)
//	{
//		stn = stninput;
//	}

//	static void setBroadcast(Broadcast b)
//	{
//		broadcast = b;
//	}


	static void setRCMTester(MutexTest m)
	{
		mt = m;
	}

	static void setApplication(Application a)
	{
		app = a;
	}

	static void setRCMutex(RCMutex rcminput)
	{
		rcm = rcminput;
	}

	void setNeighborId(Integer i){
		this.neighborId = i;
		/*if(stn != null)
		{
			stn.addSender(neighborId, this);
		}*/
		if(rcm != null)
		{
			rcm.addSender(neighborId, this);
		}
		if(app != null)
		{
			app.addSender(neighborId, this);
		}	
		if(i == 0)
		{
			mt.setCoordinator(this);
			app.setCoordinator(this);	
		}
	}

	public MessageManager(Socket socket,NodeInfo NIobj) {
		this.socket=socket;
		this.NIobj=NIobj;	
		activeManagers.add(this);
		isRunning = true;
		try{
			oos = new ObjectOutputStream(socket.getOutputStream());
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	public void run() {
		ObjectInputStream ois = null;
		//DataInputStream dis=null;
		try {
			initiate();
			ois = new ObjectInputStream(socket.getInputStream());		
			while(isRunning){
				StreamMsg msg;
				msg=(StreamMsg) ois.readObject();
				//System.out.println("Message received");
				receive(msg);
			}
			if(!terminateSent)
			{
				terminate();
			}
			socket.close();
		}
		catch(Exception e){
			System.out.println("Exception from socket id: " + socket.getRemoteSocketAddress());
			e.printStackTrace();
			System.exit(2);
		}
		if(rcm != null)
		{
			rcm.receiveKey(neighborId);
		}
	}

	@Override
	public boolean receive(StreamMsg m)
	{
		if(m.type == MsgType.initiate)
		{
			//System.out.println("Received initiate from " + m.sourceNodeId);
			setNeighborId(m.sourceNodeId);
		}
		/*else if(m.type == MsgType.PACK || m.type == MsgType.NACK || m.type == MsgType.parentRequest){
			stn.receive(m);
		}
		else if(m.type == MsgType.broadcast || m.type == MsgType.convergeCast_ack || m.type == MsgType.broadcast_terminate){
			isRunning = !broadcast.receive(m);
		}*/
		else if(m.type == MsgType.request || m.type == MsgType.grant || m.type == MsgType.req_grant)
		{
			rcm.receive(m);
		}
		else if(m.type == MsgType.set_i || m.type == MsgType.get_i || m.type == MsgType.done_i)
		{
			mt.receive(m, this);
		}
		else if(m.type == MsgType.initiateApplication || m.type == MsgType.metricReport)
		{
			app.receive(m);
		}
		else if(m.type == MsgType.terminate)
		{
			isRunning = false;
		}
		return !isRunning;
	}

	@Override
	public boolean isTerminated()
	{
		return !isRunning;
	}

	static ArrayList<MessageManager> activeManagers = new ArrayList<MessageManager>();
	public static void joinAllThreads(){
		try{
			for(int i = 0; i < activeManagers.size(); i++){
				activeManagers.get(i).join();
			}
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static void terminateAll()
	{
		for(int i = 0; i < activeManagers.size(); i++) {
			activeManagers.get(i).terminate();
		}
	}
	
	void initiate()
	{
		//System.out.println("Sending initiate message to " + socket.getRemoteSocketAddress().toString());
		StreamMsg m = new StreamMsg();
		m.type = MsgType.initiate;
		m.sourceNodeId = NIobj.id; 
		send(m);
	}
	
	boolean terminateSent = false;
	synchronized void terminate()
	{
		if(terminateSent) return;
		StreamMsg m = new StreamMsg();
		m.type = MsgType.terminate;
		m.sourceNodeId = NIobj.id; 
		send(m);
		terminateSent = true;
	}

	@Override
	public synchronized void send(StreamMsg m)
	{
		if(socket.isClosed()){
			//System.out.println("Socket closed");
			return;
		}
		try {
			oos.writeObject(m);
			oos.flush();
		} 
		catch (IOException e)
		{
			//System.out.println("cant send this msg");
			//e.printStackTrace();
			System.out.println("Socket " + neighborId  + " terminated before receiving " + m.type + " message.");
			try{
				socket.close();
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
		}
	}
}
