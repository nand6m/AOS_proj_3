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
	static spanningTreeNode stn;
	static Broadcast broadcast;
	boolean isRunning;
	ObjectOutputStream oos;
	Integer neighborId;

	static void setSpanningTreeNode(spanningTreeNode stninput)
	{
		stn = stninput;
	}

	static void setBroadcast(Broadcast b)
	{
		broadcast = b;
	}

	void setNeighborId(Integer i){
		this.neighborId = i;
		if(stn != null)
		{
			stn.addSender(neighborId, this);
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
			ois = new ObjectInputStream(socket.getInputStream());
			//System.out.println("Message received");		
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		initiate();
		while(isRunning){
			try {			
				StreamMsg msg;
				msg=(StreamMsg) ois.readObject();		
				receive(msg);
			}
			catch(SocketException se){
			}
			catch(EOFException eofe){
				/*if(l.isTerminated()){
					//if program has terminated gracefully then cleanup
					isRunning = false;
					try{
						socket.close();
					}
					catch(IOException ioe){
						ioe.printStackTrace();
					}
				}
				else{*/
					//else show the exception
					System.out.println("Exception from socket id: " + socket.getRemoteSocketAddress());
					eofe.printStackTrace();
					System.exit(2);
				//}
			}
			catch(StreamCorruptedException e) {
				System.out.println("Exception from socket id: " + socket.getRemoteSocketAddress());
				e.printStackTrace();
				System.exit(2);
			}
			catch (IOException e) {
				System.out.println("Exception from socket id: " + socket.getRemoteSocketAddress());
				e.printStackTrace();
				System.exit(2);
			}
			catch (ClassNotFoundException e) {
				System.out.println("Exception from socket id: " + socket.getRemoteSocketAddress());
				e.printStackTrace();
				System.exit(2);
			} 				
		}
		try{
			socket.close();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
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

	@Override
	public boolean receive(StreamMsg m)
	{
		if(m.type == MsgType.initiate)
		{
			//System.out.println("Received initiate from " + m.sourceNodeId);
			setNeighborId(m.sourceNodeId);
		}
		else if(m.type == MsgType.PACK || m.type == MsgType.NACK || m.type == MsgType.parentRequest){
			stn.receive(m);
		}
		else if(m.type == MsgType.broadcast || m.type == MsgType.convergeCast_ack || m.type == MsgType.broadcast_terminate){
			isRunning = !broadcast.receive(m);
		}
		return !isRunning;
	}

	@Override
	public boolean isTerminated()
	{
		return !isRunning;
	}

	void initiate()
	{
		//System.out.println("Sending initiate message to " + socket.getRemoteSocketAddress().toString());
		StreamMsg m = new StreamMsg();
		m.type = MsgType.initiate;
		m.sourceNodeId = NIobj.id; 
		send(m);
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
