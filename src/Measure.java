
public class Measure {

	public String name;
	public int start;
	public int length;
	boolean assigned = false;
	public int pos_start;
	public int char_length;
	public String count = "one";
	
	public Measure(String name, int start, int length){
		this.name = name;
		this.start = start;
		this.length = length;
	}

}
