
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NodeInfo {

	int numOfNodes;
	int d_mean;
	int c_mean;
	int num_iteration;
	boolean ConnDone;
	int[][] adjMtx;	
	int ClientConnectionCount[];
	int id;
	ArrayList<Integer> neighbors;
	//int ConnCount;
	//ArrayList<Integer>[][] Khop=new ArrayList<>[][]();
	
	//Mapping between process number as keys and <id,host,port> as value
	HashMap<Integer,Node> nodeInfo;
	
	// Create all the channels in the beginning and keep it open till the end
	// Mapping between each process as a server and its client connections
	//HashMap<Integer,Socket> channels;
	ArrayList<Socket> channels;
		
		
	//ArrayList which holds the total processes(nodes) 
	ArrayList<Node> nodes;
	public NodeInfo() {
	
	nodes = new ArrayList<Node>();	
	nodeInfo = new HashMap<Integer,Node>();
	neighbors = new ArrayList<>();
	//channels= new HashMap<Integer,Socket>();
	channels= new ArrayList<Socket>();
	ConnDone=false;
}

}
