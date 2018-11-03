
public interface MsgListener {
	public boolean receive(StreamMsg m);
	public boolean isTerminated();
};
