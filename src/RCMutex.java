import java.util.HashMap;

public class RCMutex implements MsgListener,Sender {
	boolean CSrequired = false;
	boolean inCS = false;
	int ReqTimeStamp=0;
	int CurTimeStamp = 0;

	StreamMsg msg=new StreamMsg();
	NodeInfo NI=new NodeInfo();
	//boolean keys[];
	HashMap<Integer, Sender> senders;
	//did not complete this
	public void addSender(int neighbor, Sender s) {
		senders.put(neighbor, s);    	
	}

	public void RCMutex(NodeInfo NIobj) {
		this.NI = NIobj;	
	}

	public synchronized void CS_enter() {
		CSrequired = true;
		while(!inCS) {
			sendMissingRequest();	
		}
	}
	public synchronized void CS_leave() {
		inCS = false;
		CSrequired = false;
		notifyAll();

	}
	public boolean receive(StreamMsg m){
		ReqTimeStamp= CurTimeStamp = Math.max(m.timestamp,CurTimeStamp);
		CurTimeStamp++;
		if(m.type == MsgType.request) {
			while(inCS) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//now give
			sendGrant(m.SrcNodeId);
		}
		else if(m.type == MsgType.grant || m.type == MsgType.req_grant) {
			NI.keys[m.SrcNodeId]=true;
			notifyAll();
			//dono wat to do for req_grant....its a grant first and request next...shud save it?
		}
		return false;	
	}
	public synchronized void sendRequest(int id) {
		msg.type = MsgType.request;
		msg.SrcNodeId = NI.id;
		msg.timestamp = ReqTimeStamp;
		send(msg);
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
		send(msg);
		CurTimeStamp++;
	}
	public synchronized void sendMissingRequest() {
		int count = 0;
		for(Integer i = 0 ; i < NI.numOfNodes && i != NI.id; i++) {
			if(NI.keys[i] == true) {
				count++;
				if(count == NI.numOfNodes - 1) {
					inCS = true;
					try {
						Thread.sleep(NI.c_mean); //executing critical section
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else {
				sendRequest(i);
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}



	}
	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void send(StreamMsg m) {
		// TODO Auto-generated method stub

	}
}
