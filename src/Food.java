
public class Food extends Fact{

	public String name;
	
	public int pos_start;
	public int char_length;
	public String count = "one";
	public String measure = "none";
	
	public Food(String name, int start, int length, int id){
		super(id);
		this.name = name;
		this.start = start;
		this.length = length;
	}
}
