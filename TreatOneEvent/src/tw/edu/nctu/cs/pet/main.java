package tw.edu.nctu.cs.pet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;


public class main {
	
	//Simulation setting
	private static final int bs_amount = 20;
	private static final int domain_amount = 3;
	private static final int mobile_amount_per_domain = 50;
	private static final int mobile_amount = mobile_amount_per_domain*domain_amount;
	private static final int device_amount = bs_amount+mobile_amount;
	private static final int seed = 1;
	private static final double beacon_pub_time = 600.0;	//sec
	//private static final double beacon_ttl_time = 600.0;	//sec
	//private static final int beacon_ttl_hop = 3;
	
	//The One Event Log file path
	private static final String filepath = "C:\\OneEvent\\eventlog.txt";
	private static final String eventlog_filepath = "C:\\OneEvent\\eventlog_" + bs_amount + "_" + domain_amount + "_" + mobile_amount_per_domain + "_" + seed + ".txt";
	private static final int doc_pub_amount = 300;
	private static final String doc_size_filepath = "C:\\One\\doc_size_" + doc_pub_amount + ".txt";
	//private static final boolean doc_size_make = true;
	
	private static final String power_filepath = "C:\\One\\power_of_" + mobile_amount + "_mobile.txt";
	
	public static int getDeviceAmount(){
		return device_amount;
	}
	
	public static int getDocAmount(){
		return doc_pub_amount;
	}
	
	private static final double document_size_min = 100.0;
	private static final double document_size_max = 1024.0;
	private static final double tag_size = 4.0;
	
	public static double getTagSize(){
		return tag_size;
	}
	
	private static final double tag_max_size = 400.0;
	private static final double bs_bandwidth = 10240.0; //kbps
	private static final double mobile_bandwidth = 2048.0; //kbps
	private static final int bs_transmit_max = (int)(bs_bandwidth/mobile_bandwidth);
	private static final double doc_pub_freq = 120.0;
	private static final boolean send_by_freq = true;
	private static final double check_freq = 1.0;
	private static final double end_time = 42000.0;
	private static final int tag_trans_method = 1; //1:epdemic 2:固定機率(0.5/0.3/0.1)[跟在tag上] 3:依據文件到達時間計算機率，機率隨時間增加而提升。
	private static final double power_min = 1800.0;	//Time: Second	0.5hr
	private static final double power_max = 172800.0;		//2 Days
	
	//Tag Transmit setting
	private static final int tag_max_copies = 16;
	private static final double tag_threshold_power = 0.1;
	private static final double tag_critical_power = 0.1;
	private static final double alpha = 0.4;
	private static final double beta = 0.3;
	private static final double gamma = 0.3;
	
	//Simulation Control
	private static ArrayList<ArrayList<Integer>> buffer_list = new ArrayList<ArrayList<Integer>>(); //Device --> Document Number
	private static ArrayList<ArrayList<Double>> sending_list = new ArrayList<ArrayList<Double>>();  //Document --> Device sending size
	private static ArrayList<Boolean> sending_empty_list = new ArrayList<Boolean>();
	private static int[][] device_to_device_sending_document = new int[bs_amount+mobile_amount][bs_amount+mobile_amount];
	private static int[][] device_to_device_sending_tags_from = new int[bs_amount+mobile_amount][bs_amount+mobile_amount];
	private static int[][] device_to_device_sending_tags_to = new int[bs_amount+mobile_amount][bs_amount+mobile_amount];
	private static Tag[][] device_to_device_sending_tags = new Tag[bs_amount+mobile_amount][bs_amount+mobile_amount];
	private static double[][] device_to_device_sending_document_size = new double[bs_amount+mobile_amount][bs_amount+mobile_amount];
	private static ArrayList<Integer> sending_from = new ArrayList<Integer>();
	private static ArrayList<Integer> sending_to = new ArrayList<Integer>();
	private static ArrayList<ArrayList<Integer>> bs_sending_to = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<ArrayList<Integer>> connect_list = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<Double> next_do_time_list = new ArrayList<Double>();
	private static ArrayList<Double> doc_size_list = new ArrayList<Double>();
	private static ArrayList<Integer> doc_receive_amount_list = new ArrayList<Integer>();
	private static ArrayList<ArrayList<Tag>> tag_list = new ArrayList<ArrayList<Tag>>();
	private static ArrayList<ArrayList<Tag>> pub_tag_list = new ArrayList<ArrayList<Tag>>();
	//private static boolean[][][][] tag_in_device_bool = new boolean[device_amount][doc_pub_amount+1][device_amount][device_amount];
	private static ArrayList<ArrayList<ArrayList<Integer>>> tag_in_device_list = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private static ArrayList<Boolean> sending_is_tag = new ArrayList<Boolean>();	//Set in from_device
	private static TagManager tm = new TagManager();
	private static ArrayList<Double> power_list = new ArrayList<Double>();
	private static ArrayList<Boolean> device_alive = new ArrayList<Boolean>();
	private static ArrayList<ArrayList<Double>> device_doc_recv_time = new ArrayList<ArrayList<Double>>();
	//private static Tag[][][] tag_in_device = new Tag[device_amount][doc_pub_amount+1][device_amount][device_amount];
	private static boolean[][][] tag_recv_in_bs = new boolean[doc_pub_amount+1][device_amount][device_amount];
	private static ArrayList<Boolean> treat_ok_this_round = new ArrayList<Boolean>();
	//private static TagInDevice[] tag_in_device = new TagInDevice[device_amount];
	private static ArrayList<DeviceNumberInTag> device_num_in_tag = new ArrayList<DeviceNumberInTag>();
	//private ArrayList<TagInDevice> real_tag_in_device = new ArrayList<TagInDevice>
	private static ArrayList<TagInDevice> tag_in_device = new ArrayList<TagInDevice>();
	private static ArrayList<Tag> all_tag_list = new ArrayList<Tag>();
	
	
	private static int pub_doc_count = 0;
	private static double last_conn_time =0.0;
	private static double last_check_time = 0.0;
	private static double last_pub_time = 0.0;
	private static int doc_recv_count = 0;
	private static int last_pub_doc = 0;
	private static int tag_trans_count = 0;
	private static int tag_recv_count = 0;
	
	private static double now = 0.0;
	
	//output counter
	private static int receive_count = 0;
	private static ArrayList<ArrayList<Double>> doc_rev_count_time = new ArrayList<ArrayList<Double>>();
	private static ArrayList<Double> tag_rev_count_time = new ArrayList<Double>();
	
	private static int count5566 = 0;
	private static int count555666 = 0;
	
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		
		Random ran = new Random();
		
		for(int i=0;i<doc_pub_amount+1;i++){
			for(int j=0;j<device_amount;j++){
				for(int k=0;k<device_amount;k++){
					tag_recv_in_bs[i][j][k] = false;
				}
			}
		}
		
		for(int i=0;i<device_amount;i++){
			device_alive.add(true);
			treat_ok_this_round.add(false);
			tag_in_device.add(new TagInDevice());
		}
		
		ArrayList<Integer> tmp = null;
		ArrayList<Integer> tmp2 = null;
		ArrayList<Tag> tmp3 = null;
		for(int j=0;j<device_amount;j++){
			tmp = new ArrayList<Integer>();
			tmp2 = new ArrayList<Integer>();
			tmp3 = new ArrayList<Tag>();
			buffer_list.add(tmp);
			connect_list.add(tmp2);
			tag_list.add(tmp3);
			sending_empty_list.add(true);
			sending_is_tag.add(false);
		}
		
