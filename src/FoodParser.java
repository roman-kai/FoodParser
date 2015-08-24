import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class FoodParser {

	InputStream modelIn = null;
	private String posbinPath = "en-pos-maxent.bin";
	int longestFood = 1;
	int longestMeasure = 1;
	int factCount = 0;
	POSModel model;
	POSTaggerME tagger;
	int phraseParsed[];
	
	ArrayList<Food> foods;
	ArrayList<Measure> measures;
	ArrayList<UnknownDish> uds;
	String []phraseParts;
	String []tags;
	HashSet<String> db = new HashSet<String>();
	HashSet<String> dbm = new HashSet<String>();
	HashSet<String> h_db = new HashSet<String>();
	HashSet<String> h_dbm = new HashSet<String>();
	ArrayList<Fact> facts;

	public void load() {
		try {
			modelIn = new FileInputStream(posbinPath);
			model = new POSModel(modelIn);
		} catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		tagger = new POSTaggerME(model);
	}

	public void fillFood(List<String> foods){
		for(String s : foods){
			if(s.contains(" ")){
				String [] foodParts = s.split(" ");
				h_db.add(singularize(foodParts[0]).toLowerCase());
				if(foodParts.length > longestFood)
					longestFood = foodParts.length;
			}else{
				h_db.add(singularize(s).toLowerCase());
			}
			db.add(singularizeWithSpaces(s).toLowerCase());
		}
	}
	
	public void fillMeasures(List<String> measures) {
		for (String m : measures) {
			if (m.contains(" ")) {
				String[] foodParts = m.split(" ");
				h_dbm.add(singularize(foodParts[0]).toLowerCase());
				if (foodParts.length > longestMeasure)
					longestMeasure = foodParts.length;
			}else{
				h_dbm.add(singularize(m).toLowerCase());
			}
			dbm.add(singularizeWithSpaces(m).toLowerCase());
		}
	}
	
	public int calcStartPos(int k){
		int summ = 0;
		for(int i = 0; i<k;i++){
			summ += phraseParts[i].length() + 1;
		}
		return summ;
	}
	
	public int calcLength(int s, int l){
		int summ = 0;
		for(int i = s; i<s+l;i++){
			summ += phraseParts[i].length() + 1;
		}
		if(summ > 1) // extract last " " 
			summ--;
		return summ;
	}
	
	
	
	public void printSet(){
		for (String s : db) {
		    Log.m(s);
		}
	}
	
	public boolean setContains(String s){
		return db.contains(s);
	}
	

	public String[] tag(String[] sent) {
		if(tagger == null)
			Log.m("Tagger == null");
		return tagger.tag(sent);
	}
	
	public void preprocessPhrase1(String phrase) {
		factCount = 0;
		foods = new ArrayList<Food>();
		measures = new ArrayList<Measure>();
		// find multiword foods in database
		String[] words = phrase.toLowerCase().split(" ");
		phraseParsed = new int[words.length];
		Arrays.fill(phraseParsed, 0);
		phraseParts = words;
		tags = tag(phraseParts);
		// Log.m("Splitting [" + phrase + "]");
		// Log.m("WordsStart");
		// for(String s : words)
		// Log.m(s);
		// Log.m("WOrdsEND");
		for (int i = 0; i < words.length; i++) {
			boolean found = false;
			boolean foundm = false;
			boolean foundpart = false;
			int count = 1;
			for (int j = 1; j < longestFood + 1; j++) {
				if (i + j > words.length) {
					Log.m("BREAK: i=" + i + "j=" + j);
					break;
				}
				String joined = joinStringArray(words, i, j, " ").toLowerCase();
				joined = singularizeWithSpaces(joined);
				if (1 == j && !h_db.contains(joined) && !h_dbm.contains(joined)){ // word is not in database
					found = foundm = false;
					break;
				}
				if (db.contains(joined)) {
					found = true;
					count = j;
				}
				if (dbm.contains(joined)) {
					foundm = true;
					count = j;
				}
			}
			Log.m("off loop, found = " + found);
			if (found) {
				Log.m("Found db word @:" + i + " length = " + count);
				Food f = new Food(joinStringArray(words, i, count, " "), i,
						count, factCount);
				f.pos_start = calcStartPos(i);
				f.char_length = calcLength(i, count);
				foods.add(f);
				factCount++;
				setParsed(i, count);
				i += count -1;
			}
			if (foundm) {
				Log.m("Found dbm word @:" + i + " length =" + count);
				measures.add(new Measure(joinStringArray(words, i, count, " "),
						i, count));
				i += count -1;

			}
		}
	}
	
	public void setParsed(int start, int length){
		for(int i = start; i < start + length; i++)
			phraseParsed[i] = 1;
	}
	
	public void processPhrase(String phrase){
		preprocessPhrase1(phrase);
		parsePOS();
	}
	
	
	
	public String joinStringArray(String [] array, int startIndex, int count, String delimiter){
		String res = "";
		int length = array.length;
		if(startIndex < 0 || startIndex > length || startIndex + count > length || count < 1){
			return "Bad join strings call";
		}
		for(int i=startIndex; i < startIndex + count; i++){
			if(i != startIndex)
				res += delimiter;
			res += array[i];
		}
		return res;
	}
	
	public void findMeasuresCD(){
		for(Measure m: measures){
			int length = 0;
			for(int i = m.start-1; i >= 0; i--){
				if(m.start == 0)
					break;
				if(tags[i].equalsIgnoreCase("CD"))
					length++;
				else
					break;
			}
			if(length > 0){
				m.count = joinStringArray(phraseParts, m.start - length, length, " ");
				Log.m("Found measure CD: "+ m.name + "/" + m.count);
				m.start = m.start - length;
				m.length += length;
				//setParsed(m.start, length + m.length);
				m.pos_start = calcStartPos(m.start);
				m.char_length = calcLength(m.start, m.length);
			}else{
				Log.m("Len in find measures was 0");
			}
		}
	}
	
	public void collapseMeasures(){
		for(Food f : foods){
			for(Measure m : measures){
				if(f.start - (m.start + m.length) == 1)
					if(tags[m.start + m.length].equalsIgnoreCase("IN")){
						f.measure = m.name;
						f.count = m.count;
						setParsed(m.start, 1+ m.length);
						f.start = m.start;
						f.pos_start = m.pos_start;
						Log.m("cl in collapse");
						m.assigned = true;
						f.char_length = calcLength(f.start, m.length + 1) + f.char_length;
					}else{
						Log.m("Tag in Collapse = " + tags[m.start + m.length + 1]);
					}
			}
		}
	}
	
	public void collapseMeasuresB() {
		for (Food f : foods) {
			for (Measure m : measures) {
				if (f.start + f.length == m.start && !m.assigned  && !tags[m.start + m.length].equalsIgnoreCase("IN")) {
					f.measure = m.name;
					f.count = m.count;
					setParsed(m.start, m.length);
					//f.start = m.start;
					//f.pos_start = m.pos_start;
					Log.m("Tybe B collapse");
					f.char_length = calcLength(f.start, m.length + 1)
							+ f.char_length;
				}else{
					Log.m("not equals for : " + m.name);
				}
			}
		}
	}
	
	public void findCountsWOMeasures(){
		for(Food f : foods){
			if(f.measure.equalsIgnoreCase("none")){
				int length = 0;
				for(int i = f.start-1; i >= 0; i--){
					if(tags[i].equalsIgnoreCase("CD"))
						length++;
					else
						break;
				}
				if(length > 0){
					f.count = joinStringArray(phraseParts, f.start - length, length, " ");
					setParsed(f.start - length, length);
					f.start = f.start - length;
					f.pos_start = calcStartPos(f.start);
					f.char_length = calcLength(f.start, length + f.length);
				}else{
					Log.m("Len was = 0");
				}
			}
		}
	}
	
	public String singularize(String string){
		String res  ="";
		res = Inflector.singularize(string);
		
		return res;
	}
	
	public String singularizeForIndex(int k){
		String res = "";
		
		if(tags[k].equals("NNS"))
			res = Inflector.singularize(phraseParts[k]);
		else
			res = phraseParts[k];
		
		return res;
	}
	
	public String singularizeWithSpaces(String string){
		String res = "";
		String[] tags;
		String[] parts = string.split(" ");
		tags = tag(parts);
		for(int i =0; i < parts.length; i++){
			if(tags[i].equals("NNS"))
				parts[i] = Inflector.singularize(parts[i]);
			if(i != 0){
				res += " ";
			}
			res += parts[i];
		}
		return res;
		
	}
	
	public void createUnknowns(){
		int count=0;
		uds = new ArrayList<UnknownDish>();
		UnknownDish ud;
		for(int i = 0; i < phraseParsed.length; i++){
			if(phraseParsed[i] == 0 && !tags[i].equalsIgnoreCase("CC")){
				count++;
			}else{
				if(count > 0){
					ud = new UnknownDish(factCount, joinStringArray(phraseParts, i-count, count, " "));
					ud.start = i-count;
					ud.pos_start = calcStartPos(ud.start);
					ud.char_length = calcLength(ud.start, count);
					Log.m("in Unkwnows: start=" + ud.start + "count =" + count );
					uds.add(ud);
					factCount++;
					count = 0;
				}
			}	
		}
		if(count > 0){
			ud = new UnknownDish(factCount, joinStringArray(phraseParts, phraseParsed.length-count, count, " "));
			ud.start = phraseParsed.length-count;
			ud.pos_start = calcStartPos(ud.start);
			ud.char_length = calcLength(ud.start, count);
			Log.m("in Unkwnows: start=" + ud.start + "count =" + count );
			uds.add(ud);
			factCount++;
		}
	}
	
	public void calculateFactIDs(){
		facts = new ArrayList<Fact>();
		facts.addAll(foods);
		facts.addAll(uds);
		int min_index = 0;
		int factID = 0;
		while(factCount != 0){
			int min_start = phraseParts.length; // init min as max
			Fact current = facts.get(0);
			for(Fact f : facts){
				if(f.start < min_start && f.start >= min_index && f.factID == -1){
					min_start = f.start;
					current = f;
				}
			}
			min_index += current.length;
			current.factID = factID;
			factID++;
			factCount--;
		}
		Collections.sort(facts, new FactComparator());
	}
	
	public class FactComparator implements Comparator<Fact> {
	    public int compare(Fact f1, Fact f2) {
	        return f1.factID - f2.factID;
	    }
	}
	
	public void parsePOS(){
		findMeasuresCD();
		collapseMeasures();
		findCountsWOMeasures();
		collapseMeasuresB();
		createUnknowns();
		calculateFactIDs();
		XMLCreator.create(foods, uds, facts);
	}
	
	public void setPosbinPath(String path){
		this.posbinPath = path;	
	}

}
