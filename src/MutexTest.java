//This Tester is for testing safety of our Mutual Exclusion algorithm. To measure performance we use Application.java file
class MutexTest extends Thread 
{
	int i;
	int id;
	int iterations;
	Sender coordinator;
	RCMutex rcm;

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

	public boolean receive(StreamMsg m, Sender s)
	{
		if(m.type == MsgType.get_i)
		{
			send_i(s);
		}
		else if(m.type == MsgType.set_i)
		{
			i = Integer.parseInt(m.message);
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

	public void request_i()
	{
		StreamMsg m = new StreamMsg();
		m.sourceNodeId = id;
		m.type = MsgType.get_i;
		coordinator.send(m);
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
				Thread.sleep(100);
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
	}
}
