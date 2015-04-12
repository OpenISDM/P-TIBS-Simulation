package tw.edu.nctu.cs.pet;

import java.util.ArrayList;

public class TagManager {
	private ArrayList<ArrayList<ArrayList<Integer>>> tag_in_device = new ArrayList<ArrayList<ArrayList<Integer>>>();
	private int device_amount = main.getDeviceAmount();
	
	public TagManager(){
		ArrayList<ArrayList<Integer>> tmp = null;
		ArrayList<Integer> tmp2 = null;
		for(int i=0;i<device_amount;i++){
			tmp = new ArrayList<ArrayList<Integer>>(); 
			for(int j=0;j<device_amount;j++){
				tmp2 = new ArrayList<Integer>();
				tmp.add(tmp2);
			}
			tag_in_device.add(tmp);
		}
	}
	
	public void addTag(int device, int from, int to){
		tag_in_device.get(device).get(from).add(to);
	}
	
	public String checkAndSendTag(int device1, int device2){
		ArrayList<ArrayList<Integer>> device1_tag = tag_in_device.get(device1);
		ArrayList<ArrayList<Integer>> device2_tag = tag_in_device.get(device2);
		int choice_tag_from = -1;
		int choice_tag_to = -1;
		boolean send = false;
		int direct = 0;	//0: NO Send ; 1: device1->device2 ; -1: device2->device1
		
		for(int i=0;i<device_amount;i++){
			ArrayList<Integer> tmp = device1_tag.get(i);
			ArrayList<Integer> tmp2 = device2_tag.get(i);
			for(int j=0;j<tmp.size();j++){
				if(tmp2.contains(tmp.get(j))){
					choice_tag_from = i;
					choice_tag_to = tmp.get(j);
					send = true;
					direct = 1;
					break;
				}
			}
			if(send){
				break;
			}
		}
		
		if(!send){
			for(int i=0;i<device_amount;i++){
				ArrayList<Integer> tmp = device1_tag.get(i);
				ArrayList<Integer> tmp2 = device2_tag.get(i);
				for(int j=0;j<tmp2.size();j++){
					if(tmp.contains(tmp2.get(j))){
						choice_tag_from = i;
						choice_tag_to = tmp2.get(j);
						send = true;
						direct = -1;
						break;
					}
				}
				if(send){
					break;
				}
			}
		}
		
		String re = direct + " " + choice_tag_from + " " + choice_tag_to;
		return re;
	}
}
