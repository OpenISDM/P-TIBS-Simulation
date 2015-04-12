package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class DeviceNumberInTag {
	
	private String tag_id = "";
	
	private ArrayList<Integer> tag_in_device_number = new ArrayList<Integer>();
	private ArrayList<Integer> tag_in_device_group_number = new ArrayList<Integer>();
	
	
	public void setTagId(String id){
		this.tag_id = id;
	}
	
	public boolean checkTagHaveToSend(int mac1, int mac2){
		return tag_in_device_number.contains(mac1) && !tag_in_device_number.contains(mac2);
	}
	
	public boolean checkDeviceNum(int mac){		//This Tag is in device(mac)?
		return tag_in_device_number.contains(mac);
	}
	
	public void addDeviceNum(int mac, int group){
		tag_in_device_number.add(mac);
		tag_in_device_group_number.add(group);
	}
	
	public void removeDeviceNum(int mac){
		tag_in_device_number.remove(tag_in_device_number.indexOf(mac));
		tag_in_device_group_number.remove(tag_in_device_number.indexOf(mac));
	}
	
	public DeviceNumberInTag(String id, int mac){
		this.tag_id = id;
		tag_in_device_number.add(mac);
	}
}
