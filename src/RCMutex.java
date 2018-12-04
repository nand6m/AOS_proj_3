import java.util.HashMap;

public class RCMutex implements MsgListener {
	boolean CSrequired = false;
	boolean inCS = false;
	long ReqTimeStamp=0;
	long CurTimeStamp = 0;

	double messagesReceived = 0;
	double csRequests = 0;
	
	public synchronized double getAverageMessageComplexity()
	{
		return messagesReceived/csRequests;	
	}

	//StreamMsg msg=new StreamMsg();
	NodeInfo NI;
	boolean keys[];
	boolean keyRequired[];
	HashMap<Integer, Sender> senders;

	public synchronized void addSender(int neighbor, Sender s) {
		senders.put(neighbor, s);
		notifyAll();
		//System.out.println("Adding "+ neighbor+ " socket to senders");    	
	}

	public RCMutex(NodeInfo NIobj) {
		this.NI = NIobj;
		int i;
		this.senders = new HashMap<Integer , Sender>();	
		keys = new boolean[NIobj.numOfNodes];
		keyRequired = new boolean[NIobj.numOfNodes];
		for(i = 0; i < NI.id; i++)
		{
			keys[i] = false;
			keyRequired[i] = false;
		}
		for(; i < NI.numOfNodes; i++)
		{
			keys[i] = true;
			keyRequired[i] = false;
		}
	}

	boolean hasAllKeys()
	{
		for(int i = 0; i < NI.numOfNodes; i++)
		{
			if(keys[i] == false) return false;
		}
		return true;
	}

	public synchronized void cs_enter() throws InterruptedException
	{	
		csRequests++;
		ReqTimeStamp = CurTimeStamp + 1;
		CSrequired = true;
		sendMissingRequest();	
		inCS = hasAllKeys();
		while(!inCS) {
			wait();
			inCS = hasAllKeys();
		}
	}
	public synchronized void cs_leave() {
		inCS = false;
		CSrequired = false;
		sendRequiredKeys();
	}

	void sendRequiredKeys()
	{
		for(int i = 0; i < NI.numOfNodes; i++)
		{
			if(keyRequired[i] == true)
			{
				keyRequired[i] = false;
				sendGrant(i);
			}
		}

	}

	synchronized void recordRequest(Integer sourceNodeId , long timestamp )
	{
		if(inCS || (CSrequired && ReqTimeStamp < timestamp) || (CSrequired && ReqTimeStamp == timestamp && NI.id < sourceNodeId) ) 
		{
			keyRequired[sourceNodeId] = true;
			/*try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		else
		{
			sendGrant(sourceNodeId);
		}
	}

	public synchronized boolean receive(StreamMsg m){
		messagesReceived++;
		CurTimeStamp = Math.max(m.timestamp,CurTimeStamp);
		CurTimeStamp++;
		if(m.type == MsgType.request) {
			recordRequest(m.sourceNodeId , m.timestamp);
			}
		else if(m.type == MsgType.grant || m.type == MsgType.req_grant) {
			keys[m.sourceNodeId]=true;
			notifyAll();
			if(m.type == MsgType.req_grant)
			{
				recordRequest(m.sourceNodeId , m.timestamp);
			}
		}
		return false;	
	}
	
	public synchronized void receiveKey(int neighborId)
	{
		keys[neighborId] = true;
		notifyAll();
	}

	public synchronized void sendRequest(int id) {
		StreamMsg msg=new StreamMsg();
		msg.type = MsgType.request;
		msg.sourceNodeId = NI.id;
		msg.timestamp = ReqTimeStamp;
		//System.out.println(NI.id+" Sending "+ msg.type +" to "+id +" ,timestamp: "+msg.timestamp);
		try{
			while(!senders.containsKey(id))
			{ 
				wait();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		senders.get(id).send(msg);
		//ReqTimeStamp++;
		CurTimeStamp++;
	}

	public synchronized void sendGrant(int id) {
		StreamMsg msg=new StreamMsg();
		if(CSrequired) {
			msg.type = MsgType.req_grant;
		}else {
			msg.type = MsgType.grant;
		}
		keys[id] = false;
		msg.sourceNodeId = NI.id;
		msg.timestamp = CurTimeStamp;
		//System.out.println(NI.id+" Sending "+ msg.type +" to "+id +" ,timestamp: "+ msg.timestamp);
		try{
			while(!senders.containsKey(id))
			{ 
				wait();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		senders.get(id).send(msg);
		CurTimeStamp++;
	}

	public synchronized void sendMissingRequest() {
		for(Integer i = 0 ; i < NI.numOfNodes; i++) {
			if(i == NI.id) continue;
			if(keys[i] != true) {
				sendRequest(i);
			}
		}
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}
}
