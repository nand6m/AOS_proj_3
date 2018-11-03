import java.io.Serializable;
import java.util.ArrayList;

enum MsgType{request,grant,req_grant};
public class StreamMsg implements Serializable {
	int timestamp;
	int SrcNodeId;
	MsgType type;
	String message;

	public StreamMsg(){
		timestamp = -1;
		SrcNodeId = -1;
		type = MsgType.request;
		message = " ";
	}
}
