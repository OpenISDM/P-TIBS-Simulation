package be.datablend.streaming.sail.inferencing;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.*;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

import tw.edu.nctu.cs.pet.TagInfo;

/**
 * User: dsuvee
 * Date: 20/11/11
 */
public class InferenceLoop {

    private Neo4jGraph neograph;
    private NotifyingSail sail;
    private NotifyingSailConnection connection;
    boolean firstLoop = true;
    
    private static String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";

    // Setup the Foward chaing RDFS inferencer
    public InferenceLoop() throws SailException {
		deleteAll(new File("d:\\var"));
		
		File ff = new File("d:\\var\\rdf");
		if(!ff.exists()){   
			ff.mkdir();    
		}
    	
        neograph = new Neo4jGraph("d:\\var\\rdf");
        neograph.setMaxBufferSize(0);
        neograph.getRawGraph().registerTransactionEventHandler(new PushTransactionEventHandler());
        sail = new ForwardChainingRDFSInferencer(new GraphSail(neograph));
        sail.initialize();
        connection = sail.getConnection();
        neograph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    // Add the inference
    public void inference(URI subject, URI predicate, URI object) throws SailException, InterruptedException {
        neograph.startTransaction();
        connection.addStatement(subject, predicate, object);
        connection.commit();
        neograph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    // Parses and add the RDF statement accordingly
    public void inference(String statement) throws SailException, InterruptedException {
        String[] triple = statement.split(" ");
        inference(new URIImpl(triple[0]), new URIImpl(triple[1]), new URIImpl(triple[2]));
    }

    /* Try it out adding the following statements:
   *
   * http://datablend.be/example/teaches http://www.w3.org/2000/01/rdf-schema#domain http://datablend.be/example/teacher
   * http://datablend.be/example/teaches http://www.w3.org/2000/01/rdf-schema#range http://datablend.be/example/student
   * http://datablend.be/example/Davy http://datablend.be/example/teaches http://datablend.be/example/Bob
   *
   * http://datablend.be/example/teacher http://www.w3.org/2000/01/rdf-schema#subClassOf http://datablend.be/example/person
   * http://datablend.be/example/student http://www.w3.org/2000/01/rdf-schema#subClassOf http://datablend.be/example/person
   *
   * http://datablend.be/example/Bob http://datablend.be/example/teaches http://datablend.be/example/Davy
   *
   * */
    /*public static void main(String[] args) throws SailException, InterruptedException {
        InferenceLoop loop = new InferenceLoop();
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("Provide RDF statement:");
            System.out.print("=> ");
            String input = in.nextLine();
            System.out.println("The following edges were created:");
            loop.inference(input);
        }
    }*/
    
    public static void addToGraph(ArrayList<TagInfo> tag_list) throws SailException, InterruptedException {
    	
    	Scanner scanner = new Scanner(System.in);
    	
        InferenceLoop loop = new InferenceLoop();
        
        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss");
        
        ArrayList<String> have_to_do = new ArrayList<String>();

        //Draw the data flow
        for(int i=0;i<tag_list.size();i++){
        	
        	String from_domain = null;
        	String to_domain = null;
        	
        	String[] tmp = tag_list.get(i).getTransFrom().getDomain().split(".nctu", 2);
        	String fromD = tmp[0].substring(7) + "_" + tag_list.get(i).getTransFrom().getName();
        	
        	from_domain = tmp[0].substring(7);
        	
        	ArrayList<String> toDevice = new ArrayList<String>();
        	
        	Date toDate = tag_list.get(i).getTransTime();
        	String s_toDate = sdFormat.format(toDate);
        	
        	
        	for(int j=0;j<tag_list.get(i).getTransTo().size();j++){
				
        		tmp = tag_list.get(i).getTransTo().get(j).getDomain().split(".nctu", 2);
				toDevice.add(tmp[0].substring(7) + "_" + tag_list.get(i).getTransTo().get(j).getName());
        		
			}
        	
        	if(toDevice.size()>1){
        	
	        	for(int j=0;j<toDevice.size();j++){
	        		
	        		to_domain = toDevice.get(j).split("_")[0];
	        		
	        		String t = "";
	        		
	        		/*if(from_domain.compareTo(to_domain)!=0){
	        			t = "(**)";
	        		}*/
	        		
	        		String target = pre_property + fromD + " " + pre_property + s_toDate + t + " " + pre_property + toDevice.get(j) + t;
	        		System.out.println(target);
	        		loop.inference(target);
					//be.datablend.streaming.sail.inferencing.InferenceLoop.addToGraph(pre_property + fromD + " " + pre_property + s_toDate + " " + pre_property + toDevice.get(j));
	        		
	        	}
        	}else{
        		
        		to_domain = toDevice.get(0).split("_")[0];
        		
        		String t = "";
        		
        		if(from_domain.compareTo(to_domain)!=0){
        			t = "(**)";
        		}
        		
        		if(have_to_do.contains(pre_property + fromD + " " + pre_property + s_toDate + t + " " + pre_property + toDevice.get(0))){
        			System.out.println("5566");
        			continue;
        		}
        			
        		
        		System.out.println(pre_property + fromD + " " + pre_property + s_toDate + t + " " + pre_property + toDevice.get(0));
        		have_to_do.add(pre_property + fromD + " " + pre_property + s_toDate + t + " " + pre_property + toDevice.get(0));
        		loop.inference(pre_property + fromD + " " + pre_property + s_toDate + t + " " + pre_property + toDevice.get(0));
        		
        		//System.out.println("GGININDER:");
    			//int gg = scanner.nextInt();
        		
        	}
        	
        	
        }
        
    }
    
    /*public static void preAddToGraph(String input) throws SailException, InterruptedException {
        InferenceLoop loop = new InferenceLoop();

    }
    
    public static void addToGraph(String input) throws SailException, InterruptedException {

        loop.inference(input);
    }*/
    
    public void deleteAll(File path) {
        if (!path.exists()) {
            return;
        }
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAll(files[i]);
        }
        path.delete();
    }

}
