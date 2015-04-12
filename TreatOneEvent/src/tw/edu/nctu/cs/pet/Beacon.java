package tw.edu.nctu.cs.pet;

public class Beacon {
	private double ttl_time = 0.0;
	private int ttl_hop = 0;
	
	public boolean update(double time_flow, int hop){
		this.ttl_time -= time_flow;
		this.ttl_hop -= hop;
		if(this.ttl_time<=0.0){
			return false;
		}else{
			return true;
		}
	}
	
	public int getHop(){
		return this.ttl_hop;
	}
	
	public double getTime(){
		return this.ttl_time;
	}
	
	public Beacon(){
	}
	
	public Beacon(double time, int hop){
		this.ttl_time = time;
		this.ttl_hop = hop;
	}
}
