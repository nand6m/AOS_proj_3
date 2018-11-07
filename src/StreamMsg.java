import java.io.Serializable;
import java.util.ArrayList;

enum MsgType{initiate,neighbor,okay,terminate,PACK,NACK,parentRequest,broadcast,convergeCast_ack,broadcast_terminate,request,grant,req_grant,get_i,set_i};
public class StreamMsg implements Serializable {
	int sourceNodeId;
	int immediateSourceNodeId;
	long timestamp;
	MsgType type;
	String message;

	public StreamMsg(){
		sourceNodeId = -1;
		immediateSourceNodeId = -1;
		timestamp = -1;
		type = MsgType.okay;
		message = " ";
	}
}
