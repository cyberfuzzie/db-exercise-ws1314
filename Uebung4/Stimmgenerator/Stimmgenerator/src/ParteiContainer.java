import java.util.Hashtable;


public class ParteiContainer {
	
	static Hashtable<String, Partei> map = new Hashtable<String, Partei>();
	
	public static Partei get(String name){
		String key = name.replaceAll("\\W", "");
		Partei p = map.get(key);
		if(p==null){
			p = new Partei(name);
			map.put(key, p);
		}
		return p;
	}

}
