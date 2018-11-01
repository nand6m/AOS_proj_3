
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadConfigFile {

	public static NodeInfo readConfigFile(String name) throws IOException{
		NodeInfo file=new NodeInfo();
		int node_count = 0,next = 0;
		// Keeps track of current node
		int curNode = 0;
		int NumNodes=0;
		//int adjMtx[][]=null;
		
		String fileName = name;
		System.out.println(fileName);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			
			while((line = br.readLine()) != null) {
				if(line.length() == 0 || line.startsWith("#"))
					continue;
				// Ignore comments and consider only those lines which are not comments
				String[] config_input;
				if(line.contains("#")){
					String[] config_input_comment = line.split("#.*$"); //Ignore text after # symbol
					config_input = config_input_comment[0].split("\\s+");
				}
				else {
					config_input = line.split("\\s+");
				}

				if(next == 0){
					NumNodes=Integer.parseInt(config_input[0]);
					
					file.numOfNodes = Integer.parseInt(config_input[0]);
					file.IRD=Integer.parseInt(config_input[1]);
					
					file.CSexec_time=Integer.parseInt(config_input[2]);
					file.Req_count=Integer.parseInt(config_input[3]);
					System.out.println("Num of Nodes: "+file.numOfNodes+" Inter-req-delay "+ file.IRD + " CS exec_time "+file.CSexec_time+ " Req_count "+file.Req_count);
//					mapFile.minPerActive = Integer.parseInt(config_input[1]);
//					mapFile.maxPerActive = Integer.parseInt(config_input[2]);
//					mapFile.minSendDelay = Integer.parseInt(config_input[3]);
//					mapFile.snapshotDelay = Integer.parseInt(config_input[4]);
//					mapFile.maxNumber = Integer.parseInt(config_input[5]);
					file.adjMtx = new int[NumNodes][NumNodes];
					file.keys = new boolean[NumNodes][NumNodes];
					file.ClientConnectionCount= new int[file.numOfNodes];
					next++;
				}
//				else if(next == 1 && node_count < NumNodes)
//				{							
//					System.out.println(config_input[0]+" "+Integer.parseInt(config_input[1])+" "+Integer.parseInt(config_input[2])+" "+Integer.parseInt(config_input[3]));
//					node_count++;
//					if(node_count ==NumNodes){
//						next = 2;
//					}
//				}
				else if(next == 1 && node_count < NumNodes ) {
					 // System.out.println(config_input[0]);       
					  //System.out.println(Integer.parseInt(config_input[1]));
					  
						file.nodes.add(new Node(Integer.parseInt(config_input[0]),config_input[1],Integer.parseInt(config_input[2])));
						node_count++;
					for(Integer i = 1 ; i < NumNodes ; i++){
						if(curNode != i) {
						//System.out.println(Integer.parseInt(config_input[i]));
						file.adjMtx[curNode][i] = 1;
						file.adjMtx[i][curNode] = 1;
						//file.ClientConnectionCount[curNode] = config_input.length - 2;
						if(curNode < i) {
							file.keys[curNode][i]=true;
							file.keys[i][curNode]=false;
						}
						}
					}
					curNode++;
					if(node_count ==NumNodes){
						next = 2;
					}
				}
				
			}
			br.close();  
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");                  
		}
		for(Integer i=0;i<NumNodes;i++) {
			for(Integer j=0;j<NumNodes;j++) {
			System.out.print(file.keys[i][j]+ " " );
			}
			System.out.println();
		}
		return file;
	}

}

