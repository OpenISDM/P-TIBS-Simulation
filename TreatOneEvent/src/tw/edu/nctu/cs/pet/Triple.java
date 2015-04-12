package tw.edu.nctu.cs.pet;

public class Triple {
	private int a;
	private int b;
	private int c;
	
	public Triple(int d, int e, int f){
		this.a = d;
		this.b = e;
		this.c = f;
	}
	
	public int getDoc(){
		return this.a;
	}
	
	public int getFrom(){
		return this.b;
	}
	
	public int getTo(){
		return this.c;
	}
}
