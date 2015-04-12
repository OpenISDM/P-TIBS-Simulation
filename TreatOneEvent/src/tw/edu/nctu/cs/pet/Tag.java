package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class Tag {
	//private int doc_num = -1;
	//private int from = -1;;
	//private int to = -1;

	//private double tag_make_time = 0.0;
	private ArrayList<Triple> combine_tag = new ArrayList<Triple>();
	private String tag_id = "";
	private ArrayList<Integer> has_this_tag_device_num_list = new ArrayList<Integer>();
	private double size = 0;
	
	public Tag(String id){
		this.tag_id = id;
	}
	
	public Tag(int doc, int a, int b, int c, String id, int mac, double tag_size){
		//this.doc_num = doc;
		//this.from = a;
		//this.to = b;
		combine_tag.add(new Triple(doc, a, b));
		//this.tag_make_time = now;
		this.tag_id = id;
		has_this_tag_device_num_list.add(mac);
		this.size = tag_size;
	}
	
	public int addDeviceNum(int mac){
		has_this_tag_device_num_list.add(mac);
		return has_this_tag_device_num_list.size()-1;	//return mac location
	}
	
	/*public Triple getTriple(){
		return new Triple(this.doc_num, from, to);
	}*/
	
	public ArrayList<Triple> getTriple(){
		return combine_tag;
	}
	
	public Triple getTriple(int n){
		return combine_tag.get(n);
	}
	
	public String getId(){
		return this.tag_id;
	}
	
	public boolean checkTagHaveToSend(int from, int to){
		if(has_this_tag_device_num_list.contains(from) && !has_this_tag_device_num_list.contains(to))
			return true;
		else
			return false;
	}
	
	public boolean checkTagHaveToSend(int to){
		if(!has_this_tag_device_num_list.contains(to))
			return true;
		else
			return false;
	}
	
	public double getSize(){
		return this.size;
	}
	
	public void aggreTag(Tag t){
		ArrayList<Triple> tri_list = t.getTriple();
		
		for(int i=0;i<tri_list.size();i++){
			if(!this.combine_tag.contains(tri_list.get(i))){
				this.combine_tag.add(tri_list.get(i));
				this.size += main.getTagSize();
			}
		}
		
	}
	
	/*public int getDocNum(){
		return this.doc_num;
	}
	
	public int getTagsFrom(){
		return this.from;
	}
	
	public int getTagsTo(){
		return this.to;
	}*/
	
}
