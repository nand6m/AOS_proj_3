//This Tester is for testing safety of our Mutual Exclusion algorithm. To measure performance we use Application.java file
class MutexTest extends Thread 
{
	int i;
	int id;
	int iterations;
	Sender coordinator;
	RCMutex rcm;
	volatile boolean isWaiting = false;

	public MutexTest(int id, int iterations, RCMutex rcm)
	{
		this.id  =  id;
		this.i   =   0;
		this.rcm = rcm;
		this.iterations = iterations;
	}

	public void setCoordinator(Sender s)
	{
		this.coordinator = s;
	}

	public synchronized boolean receive(StreamMsg m, Sender s)
	{
		if(m.type == MsgType.get_i)
		{
			send_i(s);
		}
		else if(m.type == MsgType.set_i)
		{
			i = Integer.parseInt(m.message);
			isWaiting = false;
			notifyAll();
		}
		return false;
	}

	public void send_i(Sender s)
	{
		StreamMsg reply = new StreamMsg();
		reply.sourceNodeId = id;
		reply.type = MsgType.set_i;
		reply.message = Integer.toString(i);
		s.send(reply);
	}

	public synchronized void request_i()
	{
		isWaiting = true;
		StreamMsg m = new StreamMsg();
		m.sourceNodeId = id;
		m.type = MsgType.get_i;
		coordinator.send(m);
		System.out.println("REQUESTED I");
		while(isWaiting){
			try{wait();}   
			catch(InterruptedException ie){ie.printStackTrace();}
		}
		System.out.println("RECEIVED I");
		
	}

	@Override
	public void run()
	{
		for(int j = 0; j < iterations; j++)
		{
			try{
				rcm.cs_enter();
				if(id != 0)
				{
					request_i();
				}
				i = i + 1;
				Thread.sleep(10);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			if(id != 0)
			{
				send_i(coordinator);
			}
			rcm.cs_leave();
		}
		try{Thread.sleep(90000);}
		catch(InterruptedException ie){
			ie.printStackTrace();
		}
		if(id == 0){
			System.out.println("Value of i after testing : "+ i);
		}
	}
}
