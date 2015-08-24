import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class XMLCreator {
	
	public static void create(ArrayList<Food> foods, ArrayList<UnknownDish> uds, ArrayList<Fact> fact_list){
	try {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(DataLoader.xml_root);
		doc.appendChild(rootElement);

		Element document = doc.createElement("document");
		rootElement.appendChild(document);
		
		// facts elements
		Element facts = doc.createElement(DataLoader.xml_fact_list);
		document.appendChild(facts);

		for(Fact ff : fact_list){
			if(ff.getClass().toString().equals("class Food"))
				appendFood(doc, facts,(Food)ff);
			else
				appendUnknownDish(doc, facts, (UnknownDish)ff);
		}
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		//StreamResult result = new StreamResult(new File("C:\\file.xml"));

		// Output to console for testing
		StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		//System.out.println("File saved!");

	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
	}
	
	private static void appendFood(Document doc, Element facts, Food f){
		Element dish = doc.createElement(DataLoader.xml_dish);
		dish.setAttribute(DataLoader.xml_attr_factId, f.factID+"");
		dish.setAttribute(DataLoader.xml_attr_pos, f.pos_start+"");
		dish.setAttribute(DataLoader.xml_attr_len, f.char_length+"");
		facts.appendChild(dish);
		Element name = doc.createElement(DataLoader.xml_dish_name);
		name.setAttribute("val", f.name);
		dish.appendChild(name);
		Element size = doc.createElement(DataLoader.xml_dish_size);
		size.setAttribute("val", f.count);
		dish.appendChild(size);
		Element measure = doc.createElement(DataLoader.xml_dish_unit);
		measure.setAttribute("val", f.measure);
		dish.appendChild(measure);
	}
	
	private static void appendUnknownDish(Document doc, Element facts, UnknownDish ud){
		Element udish = doc.createElement(DataLoader.xml_un_dish);
		udish.setAttribute(DataLoader.xml_attr_factId, ud.factID+"");
		udish.setAttribute(DataLoader.xml_attr_pos, ud.pos_start+"");
		udish.setAttribute(DataLoader.xml_attr_len, ud.char_length+"");
		facts.appendChild(udish);
		Element name = doc.createElement(DataLoader.xml_un_dish_name);
		name.setAttribute("val", ud.name);
		udish.appendChild(name);
	}

}
