import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static boolean isTest = false;
	public static boolean isDebug = false;
	
	public static void main(String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("test")){
			isTest = true;
			Log.t("Running in test mode...");
		}
		FoodParser fp;
		fp = new FoodParser();
		DataLoader dl = new DataLoader(fp);
		dl.loadConfig();
		dl.loadResources();
		BufferedReader br;
		br = new BufferedReader(new InputStreamReader(System.in));
		
			try {
				String s = br.readLine();
				if(s != null)
					fp.processPhrase(s);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

}
