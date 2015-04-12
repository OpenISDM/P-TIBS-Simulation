package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class TagInDevice {
	/*private boolean[][][] tag_in_device_bool = new boolean[main.getDocAmount()+1][main.getDeviceAmount()][main.getDeviceAmount()];
	private Tag[][][] tag_in_device = new Tag[main.getDocAmount()+1][main.getDeviceAmount()][main.getDeviceAmount()];
	private ArrayList<Triple> tag_in_device_list= new ArrayList<Triple>();
	
	public TagInDevice(){
		for(int j=1;j<main.getDocAmount()+1;j++){
			for(int k=0;k<main.getDeviceAmount();k++){
				for(int l=0;l<main.getDeviceAmount();l++){
					tag_in_device_bool[j][k][l] = false;
					tag_in_device[j][k][l] = null;
				}
			}
		}
	}
	
	public void setBoolean(int doc, int from, int to, boolean set){
		tag_in_device_bool[doc][from][to] = set;
		if(set){
			tag_in_device_list.add(new Triple(doc, from,to));
		}else{
			tag_in_device_list.remove(new Triple(doc, from,to));
		}
	}
	
	public void setTag(int doc, int from, int to, Tag tag){
		tag_in_device[doc][from][to] = tag;
	}
	
	public boolean getBoolean(int doc, int from, int to){
		return this.tag_in_device_bool[doc][from][to];
	}
	
	public Tag getTag(int doc, int from, int to){
		return this.tag_in_device[doc][from][to];
	}
	
	public ArrayList<Triple> getTagList(){
		return this.tag_in_device_list;
	}
	
	public boolean scanTagInDevice(int doc, int from, int to){
		return this.tag_in_device_list.contains(new Triple(doc, from,to));
	}*/
	
	private ArrayList<Tag> tag_list = new ArrayList<Tag>();
	
	public Tag getTag(String id){
		for(int i=0;i<tag_list.size();i++){
			if(tag_list.get(i).getId().compareTo(id)==0){
				return tag_list.get(i);
			}
		}
		return null;
	}
	
	public Tag getTagByIndex(int index){
		return this.tag_list.get(index);
	}
	
	public int getTagAmount(){
		return tag_list.size();
	}
	
	/*public Triple getTriple(int index, int index2){
		return tag_list.get(index).getTriple(index2);
	}*/
	
	public String getId(int index){
		return tag_list.get(index).getId();
	}
	
	public void addTag(Tag tag){
		tag_list.add(tag);
	}
	
	public boolean removeTag(String id){
		for(int i=0;i<tag_list.size();i++){
			if(tag_list.get(i).getId().compareTo(id)==0){
				tag_list.remove(i);
				return true;
			}
		}
		return false;
	}

}
