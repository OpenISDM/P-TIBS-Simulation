/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import core.Connection;
import core.ConnectionListener;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Event log panel where log entries are displayed.
 */
public class EventLogPanel extends JPanel 
	implements ConnectionListener, MessageListener, ActionListener {

	private static final String PANEL_TITLE = "Event log";
	/** format of a single log entry */
	private static final String ENTRY_FORMAT = "% 9.1f: %s "; 
	private static final int FONT_SIZE = 12;
	private static final String FONT_TYPE = "monospaced";
	private static final Color LOG_BUTTON_BG = Color.WHITE;
	private static final String HOST_DELIM = "<->";
	private static final Color HIGHLIGHT_BG_COLOR = Color.GREEN;
	

	// constants used for button property  
	private static final String HOST_PROP = "host";
	private static final String MSG_PROP = "message";

	/** How often the log is updated (milliseconds) */
	public static final int LOG_UP_INTERVAL = 500;
	
	/** Regular expression to filter log entries (changed trough Settings) */ 
	private String regExp = null;
	/** how many events to show in log (changed trough Settings) */
	private int maxNrofEvents = 0;
	
	private Font font;	// font used in log entries
	private DTNSimGUI gui;
	private Vector<JPanel> eventPanes;
	private GridLayout layout;
	
	private EventLogControlPanel controls;
	private EventLogControl conUpCheck;
	private EventLogControl conDownCheck;
	private EventLogControl msgCreateCheck;
	private EventLogControl msgTransferStartCheck;
	private EventLogControl msgRelayCheck;
	private EventLogControl msgRemoveCheck;
	private EventLogControl msgDeliveredCheck;
	private EventLogControl msgDropCheck;
	private EventLogControl msgAbortCheck;
	
	private static long last_timestamp = -1;
		
	//New Add To Make Tag
	private static final int doc_amount = 1;
	private static final int bs_amount = 10;
	private static final int domain_amount = 3;
	private static final int device_amount_per_domain = 40;
	private static final int sub_device_per_domain = device_amount_per_domain*domain_amount;
	
	private static long start_time = System.currentTimeMillis();
	
	private static ArrayList<ArrayList<String>> had_doc_list = new ArrayList<ArrayList<String>>();
	private static ArrayList<ArrayList<Integer>> sub_doc_list = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<Integer> sub_doc_get_count = new ArrayList<Integer>();
	private static ArrayList<Integer> sub_doc_get_count_after10 = new ArrayList<Integer>();
	private static ArrayList<Double> pa90_time = new ArrayList<Double>();
	
	private static ArrayList<ArrayList<ArrayList<Integer>>> sub_doc_list2 = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private static ArrayList<ArrayList<Integer>> sub_doc_get_count2 = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<ArrayList<Integer>> sub_doc_get_count_after102 = new ArrayList<ArrayList<Integer>>();
	
	private static ArrayList<String> doc_senLv = new ArrayList<String>();
	private static ArrayList<String> doc_version = new ArrayList<String>();
	private static ArrayList<String> doc_domain = new ArrayList<String>();
	private static ArrayList<Integer> doc_author = new ArrayList<Integer>();
	private static ArrayList<String> doc_uuid = new ArrayList<String>();
	
	private static ArrayList<String> person_idType = new ArrayList<String>();
	private static ArrayList<String> person_id = new ArrayList<String>();
	
	private static ArrayList<String> device_idType = new ArrayList<String>();
	private static ArrayList<String> device_id = new ArrayList<String>();
	
	private static ArrayList<ArrayList<Integer>> device_doc_buffer = new ArrayList<ArrayList<Integer>>();
	
	static String url = "jdbc:virtuoso://localhost:1111/";
	
	private static boolean test_over = false;
	
	private static boolean trans_over = false;
	
	//Transmit flow information
	private static final long size_per_file = 512000; //100KB
	private static long file_flow = 0;
	private static long file_flow_recent = 0;
	private static long tag_flow = 0;
	private static long tag_flow_recent = 0;
	private static long recent_time = 0;
	private static long tag_make_size = 0;
	private static ArrayList<ArrayList<String>> tag_in_device = new ArrayList<ArrayList<String>>();
	private static ArrayList<ArrayList<Integer>> tag_send_count = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<String> tag_in_bs = new ArrayList<String>();
	private static ArrayList<String> tag_not_in_bs = new ArrayList<String>();
	
	private static int pub_doc_count = 0;
	
	private static int excel_index = 1;
	
	private static int tag_count = 0;
	
	private static int tag_recv_count = 0;
	
	private static double doc_all_recv_time = 0;
	
	private static boolean set_recv_time = false;
	
	private static int doc_get_count = 0;
	
	private static int pa90_set_count = 0;
	
	private static VirtGraph set = null;
	
	/**
	 * Creates a new log panel
	 * @param gui The where this log belongs to (for callbacks) 
	 */
	public EventLogPanel(DTNSimGUI gui) {
		
		deleteAll(new File("c:\\simulation_tag"));
		
		File ff = new File("C:\\simulation_tag");
		if(!ff.exists()){   
			ff.mkdir();    
		}
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		for(int i=0;i<bs_amount+device_amount_per_domain*domain_amount;i++){
			ArrayList<String> tmp = new ArrayList<String>();
			tag_in_device.add(tmp);	
			ArrayList<Integer> i_tmp = new ArrayList<Integer>();
			tag_send_count.add(i_tmp);
		}
		
		//Set Document Subscribe
		int sub_index =0;
		for(int i=0;i<doc_amount;i++){
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for(int j=0;j<sub_device_per_domain;j++){
				tmp.add(bs_amount + sub_index%(device_amount_per_domain*domain_amount));
				sub_index++;
			}
			sub_doc_list.add(tmp);
			sub_doc_get_count.add(0);
			pa90_time.add(0.0);
		}
		
		System.out.println("Prepare All information...");
		
		//Set tag file
		for(int i=0;i<bs_amount;i++){
			
			File tagDoc = new File("C:\\simulation_tag\\BS_" + i);
			if(!tagDoc.exists()){   
				tagDoc.mkdir();    
			}
			
		}	
		for(int i=0;i<domain_amount;i++){
			for(int j=0;j<device_amount_per_domain;j++){
				
				File tagDoc = new File("C:\\simulation_tag\\D" + (i+1) + "_" + (bs_amount+device_amount_per_domain*i+j));
				if(!tagDoc.exists()){   
					tagDoc.mkdir();    
				}
				
			}
		}
		
		Random ran = new Random();
		
		//===== Set Document Information =====
		for(int i=0;i<doc_amount;i++){
			int r= ran.nextInt(3);
			switch(r){
			
			case 0:
				doc_senLv.add("PRIVATE");
				break;
			case 1:
				doc_senLv.add("PUBLIC");
				break;
			case 2:
				doc_senLv.add("SENSITIVE");
				break;
			
			}
		}
		
		for(int i=0;i<doc_amount;i++){
			
			int r= ran.nextInt(90)+10;
			double x = ((double)r)/10;
			DecimalFormat df = new DecimalFormat("#.#");
			String s = df.format(x);   
			doc_version.add(s);

		}
		
		for(int i=0;i<doc_amount;i++){
			
			int r= ran.nextInt(domain_amount)+1;   
			doc_domain.add("D" + r);

		}
		
		for(int i=0;i<doc_amount;i++){
			
			int r= ran.nextInt(bs_amount);   
			doc_author.add(r);

		}
		
		//====================================
		
			
		
		//====== Set Device Information ======
		       
		for(int i=0;i<bs_amount+device_amount_per_domain*domain_amount;i++){
			
			int r= ran.nextInt(10);   
			switch(r){
			
			case 0:
				person_idType.add("National Health Insurabce");
				break;
			case 1:
			case 2:
				person_idType.add("Passport");
				break;
			default:
				person_idType.add("Tax ID");
				break;
			
			}

		}
		
		for(int i=0;i<bs_amount+device_amount_per_domain*domain_amount;i++){
			
			String id_type = person_idType.get(i);
			String id = "";
			boolean id_same = true;
			switch(id_type){
			
			case "National Health Insurabce" :
				id = (ran.nextInt(9000)+1000) + " " + (ran.nextInt(9000)+1000) + " " + (ran.nextInt(9000)+1000) + " " + (ran.nextInt(9000)+1000);
				
				while(id_same){
					
					if(!person_id.contains(id)){
						
						person_id.add(id);
						id_same = false;
						
					}
					
				}
				break;
			case "Passport" :
				id = "" + (ran.nextInt(900000000)+100000000);
				
				while(id_same){
					
					if(!person_id.contains(id)){
						
						person_id.add(id);
						id_same = false;
						
					}
					
				}
				
				break;
			case "Tax ID" :
				id = "" + (char)(ran.nextInt(26)+65) + (ran.nextInt(2)+1) + (ran.nextInt(90000000)+10000000);
				
				while(id_same){
					
					if(!person_id.contains(id)){
						
						person_id.add(id);
						id_same = false;
						
					}
					
				}
				
				break;
			}

		}
		
		for(int i=0;i<bs_amount;i++)
			device_idType.add("MAC address");
		
		for(int i=0;i<device_amount_per_domain*domain_amount;i++){
			
			int r= ran.nextInt(2);   
			switch(r){
			
			case 0:
				device_idType.add("IMEI");
				break;
			case 1:
				device_idType.add("MAC address");
				break;
			
			}

		}
		
		for(int i=0;i<bs_amount+device_amount_per_domain*domain_amount;i++){
			
			String id_type = device_idType.get(i);
			String id = "";
			boolean id_same = true;
			
			switch(id_type){
			
			case "IMEI" :
				id = "" + (ran.nextInt(90000000)+10000000) + (ran.nextInt(9000000)+1000000);
				
				while(id_same){
					
					if(!device_id.contains(id)){
						
						device_id.add(id);
						id_same = false;
						
					}
					
				}
				
				break;
			case "MAC address" :
				
				byte[] macAddr = new byte[6];
			    ran.nextBytes(macAddr);

			    macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

			    StringBuilder sb = new StringBuilder(18);
			    for(byte b : macAddr){

			        if(sb.length() > 0)
			            sb.append(":");

			        sb.append(String.format("%02x", b));
			    }
			    
			    id = sb.toString();
				
			    while(id_same){
					
					if(!device_id.contains(id)){
						
						device_id.add(id);
						id_same = false;
						
					}
					
				}
			    
				break;
			}

		}
		
		//====================================
		
		
		set = new VirtGraph (url, "dba", "dba");
		String str = "CLEAR GRAPH <http://pet.cs.nctu.edu.tw/simulation_trace>";
        VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
        vur.exec(); 
		
		//===== INSERT the document information to DB =====
		for(int i=0;i<doc_amount;i++){
			
			System.out.println("==> INSERT Doc" + i + " information");
			String uuid = insertDocumentInfo(set, i);
			doc_uuid.add(uuid);
			
		}
		
		//=================================================
		
		System.out.println("Document Information INSERT to DB done!!");
		
		this.gui = gui;
		String title = PANEL_TITLE;
		Settings s = new Settings("GUI.EventLogPanel");
		
		if (s.contains("nrofEvents")) {
			this.maxNrofEvents = s.getInt("nrofEvents");
		}
		if (s.contains("REfilter")) {
			this.regExp = s.getSetting("REfilter");
		}
		
		layout = new GridLayout(maxNrofEvents,1);

		this.setLayout(layout);
		if (this.regExp != null) {
			title += " - RE-filter: " + regExp;
		}
		this.setBorder(BorderFactory.createTitledBorder(
				getBorder(), title));
		
		this.eventPanes = new Vector<JPanel>(maxNrofEvents);
		this.font = new Font(FONT_TYPE,Font.PLAIN, FONT_SIZE);
		this.controls = createControls();
		
		// set log view to update every LOG_UP_INTERVAL milliseconds
		// also ensures that the update is done in Swing's EDT
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
		          updateLogView();
		      }
		  };
		  Timer t = new Timer(LOG_UP_INTERVAL, taskPerformer);
		  t.start();
	}

	/**
	 * Creates a control panel for the log
	 * @return The created EventLogControls
	 */
	private EventLogControlPanel createControls() {
		EventLogControlPanel c = new EventLogControlPanel();
		c.addHeading("connections");
		conUpCheck = c.addControl("up");
		conDownCheck = c.addControl("down");
		c.addHeading("messages");
		msgCreateCheck = c.addControl("created");
		msgTransferStartCheck = c.addControl("started relay");
		msgRelayCheck = c.addControl("relayed");
		msgDeliveredCheck = c.addControl("delivered");
		msgRemoveCheck = c.addControl("removed");
		msgDropCheck = c.addControl("dropped");
		msgAbortCheck = c.addControl("aborted");
		return c;
	}
	
	/**
	 * Returns the control panel that this log uses
	 * @return The control panel
	 */
	public EventLogControlPanel getControls() {
		return this.controls;
	}
	
	/**
	 * Adds a new event to the event log panel
	 * @param description Textual description of the event
	 * @param host1 Host that caused the event or null if there was not any
	 * @param host2 Another host that was involved in the event (or null)
	 * @param message Message that was involved in the event (or null)
	 * @param highlight If true, the log entry is highlighted
	 */
	private void addEvent(String description, DTNHost host1,
			DTNHost host2, Message message, boolean highlight) {
		JPanel eventPane = new JPanel();
		eventPane.setLayout(new BoxLayout(eventPane,BoxLayout.LINE_AXIS));
		
		String text = String.format(ENTRY_FORMAT, 
				SimClock.getTime(),description);
		JLabel label = new JLabel(text);
		label.setFont(font);
		eventPane.add(label);
		
		if (host1 != null) {
			addInfoButton(eventPane,host1,HOST_PROP);
		}
		if (host2 != null) {
			JLabel betweenLabel = new JLabel(HOST_DELIM);
			betweenLabel.setFont(font);
			eventPane.add(betweenLabel);
			addInfoButton(eventPane,host2,HOST_PROP);
		}
		if (message != null) {
			addInfoButton(eventPane, message, MSG_PROP);
		}

		if (highlight) {
			eventPane.setBackground(HIGHLIGHT_BG_COLOR);
		}
		
		eventPanes.add(eventPane);
		
		// if the log is full, remove oldest entries first
		if (this.eventPanes.size() > maxNrofEvents) {
			eventPanes.remove(0);
		}
	}
	
	/**
	 * Updates the log view
	 */
	private void updateLogView() {
		//TODO Optimization: Check if update is really necessary
		this.removeAll();
		for (int i=0; i< this.eventPanes.size(); i++) {
			this.add(eventPanes.get(i));
		}
		revalidate();
	}
	
	
	/**
	 * Adds a new button to a log entry panel and attaches a client 
	 * property into it
	 * @param panel Panel where to add the button
	 * @param o Client property object to add
	 * @param clientProp Client property key to use for the object
	 */
	private void addInfoButton(JPanel panel, Object o, String clientProp) {
		JButton hButton;
		hButton = new JButton(o.toString());
		hButton.putClientProperty(clientProp, o);
		hButton.addActionListener(this);
		hButton.setFont(font);
		hButton.setMargin(new Insets(0,0,0,0));
		hButton.setBackground(LOG_BUTTON_BG);
		panel.add(hButton);
	}
	
	/**
	 * Processes a log event
	 * @param check EventLogControls used to check if this entry type should
	 * be shown and/or paused upon
	 * @param name Text description of the event
	 * @param host1 First host involved in the event (if any, can be null)
	 * @param host2 Second host involved in the event (if any, can be null)
	 * @param message The message involved in the event (if any, can be null) 
	 */
	private void processEvent(EventLogControl check, final String name,
			final DTNHost host1, final DTNHost host2, final Message message) {
		/*if(had_doc_list.size()>0){
			FileWriter fw;
			BufferedWriter bufferWritter;
			
			try {
				fw = new FileWriter("trans_time.txt", true);
				bufferWritter = new BufferedWriter(fw);
				bufferWritter.write("" + SimClock.getTime());
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				fw = new FileWriter("trans_count.txt", true);
				bufferWritter = new BufferedWriter(fw);
				bufferWritter.write("" + (had_doc_list.get(0).size()-bs_amount));
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		if(SimClock.getTime() > 120 + pub_doc_count*120){
			/*if(pub_doc_count>=2 && sub_doc_get_count_after10.size()<300){
				sub_doc_get_count_after10.add(sub_doc_get_count.get(pub_doc_count-2));
			}*/
			
			/*Random ran = new Random();
			
			//===== Set Document Information =====
			int r= ran.nextInt(3);
			switch(r){
			
			case 0:
				doc_senLv.add("PRIVATE");
				break;
			case 1:
				doc_senLv.add("PUBLIC");
				break;
			case 2:
				doc_senLv.add("SENSITIVE");
				break;
			
			}
				
			r= ran.nextInt(90)+10;
			double x = ((double)r)/10;
			DecimalFormat df = new DecimalFormat("#.#");
			String s = df.format(x);   
			doc_version.add(s);
				
			r= ran.nextInt(bs_amount);   
			doc_author.add(r);*/
			
			ArrayList<String> tmp = new ArrayList<String>();
			had_doc_list.add(tmp);
			
			//String uuid = "" + UUID.randomUUID();
			doc_uuid.add(doc_uuid.get(pub_doc_count));
			
			
			
			
			
			if(pub_doc_count<doc_amount){
				for(int j=0;j<bs_amount;j++){
					had_doc_list.get(pub_doc_count).add("BS_" + j);
				}
			}
			pub_doc_count++;
		}
		
		/*if(SimClock.getTime() - recent_time > 120){
			if(file_flow!=0)
				System.out.println("Tag Flow/Total Flow: " + ((double)(tag_flow-tag_flow_recent)/(double)((tag_flow-tag_flow_recent)+(file_flow-file_flow_recent))));
			//System.out.println(tag_flow_recent + "(1)" + file_flow_recent);
			
			FileWriter fw;
			BufferedWriter bufferWritter;
			
			try {
				fw = new FileWriter("Tag_flow_lu_dp" + device_amount_per_domain + ".txt", true);
				bufferWritter = new BufferedWriter(fw);
				bufferWritter.write("" + ((double)tag_flow/(double)(tag_flow+file_flow)));
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				fw = new FileWriter("file_flow_dp" + device_amount_per_domain + ".txt", true);
				bufferWritter = new BufferedWriter(fw);
				bufferWritter.write("" + file_flow);
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				fw = new FileWriter("tag_flow_dp" + device_amount_per_domain + ".txt", true);
				bufferWritter = new BufferedWriter(fw);
				bufferWritter.write("" + tag_flow);
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				fw = new FileWriter("tag_count_dp" + device_amount_per_domain + ".txt", true);
				bufferWritter = new BufferedWriter(fw);
				bufferWritter.write("" + tag_count);
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			tag_flow_recent = tag_flow;
			file_flow_recent = file_flow;
			recent_time = (long) SimClock.getTime();
		}*/
		
		String descString;	// String format description of the event
		
		/*FileWriter fw;
		BufferedWriter bufferWritter;*/
			
		
		/*if(SimClock.getTime() > doc_amount*120 + 610){
			System.out.println("Simulation Done!!");
			int doc_get_count = 0;
			System.out.println(sub_doc_get_count_after10.size());
			for(int i=0;i<sub_doc_get_count_after10.size();i++){
				doc_get_count += sub_doc_get_count_after10.get(i);
			}
			System.out.println("Penetration: " + doc_get_count + " / " + sub_device_per_domain + " * " + doc_amount);
			Scanner scanner = new Scanner(System.in);
			String gg = scanner.next();*/
			//System.out.println("Document Flow: " + file_flow + ", Tag Flow: " + tag_flow);
			/*double average = 0.0;
			for(int i=0;i<doc_amount;i++){
				System.out.print("Document Flow: " + doc_file_flow.get(i) + ", Tag Flow: " + doc_tag_flow.get(i));
				average += (double)doc_tag_flow.get(i)/((double)doc_tag_flow.get(i)+(double)doc_file_flow.get(i));
				System.out.println(" " + (double)doc_tag_flow.get(i)/((double)doc_tag_flow.get(i)+(double)doc_file_flow.get(i)));
			}*/
			//System.out.println("Tag Flow/Total Flow: " + average/(double)doc_amount);
			/*System.out.println("Tag Make Size: " + tag_make_size);

			int get_count = 0;
			File file = new File("C:\\doc_get_count");
			if(!file.exists())
				file.mkdir();
			file = new File("C:\\doc_get_count\\dp_" + device_amount_per_domain + ".txt");
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try{		
				fw = new FileWriter("C:\\doc_get_count\\dp_" + device_amount_per_domain + ".txt", true);
				bufferWritter = new BufferedWriter(fw);
				for(int i=0;i<doc_amount;i++){
					int get = had_doc_list.get(i).size() - bs_amount;
					bufferWritter.write("" + get);
					bufferWritter.newLine();
					bufferWritter.flush();
					get_count += get;
				}
	    	    bufferWritter.close();
				//fw.append(descString);
				//fw.close();
			}catch(IOException ioe){
				//throw ioe;
			}
			System.out.println("Penetration: " + get_count + " / " + device_amount_per_domain*domain_amount + " * " + doc_amount);
			Scanner scanner = new Scanner(System.in);
			int ii = scanner.nextInt();
		}*/

		//System.out.println(file_flow + " < " + size_per_file*device_amount_per_domain*domain_amount*doc_amount + " ?");
		
		/*if(test_over){
			for(int i=0;i<had_doc_list.size();i++){
				
				if(had_doc_list.get(i).size()!=bs_amount+device_amount_per_domain*domain_amount){
					trans_over = false;
					break;
				}
			}
			
			if(trans_over){
				System.out.println("All node get All Document!!");
				test_over = true;
				Scanner scanner = new Scanner(System.in);
				int s = scanner.nextInt();
			}
			
		}*/
		
		
		if (!check.showEvent()) {
			return; // if event's "show" is not checked, won't pause either 
		}
		
		descString = name + " " + 
			(host1!=null ? host1 : "") + 
			(host2!= null ? (HOST_DELIM + host2) : "") + 
			(message!=null ? " " + message : "");
			
		String[] event = descString.split(" ", 3);
		
		//Determine the connect is legal? Because there are to connect interface:(1)WIFI-AP(2)phone&phone peer-to-peer,then(1)At least a BS(2)Both of phone
		boolean real_con = false;
		if(event[0].compareTo("Connection")==0){
			List<Connection> list_con = new ArrayList<Connection>();
			
			list_con = host1.getConnections();
			
			List<String> conn_interface = new ArrayList<String>();
			
			for(int i=0;i<list_con.size();i++){
				
				String from = list_con.get(i).fromNode.toString();
				String to = list_con.get(i).toNode.toString();
				
				
				//System.out.println(from + " == " + to);
				//System.out.println(host1.toString() + " ** " + host2.toString());
				
				
				if(from.compareTo(host1.toString())==0 && to.compareTo(host2.toString())==0){
					
					conn_interface.add(list_con.get(i).fromInterface.getInterfaceType());
					
				}
				
			}
			
			for(int i=0;i<conn_interface.size();i++){
				
				if(conn_interface.get(i).startsWith("ap")){
					
					if(host1.toString().startsWith("BS") || host2.toString().startsWith("BS")){
						real_con = true;
						break;
					}
					
				}else if(conn_interface.get(i).startsWith("ph")){
					
					if(!host1.toString().startsWith("BS") && !host2.toString().startsWith("BS")){
						real_con = true;
						break;
					}
					
				}
				
			}
		}
		
		int count = 0;
		int trans = 0;
		if(event[0].compareTo("Connection")==0 && real_con){
			String con = event[1];
			if(con.compareTo("UP")==0){
				String mac[] = event[2].split("<->");
				
				if(!set_recv_time&&pub_doc_count>0){
					for(int i=0;i<pub_doc_count;i++){
						count++;
						if(had_doc_list.get(i).size()!=bs_amount+device_amount_per_domain*domain_amount)
							if(handleUpEvent(i, mac[0],mac[1]))
								trans++;
					}
				}
				
				//Transfer tag to BS
				if(mac[0].startsWith("BS")&&!mac[1].startsWith("BS"))
					tag_flow += checkAndTransferTag(mac[0],mac[1], set);
				else if(!mac[0].startsWith("BS")&&mac[1].startsWith("BS"))
					tag_flow += checkAndTransferTag(mac[1],mac[0], set);
				
				if(!set_recv_time && doc_get_count>=doc_amount*device_amount_per_domain*domain_amount){
					doc_all_recv_time = SimClock.getTime();
					set_recv_time = true;
				}
				
				if(tag_recv_count>=doc_amount*device_amount_per_domain*domain_amount*2){
					System.out.println("All node get All Document time is: " + doc_all_recv_time);
					System.out.println("All tag receice twist!! time: " + SimClock.getTime());
					Scanner scanner = new Scanner(System.in);
					String s = scanner.next();
				}
				
				/*if(file_flow == size_per_file*device_amount_per_domain*domain_amount*doc_amount && !trans_over){
					trans_over = true;
					System.out.println("All node get All Document!!");
					System.out.println("Type to continue");
					System.out.println("Document Flow: " + file_flow + ", Tag Flow: " + tag_flow);
					try {
						System.out.println("Total Make Tag Size: " + getFileSize(new File("c:\\simulation_tag")));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println("Time: " + SimClock.getTime());
					
					int count2 =0;
					
					for(int i=bs_amount;i<tag_in_device.size();i++){
						for(int j=0;j<tag_in_device.get(i).size();j++){
							tag_not_in_bs.add(tag_in_device.get(i).get(j));
							count2++;
						}
					}
					
					//System.out.println(tag_not_in_bs.size() + " " + count2);
					
					for(int i=0;i<tag_in_bs.size();i++){
						int index = tag_not_in_bs.indexOf(tag_in_bs.get(i));
						if(index<0){
							System.out.println("index error");
						}else{
							tag_not_in_bs.remove(index);
							//System.out.println(index);
						}
					}
					//deleteAll(new File("c:\\simulation_tag"));
					Scanner scanner = new Scanner(System.in);
					String s = scanner.next();
				}*/
				
				/*if(trans_over)
					System.out.println("tag_not_in_bs.size: " + tag_not_in_bs.size());*/
				
				//if(trans_over && tag_in_bs.size()==doc_amount*device_amount_per_domain*domain_amount){
				/*if(trans_over && tag_not_in_bs.size()==0){
					System.out.println("BS get all tag");
					deleteAll(new File("c:\\simulation_tag"));
					System.out.println("Time: " + SimClock.getTime());
					Scanner scanner = new Scanner(System.in);
					String s = scanner.next();
				}*/
				
				/*for(int i=0;i<had_doc_list.size();i++){
					
					count += had_doc_list.get(i).size();
				}*/
				
				//System.out.println("Document Flow: " + FormetFileSize(file_flow) + ", Tag Flow: " + FormetFileSize(tag_flow));
			}
		}
		
		count = 0;
		trans = 0;
		
		
		
		/*try{		
			fw = new FileWriter("test.txt", true);
			bufferWritter = new BufferedWriter(fw);
    	    bufferWritter.write(SimClock.getTime() + " " + descString + " " + trans);
			bufferWritter.newLine();
			bufferWritter.flush();
    	    bufferWritter.close();
			//fw.append(descString);
			//fw.close();
		}catch(IOException ioe){
			//throw ioe;
		}*/
	
		if (regExp != null && !descString.matches(regExp)){
			return;	// description doesn't match defined regular expression
		}
		
		if (check.pauseOnEvent()) {
			gui.setPaused(true);
			if (host1 != null) {
				gui.setFocus(host1);
			}
		}
		
      	addEvent(name, host1, host2, message, check.pauseOnEvent());	
	}
	
	// Implementations of ConnectionListener and MessageListener interfaces
	public void hostsConnected(DTNHost host1, DTNHost host2) {
		processEvent(conUpCheck, "Connection UP", host1, host2, null);
	}

	public void hostsDisconnected(DTNHost host1, DTNHost host2) {
		processEvent(conDownCheck, "Connection DOWN", host1, host2, null);
	}

	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (!dropped) {
			processEvent(msgRemoveCheck, "Message removed", where, null, m);
		}
		else {
			processEvent(msgDropCheck, "Message dropped", where, null, m);
		}
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean firstDelivery) {
		if (firstDelivery) {
			processEvent(msgDeliveredCheck, "Message delivered", from, to, m); 
		}
		else if (to == m.getTo()) {
			processEvent(msgDeliveredCheck, "Message delivered again", 
					from, to, m);
		}
		else {
			processEvent(msgRelayCheck, "Message relayed", from, to, m);
		}
	}

	public void newMessage(Message m) {
		processEvent(msgCreateCheck, "Message created", m.getFrom(), null, m);
	}
	
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		processEvent(msgAbortCheck, "Message relay aborted", from, to, m);
	}
	
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		processEvent(msgTransferStartCheck,"Message relay started", from,
				to,m);
		
	}
	
	// end of message interface implementations
	
	
	/**
	 * Action listener for log entry (host & message) buttons
	 */
	public void actionPerformed(ActionEvent e) {
		JButton source = (JButton)e.getSource();
		
		if (source.getClientProperty(HOST_PROP) != null) {
			// button was a host button -> focus it on GUI
			gui.setFocus((DTNHost)source.getClientProperty(HOST_PROP));
		}
		else if (source.getClientProperty(MSG_PROP) != null) {
			// was a message button -> show information about the message
			Message m = (Message)source.getClientProperty(MSG_PROP);
			gui.getInfoPanel().showInfo(m);
		}
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + " with " + 
			this.eventPanes.size() + " events";
	}
	
	private static String insertDocumentInfo(VirtGraph set, int doc_num) {		//Insert the document info to DB
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		Model model = ModelFactory.createDefaultModel();
		
		String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
		String pre_property_person = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Person_";
		String pre_property_domain = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Domain_";
		
		Property has_uuid = model.createProperty(pre_property + "hasUUID");
		
		Property has_title = model.createProperty(pre_property + "hasTitle");
		Property sensitivity_level = model.createProperty(pre_property + "hasSensitivityLevel");
		Property has_version = model.createProperty(pre_property + "hasVersion");
		
		Property has_domain_name = model.createProperty(pre_property + "hasDomainName");

		Property belong_to = model.createProperty(pre_property + "belongsTo");
		Property write_by = model.createProperty(pre_property + "hasAuthor");
		
		Property has_id = model.createProperty(pre_property + "hasID");
		
		Property has_person_id_type = model.createProperty(pre_property + "hasPersonIDtype");
		Property has_legal_name = model.createProperty(pre_property + "hasLegalName");
		
		//VirtGraph set = new VirtGraph (url, "dba", "dba");
		
		String uuid = "" + UUID.randomUUID();
		
		Resource record = model.createResource(pre_property + "Document_" + uuid).addProperty(has_uuid, uuid)
																						 .addProperty(has_title, "Doc" + (doc_num + 1))
																						 .addProperty(sensitivity_level, doc_senLv.get(doc_num))
																						 .addProperty(has_version, doc_version.get(doc_num));
		record.addProperty(write_by, model.createResource(pre_property_person + "http://PETLab.nctu.edu.tw/Person" + doc_author.get(doc_num)).addProperty(has_person_id_type, person_idType.get(doc_author.get(doc_num)))
																																	   .addProperty(has_id, person_id.get(doc_author.get(doc_num)))
																																	   .addProperty(has_legal_name, "Person" + doc_author.get(doc_num)));
				
		record.addProperty(belong_to, model.createResource(pre_property_domain + "http://PETLab.nctu.edu.tw/").addProperty(has_domain_name, "PETLab"));
		
		StringWriter out = new StringWriter();
		model.write(out, "N-TRIPLE");
		//System.out.println(out.toString());
		
		String str = "insert into graph <http://pet.cs.nctu.edu.tw/trace> { " + out.toString() +" }";
		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
		vur.exec();
		
		return uuid;
		
	}
	
	private static boolean handleUpEvent2(int doc_num, String mac1, String mac2) {
		String uuid = "" + UUID.randomUUID();
		boolean trans_doc = false;
		if(had_doc_list.get(doc_num).contains(mac1) && !had_doc_list.get(doc_num).contains(mac2)){
			int from_num = Integer.parseInt(mac1.split("_")[1]);
			int to_num = Integer.parseInt(mac2.split("_")[1]);
			//doc_get_count++;
			had_doc_list.get(doc_num).add(mac2);
			tag_in_device.get(to_num).add("tag_" + uuid + ".rdf");
			tag_send_count.get(to_num).add(new Integer(0));
			if(sub_doc_list.get(doc_num).contains(to_num)){
				sub_doc_get_count.set(doc_num, sub_doc_get_count.get(doc_num)+1);
				System.out.println("=====================" +sub_doc_get_count.get(doc_num) + " ?=? " + sub_device_per_domain*9/10);
				if(sub_doc_get_count.get(doc_num)==sub_device_per_domain*9/10){
					pa90_time.set(doc_num, SimClock.getTime()-(double)(doc_num+1)*120.0);
					pa90_set_count++;
					if(pa90_set_count==doc_amount){
						double coo = 0.0;
						for(int i=0;i<pa90_time.size();i++){
							coo += pa90_time.get(i);
						}
						System.out.println("Average 90% time: " + coo/300.0);
						Scanner scanner = new Scanner(System.in);
						String gg = scanner.nextLine();
					}
					System.out.println("=====================" + pa90_set_count);
				}
				doc_get_count++;
				System.out.println("Doc" + (doc_num+1) + " " + from_num + " -> " + to_num);
				System.out.println("receive count: " + doc_get_count);
			}
			trans_doc = true;
		}
		if(had_doc_list.get(doc_num).contains(mac2) && !had_doc_list.get(doc_num).contains(mac1)){
			int from_num = Integer.parseInt(mac2.split("_")[1]);
			int to_num = Integer.parseInt(mac1.split("_")[1]);
			//doc_get_count++;
			had_doc_list.get(doc_num).add(mac1);
			tag_in_device.get(to_num).add("tag_" + uuid + ".rdf");
			tag_send_count.get(to_num).add(new Integer(0));
			if(sub_doc_list.get(doc_num).contains(to_num)){
				sub_doc_get_count.set(doc_num, sub_doc_get_count.get(doc_num)+1);
				if(sub_doc_get_count.get(doc_num)==sub_device_per_domain*9/10){
					pa90_time.set(doc_num, SimClock.getTime()-(double)(doc_num+1)*120.0);
					pa90_set_count++;
					if(pa90_set_count==doc_amount){
						double coo = 0.0;
						for(int i=0;i<pa90_time.size();i++){
							coo += pa90_time.get(i);
						}
						System.out.println("Average 90% time: " + coo/300.0);
						Scanner scanner = new Scanner(System.in);
						String gg = scanner.nextLine();
					}
					System.out.println("=====================" + pa90_set_count);
				}
				doc_get_count++;
				System.out.println("Doc" + (doc_num+1) + " " + from_num + " -> " + to_num);
				System.out.println("receive count: " + doc_get_count);
			}
			trans_doc = true;
		}
		return trans_doc;
	}
	
	//New Add To Make Tag
	private static boolean handleUpEvent(int doc_num, String mac1, String mac2) {	//If two device connect, check the data transfer
		// TODO Auto-generated method stub
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		boolean trans_doc = false;
		
		long timestamp = start_time + (long)SimClock.getTime()*1000;
		
		if(timestamp <= last_timestamp){
			timestamp = last_timestamp + 1000;
		}
		
		last_timestamp = timestamp;
		
		double d_timestamp = timestamp/1000.0;
		Date date = new Date(timestamp);
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd kk:mm:ss");
		
		/*String prefix = "http://PETLab.cs.nctu.edu.tw/";
		String prefix1 = "http://NNLab.cs.nctu.edu.tw/";
		String prefix2 = "http://MMLab.cs.nctu.edu.tw/";*/
		
		Model model = ModelFactory.createDefaultModel();
		String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
		String pre_property_person = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Person_";
		String pre_property_device = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Device_";
		String pre_property_domain = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Domain_";
		
		Property has_uuid = model.createProperty(pre_property + "hasUUID");
		Property time = model.createProperty(pre_property + "hasOccurAt");
		
		Property title = model.createProperty(pre_property + "hasTitle");
		Property sensitivity_level = model.createProperty(pre_property + "hasSensitivityLevel");
		Property has_version = model.createProperty(pre_property + "hasVersion");
		
		Property has_device_id_type = model.createProperty(pre_property + "hasDeviceIDtype");
		Property has_id = model.createProperty(pre_property + "hasID");
		Property has_alias = model.createProperty(pre_property + "hasAlias");
		
		Property has_domain_name = model.createProperty(pre_property + "hasDomainName");
		
		Property has_person_id_type = model.createProperty(pre_property + "hasPersonIDtype");
		Property has_legal_name = model.createProperty(pre_property + "hasLegalName");
		
		Property doc = model.createProperty(pre_property + "isTransportSessionOf");
		Property doc_re = model.createProperty(pre_property + "hasTransportSession");
		Property from = model.createProperty(pre_property + "hasSource");
		Property to = model.createProperty(pre_property + "hasDestination");

		Property belong_to = model.createProperty(pre_property + "belongsTo");
		Property has_owner = model.createProperty(pre_property + "ownedBy");
		Property write_by = model.createProperty(pre_property + "hasAuthor");
		
		FileWriter fw;
		BufferedWriter bufferWritter;
		
		//long timestamp = 0;
		String document = "";
		
		//System.out.println(had_doc_list.get(doc_num).size());	
		
		//Make Tag
		try{		
			fw = new FileWriter("EventLog.txt", true);
			bufferWritter = new BufferedWriter(fw);
			
			document = "";
			
			if(had_doc_list.get(doc_num).contains(mac1) && !had_doc_list.get(doc_num).contains(mac2)){
				
				doc_get_count++;
					
				document = "Doc" + (doc_num+1);
				
				String from_domain = "";
				String to_domain = "";
				String from_person = "";
				String to_person = "";
				String from_idType = "";
				String from_id = "";
				String to_idType = "";
				String to_id = "";
				String from_didType = "";
				String from_did = "";
				String to_didType = "";
				String to_did = "";
				String from_alias = "";
				String to_alias = "";
				
				int from_num = Integer.parseInt(mac1.split("_")[1]);
				int to_num = Integer.parseInt(mac2.split("_")[1]);
				
				if(mac1.startsWith("BS")){
					from_domain = "PETLab";
					from_alias = "BS" + from_num;
				}else{
					from_domain = mac1.split("_")[0];						
					if(((from_num-bs_amount)%device_amount_per_domain) <(device_amount_per_domain/2)){
						from_alias = "Ph" + ((from_num-bs_amount)%device_amount_per_domain+1);
					}else{
						from_alias = "Tb" + ((from_num-bs_amount)%device_amount_per_domain-(device_amount_per_domain/2)+1);
					}	
				}
				
				if(mac2.startsWith("BS")){
					to_domain = "PETLab";
					to_alias = "BS" + to_num;
				}else{
					to_domain = mac2.split("_")[0];						
					if(((to_num-bs_amount)%device_amount_per_domain) <(device_amount_per_domain/2)){
						to_alias = "Ph" + ((to_num-bs_amount)%device_amount_per_domain+1);
					}else{
						to_alias = "Tb" + ((to_num-bs_amount)%device_amount_per_domain-(device_amount_per_domain/2)+1);
					}	
				}
				
				String from_domainUri = "http://" + from_domain + ".nctu.edu.tw/";
				String to_domainUri = "http://" + to_domain + ".nctu.edu.tw/";
				
				from_person = "person" + from_num;
				from_idType = person_idType.get(from_num);
				from_id = person_id.get(from_num);
				
				to_person = "person" + to_num;
				to_idType = person_idType.get(to_num);
				to_id = person_id.get(to_num);				
				
				from_didType = device_idType.get(from_num);
				from_did = device_id.get(from_num);
				
				to_didType = device_idType.get(to_num);
				to_did = device_id.get(to_num);
				
				System.out.println("Doc" + (doc_num+1) + " " + from_domain + "_" + from_alias + " -> " + to_domain + "_" + to_alias + " at " + ft.format(date));
				file_flow += size_per_file;
				bufferWritter.write("Doc" + (doc_num+1) + " " + from_domain + "_" + from_alias + " -> " + to_domain + "_" + to_alias + " at " + ft.format(date));
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
				had_doc_list.get(doc_num).add(mac2);
				//get_doc_time.get(to_num).set(doc_num, timestamp);
				
				String uuid = doc_uuid.get(doc_num);
				Resource person_from = model.createResource(pre_property_person + from_domainUri + from_person).addProperty(has_person_id_type, from_idType).addProperty(has_id, from_id).addProperty(has_legal_name, from_person).addProperty(belong_to, model.createResource(pre_property_domain + from_domainUri).addProperty(has_domain_name, from_domain));
				Resource person_to = model.createResource(pre_property_person + to_domainUri + to_person).addProperty(has_person_id_type, to_idType).addProperty(has_id, to_id).addProperty(has_legal_name, to_person).addProperty(belong_to, model.createResource(pre_property_domain + to_domainUri).addProperty(has_domain_name, to_domain));
				Resource docRes = model.createResource(pre_property + "Document_" + uuid);
				
				uuid = "" + UUID.randomUUID();
				Resource record_tag = model.createResource(pre_property + "TransportSession_" + uuid).addProperty(has_uuid, uuid);
				record_tag.addProperty(doc, docRes);
				record_tag.addProperty(from, model.createResource(pre_property_device + from_did).addProperty(has_device_id_type, from_didType).addProperty(has_id, from_did).addProperty(has_alias, from_alias)
											.addProperty(has_owner, person_from)
											.addProperty(belong_to, model.createResource(pre_property_domain + from_domainUri).addProperty(has_domain_name, from_domain)));
				
				record_tag.addProperty(to, model.createResource(pre_property_device + to_did).addProperty(has_device_id_type, to_didType).addProperty(has_id, to_did).addProperty(has_alias, to_alias)
											.addProperty(has_owner, person_to)
											.addProperty(belong_to, model.createResource(pre_property_domain + to_domainUri).addProperty(has_domain_name, to_domain)));
	
				//timestamp = System.currentTimeMillis() + (long)SimClock.getTime()*1000;
				record_tag.addProperty(time, "" + timestamp);
				docRes.addProperty(doc_re, record_tag);

				//File rdfFile = new File("C:\\TAG\\tag_" + uuid + ".rdf");
				File tagDoc = new File("C:\\simulation_tag\\" + mac2);
				if(!tagDoc.exists()){  
					tagDoc.mkdir();    
				}
				
				File rdfFile = new File("C:\\simulation_tag\\" + mac2 + "\\tag_" + uuid + ".rdf");
				File rdfFile2 = new File("C:\\tag\\tag_" + uuid + ".rdf");
				rdfFile.createNewFile();
				rdfFile2.createNewFile();
				tag_in_device.get(to_num).add("tag_" + uuid + ".rdf");
				tag_send_count.get(to_num).add(new Integer(0));
				//doc_file_flow.set(doc_num, doc_file_flow.get(doc_num) + size_per_file);
				//tag_of_doc.get(doc_num).add("tag_" + uuid + ".rdf");
				tag_count++;
				
				FileOutputStream fos = null;
				FileOutputStream fos2 = null;
				try {
					fos = new FileOutputStream(rdfFile.getAbsolutePath());
					fos2 = new FileOutputStream(rdfFile2.getAbsolutePath());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				model.write(fos);
				model.write(fos2);
				try {
					fos.close();
					fos2.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				trans_doc = true;
				
				FileInputStream fis = null;
	            try {
					fis = new FileInputStream("C:\\simulation_tag\\" + mac2 + "\\tag_" + uuid + ".rdf");
					tag_make_size += fis.available();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else if(had_doc_list.get(doc_num).contains(mac2) && !had_doc_list.get(doc_num).contains(mac1)){
				
				doc_get_count++;
				
				document = "Doc" + (doc_num+1);
				
				String from_domain = "";				
				String to_domain = "";
				String from_person = "";
				String to_person = "";
				String from_idType = "";
				String from_id = "";
				String to_idType = "";
				String to_id = "";
				String from_didType = "";
				String from_did = "";
				String to_didType = "";
				String to_did = "";
				String from_alias = "";
				String to_alias = "";
				
				int from_num = Integer.parseInt(mac2.split("_")[1]);
				int to_num = Integer.parseInt(mac1.split("_")[1]);
				
				if(mac2.startsWith("BS")){
					from_domain = "PETLab";
					from_alias = "BS" + from_num;
				}else{
					from_domain = mac2.split("_")[0];						
					if(((from_num-bs_amount)%device_amount_per_domain) <(device_amount_per_domain/2)){
						from_alias = "Ph" + ((from_num-bs_amount)%device_amount_per_domain+1);
					}else{
						from_alias = "Tb" + ((from_num-bs_amount)%device_amount_per_domain-(device_amount_per_domain/2)+1);
					}	
				}
				
				if(mac1.startsWith("BS")){
					to_domain = "PETLab";
					to_alias = "BS" + to_num;
				}else{
					to_domain = mac1.split("_")[0];						
					if(((to_num-bs_amount)%device_amount_per_domain) <(device_amount_per_domain/2)){
						to_alias = "Ph" + ((to_num-bs_amount)%device_amount_per_domain+1);
					}else{
						to_alias = "Tb" + ((to_num-bs_amount)%device_amount_per_domain-(device_amount_per_domain/2)+1);
					}	
				}
				
				String from_domainUri = "http://" + from_domain + ".nctu.edu.tw/";
				String to_domainUri = "http://" + to_domain + ".nctu.edu.tw/";
				
				from_person = "person" + from_num;
				from_idType = person_idType.get(from_num);
				from_id = person_id.get(from_num);
				
				to_person = "person" + to_num;
				to_idType = person_idType.get(to_num);
				to_id = person_id.get(to_num);				
				
				from_didType = device_idType.get(from_num);
				from_did = device_id.get(from_num);
				
				to_didType = device_idType.get(to_num);
				to_did = device_id.get(to_num);
				
				System.out.println("Doc" + (doc_num+1) + " " + from_domain + "_" + from_alias + " -> " + to_domain + "_" + to_alias + " at " + ft.format(date));
				file_flow += size_per_file;
				bufferWritter.write("Doc" + (doc_num+1) + " " + from_domain + "_" + from_alias + " -> " + to_domain + "_" + to_alias + " at " + ft.format(date));
				bufferWritter.newLine();
				bufferWritter.flush();
	    	    bufferWritter.close();
				had_doc_list.get(doc_num).add(mac1);
				//get_doc_time.get(to_num).set(doc_num, timestamp);
				
				String uuid = doc_uuid.get(doc_num);
				Resource person_from = model.createResource(pre_property_person + from_domainUri + from_person).addProperty(has_person_id_type, from_idType).addProperty(has_id, from_id).addProperty(has_legal_name, from_person).addProperty(belong_to, model.createResource(pre_property_domain + from_domainUri).addProperty(has_domain_name, from_domain));
				Resource person_to = model.createResource(pre_property_person + to_domainUri + to_person).addProperty(has_person_id_type, to_idType).addProperty(has_id, to_id).addProperty(has_legal_name, to_person).addProperty(belong_to, model.createResource(pre_property_domain + to_domainUri).addProperty(has_domain_name, to_domain));
				Resource docRes = model.createResource(pre_property + "Document_" + uuid);
				
				uuid = "" + UUID.randomUUID();
				Resource record_tag = model.createResource(pre_property + "TransportSession_" + uuid).addProperty(has_uuid, uuid);
				record_tag.addProperty(doc, docRes);
				record_tag.addProperty(from, model.createResource(pre_property_device + from_did).addProperty(has_device_id_type, from_didType).addProperty(has_id, from_did).addProperty(has_alias, from_alias)
											.addProperty(has_owner, person_from)
											.addProperty(belong_to, model.createResource(pre_property_domain + from_domainUri).addProperty(has_domain_name, from_domain)));
				
				record_tag.addProperty(to, model.createResource(pre_property_device + to_did).addProperty(has_device_id_type, to_didType).addProperty(has_id, to_did).addProperty(has_alias, to_alias)
											.addProperty(has_owner, person_to)
											.addProperty(belong_to, model.createResource(pre_property_domain + to_domainUri).addProperty(has_domain_name, to_domain)));
	
				//timestamp = System.currentTimeMillis() + (long)SimClock.getTime()*1000;
				record_tag.addProperty(time, "" + timestamp);
				docRes.addProperty(doc_re, record_tag);
				
				File tagDoc = new File("C:\\simulation_tag\\" + mac1);
				if(!tagDoc.exists()){   
					tagDoc.mkdir();    
				}

				File rdfFile = new File("C:\\simulation_tag\\" + mac1 + "\\tag_" + uuid + ".rdf");
				File rdfFile2 = new File("C:\\tag\\tag_" + uuid + ".rdf");
				rdfFile.createNewFile();
				rdfFile2.createNewFile();
				tag_in_device.get(to_num).add("tag_" + uuid + ".rdf");
				tag_send_count.get(to_num).add(new Integer(0));
				//doc_file_flow.set(doc_num, doc_file_flow.get(doc_num) + size_per_file);
				//tag_of_doc.get(doc_num).add("tag_" + uuid + ".rdf");
				tag_count++;
				
				FileOutputStream fos = null;
				FileOutputStream fos2 = null;
				try {
					fos = new FileOutputStream(rdfFile.getAbsolutePath());
					fos2 = new FileOutputStream(rdfFile2.getAbsolutePath());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				model.write(fos);
				model.write(fos2);
				try {
					fos.close();
					fos2.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				trans_doc = true;
				
				FileInputStream fis = null;
	            try {
					fis = new FileInputStream("C:\\simulation_tag\\" + mac1 + "\\tag_" + uuid + ".rdf");
					tag_make_size += fis.available();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}catch(IOException ioe){
			//throw ioe;
		}
		
		return trans_doc;
		
	}
	
	
	private static long checkAndTransferTag(String bs, String device, VirtGraph set){
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		//VirtGraph set = new VirtGraph (url, "dba", "dba");
		
		Model model = ModelFactory.createDefaultModel();
		
		long flow = 0;
		
		int from_num = Integer.parseInt(device.split("_")[1]);
		int to_num = Integer.parseInt(bs.split("_")[1]);
		
		int count =0;
		if(tag_in_device.get(from_num).size()>0 && tag_send_count.get(from_num).size()>0){
			for(int i=tag_in_device.get(from_num).size()-1;i>=0;i--){
				
				if(!tag_in_device.get(to_num).contains(tag_in_device.get(from_num).get(i))&&!trans_over && tag_send_count.get(from_num).get(i)<2){
					tag_in_device.get(to_num).add(tag_in_device.get(from_num).get(i));
					tag_send_count.get(from_num).set(i, tag_send_count.get(from_num).get(i)+1);
					
					FileInputStream fis = null;
		            try {
						fis = new FileInputStream("C:\\simulation_tag\\" + device + "\\" + tag_in_device.get(from_num).get(i));
						flow += fis.available();
						long tag_size = fis.available();
						/*int doc_num = -1;
						for(int j=0;j<doc_amount;j++){
							if(tag_of_doc.get(j).contains(tag_in_device.get(from_num).get(i))){
								doc_num = j;
								break;
							}
						}
						doc_tag_flow.set(doc_num, doc_tag_flow.get(doc_num) + tag_size);*/
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
		            InputStream in = FileManager.get().open("C:\\simulation_tag\\" + device + "\\" + tag_in_device.get(from_num).get(i));
	        		if (in == null) {
	        		    throw new IllegalArgumentException(
	        		                                 "File: " + "C:\\simulation_tag\\" + device + "\\" + tag_in_device.get(from_num).get(i) + " not found");
	        		}
	        		
	        		model.removeAll();
	        		model.read(in, null);
		            
		            StringWriter out = new StringWriter();
		    		model.write(out, "N-TRIPLE");
		    		//System.out.println(out.toString());
		    		
		    		String str = "insert into graph <http://pet.cs.nctu.edu.tw/simulation_trace> { " + out.toString() +" }";
		    		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
		    		vur.exec();
		            
		            
		            
		            count++;
		            
		            /*if(!tag_in_bs.contains(tag_in_device.get(from_num).get(i))){
		            	tag_in_bs.add(tag_in_device.get(from_num).get(i));
		            	count++;
		            }*/
		            
		            copyFile("C:\\simulation_tag\\" + device + "\\" + tag_in_device.get(from_num).get(i), "C:\\simulation_tag\\" + bs + "\\" + tag_in_device.get(from_num).get(i));
		            
		            if(tag_send_count.get(from_num).get(i)==2){
						tag_in_device.get(from_num).remove(i);
						tag_send_count.get(from_num).remove(i);
					}
		            
		            tag_recv_count++;
		            System.out.println("Tag receive: " + tag_recv_count + " < " + doc_amount*device_amount_per_domain*domain_amount*2);
				}
				
				/*if(trans_over){
					int index = tag_not_in_bs.indexOf(tag_in_device.get(from_num).get(i));
					if(index >=0){
						tag_not_in_bs.remove(index);
						//System.gc();
					}
				}*/
				
				/*File bs_tag_file = new File("C:\\simulation_tag\\" + bs + "\\" + device_list[i].getName());
				if(!bs_tag_file.exists()){
					copyFile("C:\\simulation_tag\\" + device + "\\" + device_list[i].getName(), "C:\\simulation_tag\\" + bs + "\\" + device_list[i].getName());
					FileInputStream fis = null;
		            try {
						fis = new FileInputStream(bs_tag_file);
						flow += fis.available();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
				}*/
			}
		}
		
		//System.out.println("receive " + count + " tags.");
		/*File bs_file = new File("C:\\simulation_tag\\" + bs);
		File device_file = new File("C:\\simulation_tag\\" + device);
		File[] bs_list = bs_file.listFiles();
		File[] device_list = device_file.listFiles();
		
		for(int i=0;i<device_list.length;i++){
			
			File bs_tag_file = new File("C:\\simulation_tag\\" + bs + "\\" + device_list[i].getName());
			if(!bs_tag_file.exists()){
				copyFile("C:\\simulation_tag\\" + device + "\\" + device_list[i].getName(), "C:\\simulation_tag\\" + bs + "\\" + device_list[i].getName());
				FileInputStream fis = null;
	            try {
					fis = new FileInputStream(bs_tag_file);
					flow += fis.available();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
			}
		}*/
		
		return flow;
		
	}
	
	
	
	public static void copyFile(String srFile, String dtFile){
    	try{
    		File f1 = new File(srFile);
    		File f2 = new File(dtFile);
    		if(!f2.exists())
    			f2.createNewFile();
    		InputStream in = new FileInputStream(f1);
    		OutputStream out = new FileOutputStream(f2);
    		byte[] buf = new byte[1024];
    		int len;
    		while ((len = in.read(buf)) > 0){
    			out.write(buf, 0, len);
    		}
    		in.close();
    		out.close();
    	}
    	catch(FileNotFoundException ex){
    		ex.printStackTrace();  
    	}
    	catch(IOException e){
    		e.printStackTrace();    
    	}
    }
	
    public long getFileSizes(File f) throws Exception{
        long s=0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
           s= fis.available();
        } else {
            f.createNewFile();
            System.out.println("文件不存在");
        }
        return s;
    }
    
    public long getFileSize(File f)throws Exception
    {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++)
        {
            if (flist[i].isDirectory())
            {
                size = size + getFileSize(flist[i]);
            } else
            {
                size = size + flist[i].length();
            }
        }
        return size;
    }
    public String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }
   
    public long getlist(File f){
        long size = 0;
        File flist[] = f.listFiles();
        size=flist.length;
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getlist(flist[i]);
                size--;
            }
        }
        return size;
    }
    
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
    
    private void resetInfo(){
    	
    }
	
}
