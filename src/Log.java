
public class Log {

	public static void m(String string){
		if(Main.isDebug)
			System.out.println(string);
	}
	
	public static void t(String string){
		if(Main.isTest)
			System.out.println(string);
	}
}