		ArrayList<Double> tmp4 = null;
		for(int i=0;i<doc_pub_amount+1;i++){
			tmp4 = new ArrayList<Double>();
			doc_receive_amount_list.add(0);
			doc_rev_count_time.add(tmp4);
		}
		
		ArrayList<Integer> tmp5 = null;
		for(int i=0;i<bs_amount;i++){
			tmp5 = new ArrayList<Integer>();
			bs_sending_to.add(tmp5);
		}
		
		
		
		//Setting Document Size
		File doc_size_file = new File(doc_size_filepath);
		if(!doc_size_file.exists()){
			FileWriter fw;
			BufferedWriter bufferWritter;
			try {
				fw = new FileWriter(doc_size_filepath, true);
				bufferWritter = new BufferedWriter(fw);
				
				doc_size_list.add(-1.0);
				for(int i=0;i<doc_pub_amount;i++){
					double temp = -2.0;
					do{
						temp = Math.abs(ran.nextGaussian()/2.0);
					}while(temp <0.0 || temp >1.0);
					//System.out.println(temp);
					double doc_size = temp*(document_size_max-document_size_min) + document_size_min;
					doc_size_list.add(doc_size);
					System.out.println("Doc" + (i+1) + " size = " + doc_size_list.get(i+1));
					
					bufferWritter.write("" + doc_size);
    				bufferWritter.newLine();
    				bufferWritter.flush();
    			    //bufferWritter.close();
    				//fw.append(descString);
    				//fw.close();
					
				}
				fw.close();
				bufferWritter.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			try{
	        	FileReader fr = new FileReader(doc_size_filepath);
	        	BufferedReader br = new BufferedReader(fr);
	        	String temp = br.readLine();
	        	doc_size_list.add(-1.0);
	        	while(temp != null){
	        		doc_size_list.add(Double.parseDouble(temp));
	        		temp = br.readLine();
	        	}
			}catch(Exception e){
	        	e.printStackTrace();
	        }
			if(doc_size_list.size() != doc_pub_amount+1){
				System.out.println("Set Document Size Error.");
				Scanner scanner = new Scanner(System.in);
				String t = scanner.nextLine();
			}
		}
		
		
		
		
		//Setting Device Power
		File power_file = new File(power_filepath);
		if(!power_file.exists()){
			FileWriter fw;
			BufferedWriter bufferWritter;
			try {
				fw = new FileWriter(power_file, true);
				bufferWritter = new BufferedWriter(fw);
				
				for(int i=0;i<bs_amount;i++){
					power_list.add(1.0);
				}
				
				for(int i=0;i<mobile_amount;i++){
					double temp = -2.0;
					do{
						temp = Math.abs(ran.nextGaussian()/2.0);
					}while(temp <0.0 || temp >1.0);
					//System.out.println(temp);
					//double power = temp*(power_max-power_min) + power_min;
					power_list.add(temp);
					System.out.println("Device" + (i+bs_amount) + " power = " + power_list.get(i+bs_amount));
					
					bufferWritter.write("" + temp);
    				bufferWritter.newLine();
    				bufferWritter.flush();
    			    //bufferWritter.close();
    				//fw.append(descString);
    				//fw.close();
					
				}
				fw.close();
				bufferWritter.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			try{
	        	FileReader fr = new FileReader(power_filepath);
	        	BufferedReader br = new BufferedReader(fr);
	        	String temp = br.readLine();
	        	for(int i=0;i<bs_amount;i++){
					power_list.add(1.0);
				}
	        	while(temp != null){
	        		power_list.add(Double.parseDouble(temp));
	        		temp = br.readLine();
	        	}
			}catch(Exception e){
	        	e.printStackTrace();
	        }
			if(power_list.size() != device_amount){
				System.out.println("Set Device Power Error.");
				Scanner scanner = new Scanner(System.in);
				String t = scanner.nextLine();
			}
		}
		
		
		//Add power_down checkpoint in 
		for(int i=bs_amount;i<device_amount;i++){
			next_do_time_list.add(power_max * power_list.get(i));
		}
		
		
		if(doc_pub_freq == 0.0){
			
			for(int i=0;i<bs_amount;i++){
				for(int j=0;j<doc_pub_amount;j++){
					buffer_list.get(i).add(j+1);
				}
			}
			last_pub_doc = doc_pub_amount;
		}
		
		for(int i=0;i<doc_pub_amount;i++){
			next_do_time_list.add(doc_pub_freq * (double)(i+1));
		}
		
		//Add pub beacon time to next_do_time_list
		for(double i=beacon_pub_time;i<end_time;i+=beacon_pub_time){
			next_do_time_list.add(beacon_pub_time);
		}
		
		
		try{
        	FileReader fr = new FileReader(eventlog_filepath);
        	BufferedReader br = new BufferedReader(fr);
        	String temp = br.readLine();
        	String[] event = null;
        	//double now = 0.0;
        	double time_flow = 0.0;
        	int index = 0;
        	
        	while(temp != null){
        		
        		event = temp.split(" ", 4);
        		
        		now = Double.parseDouble(event[3]);
        		
        		System.out.println("now " + now);
        		
        		//time_flow = now - last_conn_time;
        		
        		Collections.sort(next_do_time_list);
        		
        		index = 0;     		
        		
        		temp = br.readLine();
        		
        		//The Document sending ok checkpoint
        		while(next_do_time_list.size()>index && next_do_time_list.get(index)<=now){
        			
        			System.out.println("Treat Time: " + next_do_time_list.get(index));
        			
        			time_flow = next_do_time_list.get(index) - last_conn_time;
        			if(time_flow==0.0){
        				last_conn_time = next_do_time_list.get(index);
            			next_do_time_list.remove(index);
        				continue;
        			}
        			System.out.println("flow: " + time_flow);
        			
        			//Device Power Down??
        			for(int i=bs_amount;i<device_amount;i++){
        				power_list.set(i, power_list.get(i)-time_flow/power_max);
        				if(power_list.get(i)<=0.0){
        					device_alive.set(i, false);
        					treatPowerDownEvent(i);
        				}
        			}
        			
        			
        			//beacon publish
        			if(next_do_time_list.get(index)%beacon_pub_time==0.0){
        				pubBeacon();
        			}
        			
        			//Publish Document
        			if(next_do_time_list.get(index) == doc_pub_freq*(pub_doc_count+1)){
        				pub_doc_count++;
        				System.out.println("prepare to pub doc");
        				for(int i=0;i<bs_amount;i++){
        					buffer_list.get(i).add(pub_doc_count);
        				}
        				System.out.println("pub doc ok!!");
        				
        				for(int i=0;i<bs_amount;i++){
        					for(int j=0;j<connect_list.get(i).size();j++){
        						if(device_alive.get(connect_list.get(i).get(j)))
        							checkDocumentSendInDevice(i, connect_list.get(i).get(j));
        					}
        				}
        				
        			}
        			
        			double doc_get_time = next_do_time_list.get(index);
        			last_conn_time = next_do_time_list.get(index);
        			next_do_time_list.remove(index);
        			
        			//Check BS sending OK??
        			for(int i=0;i<bs_sending_to.size();i++){
        				if(!treat_ok_this_round.get(i)){
	        				checkDocumentReceive(i, time_flow, doc_get_time);
	        				checkTagReceive(i, time_flow, doc_get_time);
        				}
    				}
        			
        			//Check mobile2mobile sending OK??
        			for(int i=sending_from.size()-1;i>=0;i--){	
    					int from = sending_from.get(i);
    					if(!treat_ok_this_round.get(from)){
    						int to = sending_to.get(i);
	    					checkDocumentReceive(from, to, time_flow, doc_get_time, i);
	    					checkTagReceive(from, to, time_flow, doc_get_time, i);	
    					}
        			}
        			
        			for(int i=0;i<sending_from.size();i++){
        				System.out.println("sending: ("  + i + ") " + sending_from.get(i) + " " + sending_to.get(i));
        			}
        			if(next_do_time_list.size()>0){
        				Collections.sort(next_do_time_list);
        			}
        			
        			for(int i=0;i<device_amount;i++){
        				treat_ok_this_round.set(i, false);
        			}
        			
        		}
        		
        		
        		String mac[] = event[2].split("<->");
				int mac1 = Integer.parseInt(mac[0].substring(3));
				int mac2 = Integer.parseInt(mac[1].substring(3));
        		
        		//Treat connection change
        		if(event[0].compareTo("Connection")==0 && checkDeviceAlive(mac1, mac2) && (mac1>bs_amount || mac2>bs_amount)){
        			
        			String con = event[1];
        			if(con.compareTo("UP")==0){
        				
        				connect_list.get(mac1).add(mac2);
        				connect_list.get(mac2).add(mac1);
        				
        				boolean doc_trans = false;
        				
        				if(sending_empty_list.get(mac1)&&sending_empty_list.get(mac2))	
	        				if(!checkDocumentSendInDevice(mac1, mac2))
	        					if(!checkDocumentSendInDevice(mac2, mac1))
	        						if(!checkTagSendInDevice(mac1, mac2))
	    	        					checkTagSendInDevice(mac2, mac1);
        				
        			}else if(con.compareTo("DOWN")==0){
        				
        				//System.out.println(mac1 + " down " + mac2);
        				connect_list.get(mac1).remove(connect_list.get(mac1).indexOf(mac2));
        				connect_list.get(mac2).remove(connect_list.get(mac2).indexOf(mac1));
        				
        				boolean doc_drop = false;
        				
        				if(mac1<bs_amount || mac2<bs_amount){	//mobile connect down with bs
        					if(mac1<bs_amount && mac2>=bs_amount && bs_sending_to.get(mac1).contains(mac2)){	//mac1 = bs
        						bs_sending_to.get(mac1).remove(bs_sending_to.get(mac1).indexOf(mac2));
        						sending_empty_list.set(mac1, true);
        						sending_empty_list.set(mac2, true);
        						if(!sending_is_tag.get(mac2)){
	        						device_to_device_sending_document[mac1][mac2] = 0;
	            					device_to_device_sending_document[mac2][mac1] = 0;
        						}else{
        							device_to_device_sending_tags[mac2][mac1] = null;
        							sending_is_tag.set(mac2, false);
        						}
            					device_to_device_sending_document_size[mac1][mac2] = 0.0;
            					device_to_device_sending_document_size[mac2][mac1] = 0.0;
            					
            					doc_drop = true;
        						
        					}else if(mac2<bs_amount && mac1>=bs_amount && bs_sending_to.get(mac2).contains(mac1)){	//mac2 = bs
        						
        						bs_sending_to.get(mac2).remove(bs_sending_to.get(mac2).indexOf(mac1));
        						sending_empty_list.set(mac1, true);
        						sending_empty_list.set(mac2, true);
        						if(!sending_is_tag.get(mac1)){
	        						device_to_device_sending_document[mac1][mac2] = 0;
	            					device_to_device_sending_document[mac2][mac1] = 0;
        						}else{
        							device_to_device_sending_tags[mac1][mac2] = null;
        							sending_is_tag.set(mac1, false);
        						}
            					device_to_device_sending_document_size[mac1][mac2] = 0.0;
            					device_to_device_sending_document_size[mac2][mac1] = 0.0;
            					
            					doc_drop = true;
            					
        					}
        					
        					
        				}else{	//mobile 2 mobile connect down
        				
	        				for(int i=0;i<sending_from.size();i++){
	        					if((sending_from.get(i)==mac1 && sending_to.get(i)==mac2) || (sending_from.get(i)==mac2 && sending_to.get(i)==mac1)){
	        						//System.out.println(receive_count++);
	        						sending_from.remove(i);
	        						sending_to.remove(i);
	        						
	        						sending_empty_list.set(mac1, true);
	        						sending_empty_list.set(mac2, true);
	        						if(sending_is_tag.get(mac1)){
	        							device_to_device_sending_tags[mac1][mac2] = null;
	        							sending_is_tag.set(mac1, false);
	        						}else if(sending_is_tag.get(mac2)){
	        							device_to_device_sending_tags[mac2][mac1] = null;
	        							sending_is_tag.set(mac2, false);
	        						}else{
		        						device_to_device_sending_document[mac1][mac2] = 0;
		            					device_to_device_sending_document[mac2][mac1] = 0;
		        					}
	            					device_to_device_sending_document_size[mac1][mac2] = 0.0;
	            					device_to_device_sending_document_size[mac2][mac1] = 0.0;
	        						
	        						doc_drop = true;
	        						break;
	        					}
	        				}
        				
        				}
        				
        				if(doc_drop){
        					
        					boolean mac1_device_trans = false;
    						boolean mac2_device_trans = false;
        					
        					ArrayList<Integer> mac1_can_sendD = new ArrayList<Integer>();
        					ArrayList<Integer> mac1_can_sendD_copy = new ArrayList<Integer>();
    						for(int j=0;j<connect_list.get(mac1).size();j++){
    							if(sending_empty_list.get(connect_list.get(mac1).get(j))){
    								mac1_can_sendD.add(connect_list.get(mac1).get(j));
    								mac1_can_sendD_copy.add(connect_list.get(mac1).get(j));
    							}
    						}
        					
        					if(mac1_can_sendD.size()>0){
    							int device_choice = -1;
    							for(int j=mac1_can_sendD.size()-1;j>=0;j--){
    								device_choice = mac1_can_sendD.get(ran.nextInt(mac1_can_sendD.size()));
    								mac1_can_sendD.remove(mac1_can_sendD.indexOf(device_choice));
    								if(checkDocumentSendInDevice(mac1, device_choice)){
    									mac1_device_trans = true;
    									break;
    								}
    								if(checkDocumentSendInDevice(device_choice, mac1)){
    									mac1_device_trans = true;
    									break;
    								}
    							}
    						}
    						
    						ArrayList<Integer> mac2_can_sendD = new ArrayList<Integer>();
    						ArrayList<Integer> mac2_can_sendD_copy = new ArrayList<Integer>();
    						for(int j=0;j<connect_list.get(mac2).size();j++){
    							if(sending_empty_list.get(connect_list.get(mac2).get(j))){
    								mac2_can_sendD.add(connect_list.get(mac2).get(j));
    								mac2_can_sendD_copy.add(connect_list.get(mac2).get(j));
    							}
    						}
    						
    						if(mac2_can_sendD.size()>0){
    							int device_choice = -1;
    							for(int j=mac2_can_sendD.size()-1;j>=0;j--){
    								device_choice = mac2_can_sendD.get(ran.nextInt(mac2_can_sendD.size()));
    								mac2_can_sendD.remove(mac2_can_sendD.indexOf(device_choice));
    								if(checkDocumentSendInDevice(mac2, device_choice)){
    									mac2_device_trans = true;
    									break;
    								}
    								if(checkDocumentSendInDevice(device_choice, mac2)){
    									mac2_device_trans = true;
    									break;
    								}
    							}
    						}
    						
    						if(!mac1_device_trans){
    							int device_choice = -1;
    							for(int j=mac1_can_sendD_copy.size()-1;j>=0;j--){
    								device_choice = mac1_can_sendD_copy.get(ran.nextInt(mac1_can_sendD_copy.size()));
    								mac1_can_sendD_copy.remove(mac1_can_sendD_copy.indexOf(device_choice));
    								if(checkTagSendInDevice(mac1, device_choice))
    									break;
    								if(checkTagSendInDevice(device_choice, mac1))
    									break;
    							}
    						}
    						
    						if(!mac2_device_trans){
    							int device_choice = -1;
    							for(int j=mac2_can_sendD_copy.size()-1;j>=0;j--){
    								device_choice = mac2_can_sendD_copy.get(ran.nextInt(mac2_can_sendD_copy.size()));
    								mac2_can_sendD_copy.remove(mac2_can_sendD_copy.indexOf(device_choice));
    								if(checkTagSendInDevice(mac2, device_choice))
    									break;
    								if(checkTagSendInDevice(device_choice, mac2))
    									break;
    							}
    						}
    						
        				}
        				
        			}
        			
        			
        			
        		}
        		last_conn_time = now;
        		
        	}
        	fr.close();
        	br.close();
        	
        	ArrayList<Double> doc_recv_time = new ArrayList<Double>();
        	//simulation done
        	for(int i=0;i<doc_rev_count_time.size();i++){
        		for(int j=0;j<doc_rev_count_time.get(i).size();j++){
        			doc_recv_time.add(doc_rev_count_time.get(i).get(j)-(double)(i+1)*doc_pub_freq);
        		}
        	}
        	
        	System.out.println(doc_recv_time.size() + " " + count5566);
        	System.out.println(tag_recv_count + " " + count555666);
        	
        	Collections.sort(doc_recv_time);
        	FileWriter fw;
			BufferedWriter bufferWritter;
			fw = new FileWriter("C:\\OneEvent\\doc_recv_time.txt", true);
			bufferWritter = new BufferedWriter(fw);
			
			int last_index = 0;
			double last_recv_time = 0.0;
        	
        	for(int i=0;i<doc_recv_time.size()-1;i++){
    			if(doc_recv_time.get(i)==doc_recv_time.get(i+1)){
    				System.out.println("5566");
    				continue;
    			}
        		
    			try{		
    				
    				
    			    bufferWritter.write((i+1) + " " + doc_recv_time.get(i));
    				bufferWritter.newLine();
    				bufferWritter.flush();
    			    //bufferWritter.close();
    				//fw.append(descString);
    				//fw.close();
    			}catch(IOException ioe){
    				//throw ioe;
    			}
        	}
        	
        	try{		
				
				
			    bufferWritter.write(doc_recv_time.size() + " " + doc_recv_time.get(doc_recv_time.size()-1));
				bufferWritter.newLine();
				bufferWritter.flush();
			    bufferWritter.close();
				//fw.append(descString);
				fw.close();
				bufferWritter.close();
			}catch(IOException ioe){
				//throw ioe;
			}
        	
        	
        	
        }catch(Exception e){
        	e.printStackTrace();
        }
		
	}
	
	
	
	private static void checkDocumentReceive(int from, double time_flow, double doc_get_time){	//From Device is BS
		if(from<bs_amount){
			for(int i=bs_sending_to.get(from).size()-1;i>=0;i--){
				int to = bs_sending_to.get(from).get(i);
				if(!sending_is_tag.get(to) && !treat_ok_this_round.get(to)){
					int send_doc = device_to_device_sending_document[from][to];
					device_to_device_sending_document_size[from][to] += mobile_bandwidth * time_flow;
					device_to_device_sending_document_size[to][from] -= mobile_bandwidth * time_flow;
					
					double sending_overflow = device_to_device_sending_document_size[from][to] - doc_size_list.get(send_doc);
					if(sending_overflow>=0){	//document relayed OK!!	have to make tag in mobile
						count5566++;
						doc_rev_count_time.get(send_doc-1).add(doc_get_time);
						device_doc_recv_time.get(to).set(send_doc, doc_get_time - doc_pub_freq * send_doc);
						
						//Document receive & set sending empty
						buffer_list.get(to).add(send_doc);
						sending_empty_list.set(from, true);
						sending_empty_list.set(to, true);
						device_to_device_sending_document[from][to] = 0;
						device_to_device_sending_document[to][from] = 0;
						device_to_device_sending_document_size[from][to] = 0.0;
						device_to_device_sending_document_size[to][from] = 0.0;
						
						bs_sending_to.get(from).remove(i);
						
						System.out.println("Document" + send_doc + " (" + from + ")-->(" + to + ") Receive!!");
						
						//Make tag in device						
						String uuid = UUID.randomUUID().toString();
						Tag tag_temp = new Tag(send_doc, from, to, tag_max_copies, uuid, to);
						Tag tag_temp2 = new Tag(send_doc, from, to, tag_max_copies, uuid, to);
						all_tag_list.add(tag_temp);
						tag_in_device.get(to).addTag(tag_temp2);
						
						
						boolean from_device_trans = false;
						boolean to_device_trans = false;
						
						if(!checkDocumentSendInDevice(from,to)){
							ArrayList<Integer> fromD_can_sendD = new ArrayList<Integer>();
							ArrayList<Integer> fromD_can_sendD_copy = new ArrayList<Integer>();
							for(int j=0;j<connect_list.get(from).size();j++){
								if(sending_empty_list.get(connect_list.get(from).get(j))){
									fromD_can_sendD.add(connect_list.get(from).get(j));
									fromD_can_sendD_copy.add(connect_list.get(from).get(j));
								}
							}
							
							if(fromD_can_sendD.size()>0){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=fromD_can_sendD.size()-1;j>=0;j--){
									device_choice = fromD_can_sendD.get(ran.nextInt(fromD_can_sendD.size()));
									fromD_can_sendD.remove(fromD_can_sendD.indexOf(device_choice));
									if(checkDocumentSendInDevice(from, device_choice)){
										from_device_trans = true;
										break;
									}
								}
							}
							
							ArrayList<Integer> toD_can_sendD = new ArrayList<Integer>();
							ArrayList<Integer> toD_can_sendD_copy = new ArrayList<Integer>();
							for(int j=0;j<connect_list.get(to).size();j++){
								if(sending_empty_list.get(connect_list.get(to).get(j))){
									toD_can_sendD.add(connect_list.get(to).get(j));
									toD_can_sendD_copy.add(connect_list.get(to).get(j));
								}
							}
							
							if(toD_can_sendD.size()>0){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=toD_can_sendD.size()-1;j>=0;j--){
									device_choice = toD_can_sendD.get(ran.nextInt(toD_can_sendD.size()));
									toD_can_sendD.remove(toD_can_sendD.indexOf(device_choice));
									if(checkDocumentSendInDevice(to, device_choice)){
										to_device_trans = true;
										break;
									}
									if(checkDocumentSendInDevice(device_choice, to)){
										to_device_trans = true;
										break;
									}
								}
							}
							
							if(!from_device_trans){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=fromD_can_sendD_copy.size()-1;j>=0;j--){
									device_choice = fromD_can_sendD_copy.get(ran.nextInt(fromD_can_sendD_copy.size()));
									fromD_can_sendD_copy.remove(fromD_can_sendD_copy.indexOf(device_choice));
									if(checkTagSendInDevice(device_choice, from))
										break;
								}
							}
							
							if(!to_device_trans){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=toD_can_sendD_copy.size()-1;j>=0;j--){
									device_choice = toD_can_sendD_copy.get(ran.nextInt(toD_can_sendD_copy.size()));
									toD_can_sendD_copy.remove(toD_can_sendD_copy.indexOf(device_choice));
									if(checkTagSendInDevice(to, device_choice))
										break;
									if(checkTagSendInDevice(device_choice, to))
										break;
								}
							}
						}
					}
				}
			}
		}else{
			System.out.println("error");
		}
	}
	
	private static boolean checkDocumentReceive(int from, int to, double time_flow, double doc_get_time, int index){	//From Device is mobile
		if(from>=bs_amount && !sending_is_tag.get(from) && !treat_ok_this_round.get(from)){
			int send_doc = device_to_device_sending_document[from][to];
			device_to_device_sending_document_size[from][to] += mobile_bandwidth * time_flow;
			device_to_device_sending_document_size[to][from] -= mobile_bandwidth * time_flow;
			
			double sending_overflow = device_to_device_sending_document_size[from][to] - doc_size_list.get(send_doc);
			if(sending_overflow>=0){	//document relayed OK!!	have to make tag in mobile
				count5566++;
				System.out.println(from + " " + to + " " + send_doc);
				doc_rev_count_time.get(send_doc-1).add(doc_get_time);
				
				//Document receive & set sending empty
				buffer_list.get(to).add(send_doc);
				sending_empty_list.set(from, true);
				sending_empty_list.set(to, true);
				device_to_device_sending_document[from][to] = 0;
				device_to_device_sending_document[to][from] = 0;
				device_to_device_sending_document_size[from][to] = 0.0;
				device_to_device_sending_document_size[to][from] = 0.0;
				
				System.out.println("Document" + send_doc + " (" + from + ")-->(" + to + ") Receive!!");
				
				//Make tag in device						
				String uuid = UUID.randomUUID().toString();
				Tag tag_temp = new Tag(send_doc, from, to, tag_max_copies, uuid, to);
				Tag tag_temp2 = new Tag(send_doc, from, to, tag_max_copies, uuid, to);
				all_tag_list.add(tag_temp);
				tag_in_device.get(to).addTag(tag_temp2);
				
				sending_from.remove(index);
				sending_to.remove(index);
				
				boolean from_device_trans = false;
				boolean to_device_trans = false;
				
				if(!checkDocumentSendInDevice(from,to)){
					if(!checkDocumentSendInDevice(to,from)){
						ArrayList<Integer> fromD_can_sendD = new ArrayList<Integer>();
						ArrayList<Integer> fromD_can_sendD_copy = new ArrayList<Integer>();
						for(int j=0;j<connect_list.get(from).size();j++){
							if(sending_empty_list.get(connect_list.get(from).get(j))){
								fromD_can_sendD.add(connect_list.get(from).get(j));
								fromD_can_sendD_copy.add(connect_list.get(from).get(j));
							}
						}
						
						if(fromD_can_sendD.size()>0){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=fromD_can_sendD.size()-1;j>=0;j--){
								device_choice = fromD_can_sendD.get(ran.nextInt(fromD_can_sendD.size()));
								fromD_can_sendD.remove(fromD_can_sendD.indexOf(device_choice));
								if(checkDocumentSendInDevice(from, device_choice)){
									from_device_trans = true;
									break;
								}
								if(checkDocumentSendInDevice(device_choice, from)){
									from_device_trans = true;
									break;
								}	
							}
						}
						
						ArrayList<Integer> toD_can_sendD = new ArrayList<Integer>();
						ArrayList<Integer> toD_can_sendD_copy = new ArrayList<Integer>();
						for(int j=0;j<connect_list.get(to).size();j++){
							if(sending_empty_list.get(connect_list.get(to).get(j))){
								toD_can_sendD.add(connect_list.get(to).get(j));
								toD_can_sendD_copy.add(connect_list.get(to).get(j));
							}
						}
						
						if(toD_can_sendD.size()>0){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=toD_can_sendD.size()-1;j>=0;j--){
								device_choice = toD_can_sendD.get(ran.nextInt(toD_can_sendD.size()));
								toD_can_sendD.remove(toD_can_sendD.indexOf(device_choice));
								if(checkDocumentSendInDevice(to, device_choice)){
									to_device_trans = true;
									break;
								}
								if(checkDocumentSendInDevice(device_choice, to)){
									to_device_trans = true;
									break;
								}
							}
						}
						
						if(!from_device_trans){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=fromD_can_sendD_copy.size()-1;j>=0;j--){
								device_choice = fromD_can_sendD_copy.get(ran.nextInt(fromD_can_sendD_copy.size()));
								fromD_can_sendD_copy.remove(fromD_can_sendD_copy.indexOf(device_choice));
								if(checkTagSendInDevice(from, device_choice))
									break;
								if(checkTagSendInDevice(device_choice, from))
									break;
							}
						}
						
						if(!to_device_trans){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=toD_can_sendD_copy.size()-1;j>=0;j--){
								device_choice = toD_can_sendD_copy.get(ran.nextInt(toD_can_sendD_copy.size()));
								toD_can_sendD_copy.remove(toD_can_sendD_copy.indexOf(device_choice));
								if(checkTagSendInDevice(to, device_choice))
									break;
								if(checkTagSendInDevice(device_choice, to))
									break;
							}
						}
					}
				}
				
				return true;
				
			}
			
		}else{
			System.out.println("error");
		}
		
		return false;
	}
	
	
	
	private static void checkTagReceive(int to, double time_flow, double tag_get_time){	//To Device is BS
		if(to<bs_amount){
			for(int i=bs_sending_to.get(to).size()-1;i>=0;i--){
				int from = bs_sending_to.get(to).get(i);
				if(sending_is_tag.get(from) && !treat_ok_this_round.get(from)){
					Tag sending_tag = device_to_device_sending_tags[from][to];
					device_to_device_sending_document_size[from][to] += mobile_bandwidth * time_flow;
					device_to_device_sending_document_size[to][from] -= mobile_bandwidth * time_flow;
					
					double sending_overflow = device_to_device_sending_document_size[from][to] - tag_size;
					System.out.println(sending_tag.getDocNum() + " " + sending_tag.getTagsFrom() + " " + sending_tag.getTagsTo());
					if(sending_overflow>=0){	//tag relayed OK!!
						if(!tag_recv_in_bs[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()]){
							tag_rev_count_time.add(tag_get_time);
							tag_recv_in_bs[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = true;
							tag_recv_count++;
						}
						
						//Tag receive & set sending empty
						if(power_list.get(to)>=tag_threshold_power){
							sending_tag.setWhenReceive(1, sending_tag.getCopyIndex());
						}else{
							sending_tag.setWhenReceive(sending_tag.getCopyIndex(), 1);
						}
						//tag_in_device[to][sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = sending_tag;
						//tag_in_device[to].setTag(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), sending_tag);
						//tag_in_device_bool[to][sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = true;
						//tag_in_device[to].setBoolean(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), true);
						
						device_num_in_tag[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()].addDeviceNum(to);
						tag_in_device.get(to).addTag(sending_tag);
								
						sending_empty_list.set(from, true);
						sending_empty_list.set(to, true);
						sending_is_tag.set(from, false);
						device_to_device_sending_document_size[from][to] = 0.0;
						device_to_device_sending_document_size[to][from] = 0.0;
						device_to_device_sending_tags[from][to] = null;
						
						bs_sending_to.get(to).remove(i);
						
						//Remove tag in Sending Device
						//tag_in_device[from][sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = null;
						//tag_in_device_bool[from][sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = false;
						//tag_in_device[from].setTag(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), null);
						//tag_in_device[from].setBoolean(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), false);
						
						device_num_in_tag[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()].removeDeviceNum(from);
						tag_in_device.get(from).removeTag(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo());
						
						System.out.println("Tag(Doc" + sending_tag.getDocNum() + ",D" + sending_tag.getTagsFrom() + "->D" + sending_tag.getTagsTo() + ") (" + from + ")-->(" + to + ") Receive!!");
						
						boolean from_device_trans = false;
						boolean to_device_trans = false;
						
						if(!checkDocumentSendInDevice(to,from)){
							ArrayList<Integer> toD_can_sendD = new ArrayList<Integer>();
							ArrayList<Integer> toD_can_sendD_copy = new ArrayList<Integer>();
							for(int j=0;j<connect_list.get(to).size();j++){
								if(sending_empty_list.get(connect_list.get(to).get(j))){
									toD_can_sendD.add(connect_list.get(to).get(j));
									toD_can_sendD_copy.add(connect_list.get(to).get(j));
								}
							}
							
							if(toD_can_sendD.size()>0){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=toD_can_sendD.size()-1;j>=0;j--){
									device_choice = toD_can_sendD.get(ran.nextInt(toD_can_sendD.size()));
									toD_can_sendD.remove(toD_can_sendD.indexOf(device_choice));
									if(checkDocumentSendInDevice(to, device_choice)){
										to_device_trans = true;
										break;
									}
								}
							}
							
							ArrayList<Integer> fromD_can_sendD = new ArrayList<Integer>();
							ArrayList<Integer> fromD_can_sendD_copy = new ArrayList<Integer>();
							for(int j=0;j<connect_list.get(from).size();j++){
								if(sending_empty_list.get(connect_list.get(from).get(j))){
									fromD_can_sendD.add(connect_list.get(from).get(j));
									fromD_can_sendD_copy.add(connect_list.get(from).get(j));
								}
							}
							
							
							
							if(fromD_can_sendD.size()>0){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=fromD_can_sendD.size()-1;j>=0;j--){
									device_choice = fromD_can_sendD.get(ran.nextInt(fromD_can_sendD.size()));
									fromD_can_sendD.remove(fromD_can_sendD.indexOf(device_choice));
									if(checkDocumentSendInDevice(from, device_choice)){
										from_device_trans = true;
										break;
									}
									if(checkDocumentSendInDevice(device_choice, from)){
										from_device_trans = true;
										break;
									}
								}
							}
							
							if(!to_device_trans){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=toD_can_sendD_copy.size()-1;j>=0;j--){
									device_choice = toD_can_sendD_copy.get(ran.nextInt(toD_can_sendD_copy.size()));
									toD_can_sendD_copy.remove(toD_can_sendD_copy.indexOf(device_choice));
									if(checkTagSendInDevice(device_choice, to))
										break;
								}
							}
							
							if(!from_device_trans){
								int device_choice = -1;
								Random ran = new Random();
								for(int j=fromD_can_sendD_copy.size()-1;j>=0;j--){
									device_choice = fromD_can_sendD_copy.get(ran.nextInt(fromD_can_sendD_copy.size()));
									fromD_can_sendD_copy.remove(fromD_can_sendD_copy.indexOf(device_choice));
									if(checkTagSendInDevice(from, device_choice))
										break;
									if(checkTagSendInDevice(device_choice, from))
										break;
								}
							}
						}
					}
				}
			}
		}else{
			//System.out.println(to + " error");
		}
	}
	
	private static boolean checkTagReceive(int from, int to, double time_flow, double tag_get_time, int index){	//To Device is mobile
		if(from>=bs_amount && sending_is_tag.get(from) && !treat_ok_this_round.get(from)){
			Tag sending_tag = device_to_device_sending_tags[from][to];
			device_to_device_sending_document_size[from][to] += mobile_bandwidth * time_flow;
			device_to_device_sending_document_size[to][from] -= mobile_bandwidth * time_flow;
			
			double sending_overflow = device_to_device_sending_document_size[from][to] - tag_size;
			if(sending_overflow>=0){	//tag relayed OK!!
				System.out.println(from + " " + to + " tag");
				System.out.println(sending_tag.getDocNum() + " " + sending_tag.getTagsFrom() + " " + sending_tag.getTagsTo());
				/*if(!tag_recv_in_bs[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()]){
					tag_rev_count_time.add(tag_get_time);
					tag_recv_in_bs[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = true;
					tag_recv_count++;
				}*/
				
				//Tag receive & set sending empty
				//tag_in_device[to][sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = sending_tag;
				//tag_in_device_bool[to][sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()] = true;
				//tag_in_device[to].setTag(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), sending_tag);
				//tag_in_device[to].setBoolean(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), true);
				device_num_in_tag[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()].addDeviceNum(to);
				tag_in_device.get(to).addTag(sending_tag);

				
				sending_empty_list.set(from, true);
				sending_empty_list.set(to, true);
				sending_is_tag.set(from, false);
				device_to_device_sending_document_size[from][to] = 0.0;
				device_to_device_sending_document_size[to][from] = 0.0;
				device_to_device_sending_tags[from][to] = null;
				
				//tag_in_device[from].setTag(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), null);
				//tag_in_device[from].setBoolean(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo(), false);
				device_num_in_tag[sending_tag.getDocNum()][sending_tag.getTagsFrom()][sending_tag.getTagsTo()].removeDeviceNum(from);
				tag_in_device.get(from).removeTag(sending_tag.getDocNum(), sending_tag.getTagsFrom(), sending_tag.getTagsTo());
				
				sending_from.remove(index);
				sending_to.remove(index);
				
				System.out.println("Tag(Doc" + sending_tag.getDocNum() + ",D" + sending_tag.getTagsFrom() + "->D" + sending_tag.getTagsTo() + ") (" + from + ")-->(" + to + ") Receive!!");
				
				boolean from_device_trans = false;
				boolean to_device_trans = false;
				
				if(!checkDocumentSendInDevice(from,to)){
					if(!checkDocumentSendInDevice(to,from)){
						ArrayList<Integer> fromD_can_sendD = new ArrayList<Integer>();
						ArrayList<Integer> fromD_can_sendD_copy = new ArrayList<Integer>();
						for(int j=0;j<connect_list.get(from).size();j++){
							if(sending_empty_list.get(connect_list.get(from).get(j))){
								fromD_can_sendD.add(connect_list.get(from).get(j));
								fromD_can_sendD_copy.add(connect_list.get(from).get(j));
							}
						}
						
						if(fromD_can_sendD.size()>0){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=fromD_can_sendD.size()-1;j>=0;j--){
								device_choice = fromD_can_sendD.get(ran.nextInt(fromD_can_sendD.size()));
								fromD_can_sendD.remove(fromD_can_sendD.indexOf(device_choice));
								if(checkDocumentSendInDevice(from, device_choice)){
									from_device_trans = true;
									break;
								}
								if(checkDocumentSendInDevice(device_choice, from)){
									from_device_trans = true;
									break;
								}	
							}
						}
						
						ArrayList<Integer> toD_can_sendD = new ArrayList<Integer>();
						ArrayList<Integer> toD_can_sendD_copy = new ArrayList<Integer>();
						for(int j=0;j<connect_list.get(to).size();j++){
							if(sending_empty_list.get(connect_list.get(to).get(j))){
								toD_can_sendD.add(connect_list.get(to).get(j));
								toD_can_sendD_copy.add(connect_list.get(to).get(j));
							}
						}
						
						if(toD_can_sendD.size()>0){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=toD_can_sendD.size()-1;j>=0;j--){
								device_choice = toD_can_sendD.get(ran.nextInt(toD_can_sendD.size()));
								toD_can_sendD.remove(toD_can_sendD.indexOf(device_choice));
								if(checkDocumentSendInDevice(to, device_choice)){
									to_device_trans = true;
									break;
								}
								if(checkDocumentSendInDevice(device_choice, to)){
									to_device_trans = true;
									break;
								}
							}
						}
						
						if(!from_device_trans){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=fromD_can_sendD_copy.size()-1;j>=0;j--){
								device_choice = fromD_can_sendD_copy.get(ran.nextInt(fromD_can_sendD_copy.size()));
								fromD_can_sendD_copy.remove(fromD_can_sendD_copy.indexOf(device_choice));
								if(checkTagSendInDevice(from, device_choice))
									break;
								if(checkTagSendInDevice(device_choice, from))
									break;
							}
						}
						
						if(!to_device_trans){
							int device_choice = -1;
							Random ran = new Random();
							for(int j=toD_can_sendD_copy.size()-1;j>=0;j--){
								device_choice = toD_can_sendD_copy.get(ran.nextInt(toD_can_sendD_copy.size()));
								toD_can_sendD_copy.remove(toD_can_sendD_copy.indexOf(device_choice));
								if(checkTagSendInDevice(to, device_choice))
									break;
								if(checkTagSendInDevice(device_choice, to))
									break;
							}
						}
					}
				}
				
				return true;
				
			}
			
		}else{
			//System.out.println(from + " " + to + " error");
		}
		
		return false;
	}
	
	
	
	
	
	//Check Device "from" & "to" can transmit Document??
	private static boolean checkDocumentSendInDevice(int from, int to){
		ArrayList<Integer> doc_can_resend = new ArrayList<Integer>();
		for(int k=0;k<buffer_list.get(from).size();k++){
			if(!buffer_list.get(to).contains(buffer_list.get(from).get(k))){
				doc_can_resend.add(buffer_list.get(from).get(k));
			}
		}
		
		int doc_choice = -1;
		
		if(doc_can_resend.size()>0 && sending_empty_list.get(from) && sending_empty_list.get(to)){
			
			if(from<bs_amount){
				doc_choice = doc_can_resend.get(0);
				
				device_to_device_sending_document[from][to] = doc_choice;
				device_to_device_sending_document[to][from] = 0 - doc_choice;
				
				bs_sending_to.get(from).add(to);
				if(bs_sending_to.get(from).size()==bs_transmit_max)
					sending_empty_list.set(from, false);
				sending_empty_list.set(to, false);
				next_do_time_list.add(now + doc_size_list.get(doc_choice)/mobile_bandwidth);
				Collections.sort(next_do_time_list);
				System.out.println("Start Doc send from (" + from + ") --> (" + to + ") is Document" + doc_choice + " Time: " + doc_size_list.get(doc_choice)/mobile_bandwidth);
				treat_ok_this_round.set(from, true);
				return true;
			}else{
				doc_choice = doc_can_resend.get(0);
				device_to_device_sending_document[from][to] = doc_choice;
				device_to_device_sending_document[to][from] = 0 - doc_choice;
				sending_empty_list.set(from, false);
				sending_empty_list.set(to, false);
				sending_from.add(from);
				sending_to.add(to);
				next_do_time_list.add(now + doc_size_list.get(doc_choice)/mobile_bandwidth);
				Collections.sort(next_do_time_list);
				System.out.println("Start Doc send from (" + from + ") --> (" + to + ") is Document" + doc_choice + " Time: " + doc_size_list.get(doc_choice)/mobile_bandwidth);
				treat_ok_this_round.set(from, true);
				return true;
			}		
			
		}
		return false;
	}
	
	
	//Check Device "from" & "to" can transmit Tag??
	private static boolean checkTagSendInDevice(int from, int to){
		
		if(from<bs_amount)
			return false;
		
		Random ran = new Random();
		
		ArrayList<Triple> tag_can_send = new ArrayList<Triple>();
		
		String uuid = null;
		
		for(int i=0;i<tag_in_device.get(from).getTagAmount();i++){
			Tag tag_temp = tag_in_device.get(from).getTagByIndex(i);
			if(tag_temp.checkTagHaveToSend(to))
				
			uuid = tag_temp.getId();
			int device_num_in_tag_index = tag_in_device.get(from).getTagByIndex(i).getDeviceNumInTagIndex();
			if(!device_num_in_tag.get(device_num_in_tag_index).checkDeviceNum(to))
				tag_can_send.add(tri);
			
			if(!device_num_in_tag[tri.getDoc()][tri.getFrom()][tri.getTo()].checkDeviceNum(to)/* && tag_in_device.get(from).getTag(tri.getDoc(), tri.getFrom(), tri.getTo()).checkSend()*/)
				tag_can_send.add(tri);
		}
		
		/*for(int i=1;i<doc_pub_amount+1;i++){
			for(int j=0;j<device_amount;j++){
				for(int k=0;k<device_amount;k++){
					if(tag_in_device[from].getBoolean(i,j,k))
						System.out.println(tag_in_device[from].getBoolean(i,j,k) + " " + tag_in_device[to].getBoolean(i,j,k));
					if(tag_in_device[from].getBoolean(i,j,k) && !tag_in_device[to].getBoolean(i,j,k))
						tag_can_send.add(new Triple(i, j, k));
				}
			}
		}*/
		
		Triple triple_choice = new Triple(-1,-1, -1);
		
		if(tag_can_send.size()>0 && sending_empty_list.get(from) && sending_empty_list.get(to)){
			
			if(to<bs_amount){	//BS receive Tag
				int index = -1;
				while(tag_can_send.size()>0){
					index = ran.nextInt(tag_can_send.size());
					triple_choice = tag_can_send.get(index);
					tag_can_send.remove(index);
					Tag tag_choice = /*tag_in_device[from][triple_choice.getDoc()][triple_choice.getFrom()][triple_choice.getTo()];*/
										/*tag_in_device[from].getTag(triple_choice.getDoc(), triple_choice.getFrom(), triple_choice.getTo());*/
										tag_in_device.get(from).getTag(triple_choice.getDoc(), triple_choice.getFrom(), triple_choice.getTo());
					device_to_device_sending_tags[from][to] = tag_choice;
					sending_empty_list.set(from, false);
					bs_sending_to.get(to).add(from);
					sending_is_tag.set(from, true);
					if(bs_sending_to.get(to).size()==bs_transmit_max)
						sending_empty_list.set(to, false);
					next_do_time_list.add(now + tag_size/mobile_bandwidth);
					Collections.sort(next_do_time_list);
					System.out.println("Start Tag send from (" + from + ") --> (" + to + ") is Tag(" + triple_choice.getDoc() + "," + triple_choice.getFrom() + "," + triple_choice.getTo() + ") Time: " + tag_size/mobile_bandwidth);
					treat_ok_this_round.set(from, true);
					return true;
				}
			}else{
				int index = -1;
				double threshold = 0.0;
				double d = 0.0;
				while(tag_can_send.size()>0){
					index = ran.nextInt(tag_can_send.size());
					triple_choice = tag_can_send.get(index);
					tag_can_send.remove(index);
					Tag tag_choice = /*tag_in_device[from][triple_choice.getDoc()][triple_choice.getFrom()][triple_choice.getTo()];*/
										/*tag_in_device[from].getTag(triple_choice.getDoc(), triple_choice.getFrom(), triple_choice.getTo());*/
										tag_in_device.get(from).getTag(triple_choice.getDoc(), triple_choice.getFrom(), triple_choice.getTo());
					while(tag_choice == null){
						System.out.print("tag_in_device" + " error");
					}
					if(!(power_list.get(from)>tag_threshold_power && !tag_choice.checkSend())){
						double b = 0.0;
						if(tag_choice.getTagHops()==0)
							b=1.0;
						else
							b = Math.min(tag_choice.getDocHops()/tag_choice.getTagHops(), 1.0);
						threshold = alpha*power_list.get(from) + 
								   beta* b + 
								   gamma*Math.min(tag_choice.getDocTransTime()/tag_choice.getTagTransTime(), 1.0);
						/*System.out.println(alpha + "*" + power_list.get(from) + "*" + 
								   beta + "*" + b + "*" +  
								   gamma + "*" + Math.min(tag_choice.getDocTransTime()/tag_choice.getTagTransTime(), 1.0));*/
						System.out.println("threshold:" + threshold);
						d = ran.nextDouble();
						if(d>=threshold){
							device_to_device_sending_tags[from][to] = tag_choice;
							sending_empty_list.set(from, false);
							sending_empty_list.set(to, false);
							sending_from.add(from);
							sending_to.add(to);
							sending_is_tag.set(from, true);
							next_do_time_list.add(now + tag_size/mobile_bandwidth);
							Collections.sort(next_do_time_list);
							System.out.println("Start Tag send from (" + from + ") --> (" + to + ") is Tag(" + triple_choice.getDoc() + "," + triple_choice.getFrom() + "," + triple_choice.getTo() + ") Time: " + tag_size/mobile_bandwidth);
							treat_ok_this_round.set(from, true);
							return true;
						}
					}
				}
			}		
			
		}
		return false;
	}
	
	private static boolean cheakDeviceAlive(int mac){
		return device_alive.get(mac);
	}
	
	private static boolean checkDeviceAlive(int mac1, int mac2){
		return device_alive.get(mac1)&&device_alive.get(mac2);
	}
	
	private static void treatPowerDownEvent(int mac){
		
		Random ran = new Random();
		
		//Remove All Connection on "mac"
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		connect_list.set(mac, tmp);
		HashSet<Integer> set = new HashSet();
		set.add(mac);
		//ArrayList<Integer> connect_down_list = new ArrayList<Integer>();
		for(int i=0;i<connect_list.size();i++){
			connect_list.get(i).removeAll(set);
			//connect_down_list.add(i);
		}
		
		if(mac<bs_amount){	//mac:bs mac2:mobile
			for(int i=bs_sending_to.size()-1;i>=0;i--){
				int mac2 = bs_sending_to.get(mac).get(i);
				bs_sending_to.get(mac).remove(i);
				sending_empty_list.set(mac2, true);
				if(!sending_is_tag.get(mac2)){
					device_to_device_sending_document[mac][mac2] = 0;
					device_to_device_sending_document[mac2][mac] = 0;
				}else{
					device_to_device_sending_tags[mac2][mac] = null;
				}
				device_to_device_sending_document_size[mac][mac2] = 0.0;
				device_to_device_sending_document_size[mac2][mac] = 0.0;
				
				sending_is_tag.set(mac2, false);
				
			}			
			
		}
		
		else{	//mac:mobile mac2:bs or mobile
			int index_from = sending_from.indexOf(mac);
			int index_to = sending_to.indexOf(mac);
			int mac2 = -1;
			int trans_direction = 0;	//0:no trans	1:mac->mac2		-1:mac2->mac
			if(index_from>=0 && index_to>=0){
				while(true)
					System.out.println("error");
			}
			if(index_from>=0){
				mac2 = sending_to.get(index_from);
				trans_direction = 1;
				sending_from.remove(index_from);
				sending_to.remove(index_from);
			}
			if(index_to>=0){
				mac2 = sending_from.get(index_to);
				trans_direction = -1;
				sending_from.remove(index_to);
				sending_to.remove(index_to);
			}
			if(trans_direction != 0){
				sending_empty_list.set(mac2, true);
				if(!sending_is_tag.get(mac) && !sending_is_tag.get(mac2)){
					device_to_device_sending_document[mac][mac2] = 0;
					device_to_device_sending_document[mac2][mac] = 0;
				}else{
					device_to_device_sending_tags[mac2][mac] = null;
				}
				
				device_to_device_sending_document_size[mac][mac2] = 0.0;
				device_to_device_sending_document_size[mac2][mac] = 0.0;
				
				sending_is_tag.set(mac2, false);
				
				ArrayList<Integer> mac2_can_sendD = new ArrayList<Integer>();
				ArrayList<Integer> mac2_can_sendD_copy = new ArrayList<Integer>();
				for(int j=0;j<connect_list.get(mac2).size();j++){
					if(sending_empty_list.get(connect_list.get(mac2).get(j))){
						mac2_can_sendD.add(connect_list.get(mac2).get(j));
					}
				}
				
				boolean mac2_device_trans = false;
				
				if(mac2_can_sendD.size()>0){
					int device_choice = -1;
					for(int j=mac2_can_sendD.size()-1;j>=0;j--){
						device_choice = mac2_can_sendD.get(ran.nextInt(mac2_can_sendD.size()));
						mac2_can_sendD.remove(mac2_can_sendD.indexOf(device_choice));
						if(checkDocumentSendInDevice(mac2, device_choice)){
							mac2_device_trans = true;
							break;
						}
						if(checkDocumentSendInDevice(device_choice, mac2)){
							mac2_device_trans = true;
							break;
						}
					}
					
					
				}
				
				if(!mac2_device_trans){
					int device_choice = -1;
					for(int j=mac2_can_sendD_copy.size()-1;j>=0;j--){
						device_choice = mac2_can_sendD_copy.get(ran.nextInt(mac2_can_sendD_copy.size()));
						mac2_can_sendD_copy.remove(mac2_can_sendD_copy.indexOf(device_choice));
						if(checkTagSendInDevice(mac2, device_choice))
							break;
						if(checkTagSendInDevice(device_choice, mac2))
							break;
					}
				}
				
			}
			
		}
	
	}
	
	private boolean checkSameTriple(Triple t1, Triple t2){
		return t1.getDoc()==t2.getDoc() && t1.getFrom()==t2.getFrom() && t1.getTo()==t2.getTo();
	}
	
	private static void recvBeacon(int mac){
		if(power_list.get(mac)>tag_threshold_power){
			//will add algo.
		}else{
			//will add algo.
		}
	}
	
	private static void pubBeacon(){
		
		for(int i=0;i<bs_amount;i++){
			for(int j=0;j<connect_list.get(i).size();j++){
				int mac = connect_list.get(i).get(j);
				if(mac>bs_amount){
					aggreAndSendTag(mac);
				}
			}
		}
		
	}
	
	private static void aggreAndSendTag(int mac){
		
		double will_combine_tag_size = 0;
		int from_index = 0;
		int to_index = 0;
		for(int i=0;i<tag_list.get(mac).size();i++){
			will_combine_tag_size += tag_list.get(mac).get(i).getSize();
			if(will_combine_tag_size>tag_max_size && to_index>from_index){
				aggreTag(tag_list.get(mac), from_index, to_index);
				from_index = i+1;
				to_index = i+1;
				will_combine_tag_size = tag_list.get(mac).get(i).getSize();
			}else{
				to_index = i;
			}
		}
		
		if(to_index > from_index){
			aggreTag(tag_list.get(mac), from_index, to_index);
		}
		
	}
	
	private static void aggreTag(ArrayList<Tag> list, int from, int to){
		
		String uuid = UUID.randomUUID().toString();
		Tag tmp = new Tag(uuid);
		
		for(int i=from;i<=to;i++){
			tmp.aggreTag(list.get(i));
		}
		
	}
	
	
}
