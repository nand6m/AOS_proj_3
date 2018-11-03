import java.util.HashMap;

public class RCMutex implements MsgListener {
	boolean CSrequired = false;
	boolean inCS = false;
	long ReqTimeStamp=0;
	long CurTimeStamp = 0;

	StreamMsg msg=new StreamMsg();
	NodeInfo NI=new NodeInfo();
	boolean keys[];
	HashMap<Integer, Sender> senders;

	//did not complete this
	public void addSender(int neighbor, Sender s) {
		senders.put(neighbor, s);    	
	}

	public void RCMutex(NodeInfo NIobj) {
		this.NI = NIobj;
		int i;	
		keys = new boolean[NIobj.numOfNodes];
		for(i = 0; i < NI.id; i++)
		{
			keys[i] = false;
		}
		for(; i < NI.numOfNodes; i++)
		{
			keys[i] = true;
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

	public synchronized void CS_enter() {
		ReqTimeStamp = CurTimeStamp;
		CSrequired = true;
		sendMissingRequest();	
		while(!inCS) {
			inCS = hasAllKeys();
			if(!inCS) wait();
		}
	}
	public synchronized void CS_leave() {
		inCS = false;
		CSrequired = false;
		notifyAll();
	}

	synchronized void waitForGrant()
	{
		while(inCS || (CSrequired && ReqTimeStamp < m.timestamp)) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public synchronized boolean receive(StreamMsg m){
		CurTimeStamp = Math.max(m.timestamp,CurTimeStamp);
		CurTimeStamp++;
		if(m.type == MsgType.request) {
			waitForGrant();
			//now give
			sendGrant(m.SrcNodeId);
		}
		else if(m.type == MsgType.grant || m.type == MsgType.req_grant) {
			keys[m.sourceNodeId]=true;
			notifyAll();
			if(m.type == MsgType.req_grant)
			{
				waitForGrant();
			}
		}
		return false;	
	}

	public synchronized void sendRequest(int id) {
		msg.type = MsgType.request;
		msg.SrcNodeId = NI.id;
		msg.timestamp = ReqTimeStamp;
		senders.get(id).send(msg);
		ReqTimeStamp++;
		CurTimeStamp++;
	}

	public synchronized void sendGrant(int id) {
		if(CSrequired) {
			msg.type = MsgType.req_grant;
		}else {
			msg.type = MsgType.grant;
		}
		NI.keys[id] = false;
		msg.SrcNodeId = NI.id;
		msg.timestamp = CurTimeStamp;
		senders.get(id).send(msg);
		CurTimeStamp++;
	}

	public synchronized void sendMissingRequest() {
		for(Integer i = 0 ; i < NI.numOfNodes; i++) {
			if(i == NI.id) continue;
			if(NI.keys[i] != true) {
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
