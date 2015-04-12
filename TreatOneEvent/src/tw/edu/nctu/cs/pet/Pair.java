package tw.edu.nctu.cs.pet;

public class Pair {
	private int a;
	private int b;
	
	public Pair(int c, int d){
		this.a = c;
		this.b = d;
	}
	
	public int getFrom(){
		return this.a;
	}
	
	public int getTo(){
		return this.b;
	}
}
