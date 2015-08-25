import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DataLoader {
	
	String food_path = "none";
	String unit_path = "none";
	String bin_path = "none";
	
	String food_name = "food.db";
	String unit_name = "unit.db";
	String bin_name = "pos.bin";
	boolean filesPathTestingFailed = false;
	
	public static String xml_root = "fdo_objects";
	public static String xml_dish = "Dish";
	public static String xml_dish_unit = "Type";
	public static String xml_dish_size = "Size";
	public static String xml_dish_name = "Name";
	public static String xml_un_dish_name = "Value";
	public static String xml_un_dish = "UnknownDish";
	public static String xml_attr_len = "len";
	public static String xml_attr_pos = "pos";
	public static String xml_fact_list = "facts";
	public static String xml_attr_factId = "FactID";
	
	String c_xml_root = "xml_root";
	String c_xml_dish = "xml_dish";
	String c_xml_dish_unit = "xml_dish_unit";
	String c_xml_dish_size = "xml_dish_size";
	String c_xml_dish_name = "xml_dish_name";
	String c_xml_un_dish_name = "xml_dish_name";
	String c_xml_un_dish = "xml_un_dish";
	String c_xml_attr_len = "xml_attr_len";
	String c_xml_attr_pos = "xml_attr_pos";
	String c_xml_fact_list = "xml_fact_list";
	String c_xml_attr_factId = "xml_attr_factId";

	FoodParser fp;
	public DataLoader(FoodParser fp){
		this.fp = fp;
	}
	
	public void loadConfig(){
		try {

			File fXmlFile = new File("foodparser_config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
					
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList nList = doc.getElementsByTagName("path");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					if(eElement.getAttribute("name").equalsIgnoreCase(bin_name))
						bin_path = eElement.getAttribute("path");
					if(eElement.getAttribute("name").equalsIgnoreCase(food_name))
						food_path = eElement.getAttribute("path");
					if(eElement.getAttribute("name").equalsIgnoreCase(unit_name))
						unit_path = eElement.getAttribute("path");
				}
			}
			
			nList = doc.getElementsByTagName("result_xml_param");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_root))
						xml_root = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_dish))
						xml_dish = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_dish_unit))
						xml_dish_unit = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_dish_size))
						xml_dish_size = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_dish_name))
						xml_dish_name = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_un_dish_name))
						xml_un_dish_name = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_un_dish))
						xml_un_dish = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_attr_len))
						xml_attr_len = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_attr_pos))
						xml_attr_pos = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_fact_list))
						xml_fact_list = eElement.getAttribute("value");
					if(eElement.getAttribute("name").equalsIgnoreCase(c_xml_attr_factId))
						xml_attr_factId = eElement.getAttribute("value");
				}
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	}
	
	public void loadResources(){
		checkBin();
		fp.load();
		loadFood();
		loadUnits();
		if(filesPathTestingFailed)
			Log.t("Problem(s) occured while checking file paths");
		else
			Log.t("All files are located");
	}
	
	private void checkBin(){
		File bin = new File(bin_path);
		if(!bin.exists()){
			Log.t("en-pos-maxent.bin not found");
			filesPathTestingFailed = true;
		}
		else{
			fp.setPosbinPath(bin_path);
		}
		
	}
	
	private void loadFood(){
		try {
			List<String> lines = Files.readAllLines(Paths.get(food_path), Charset.defaultCharset());
			fp.fillFood(lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.t("food.db not found");
			filesPathTestingFailed = true;
		}
	}
	
	private void loadUnits(){
		try {
			List<String> lines = Files.readAllLines(Paths.get(unit_path), Charset.defaultCharset());
			fp.fillMeasures(lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.t("unit.db not found");
			filesPathTestingFailed = true;
		}
	}
}
