import java.util.Hashtable;


public class BundeslandContainer {
	
	static Hashtable<Integer, Bundesland> map = new Hashtable<Integer,Bundesland>();
	
	public static Bundesland get(int id){
		return map.get(id);
	}
	
	public static Bundesland get(String name){
		name = name.replaceAll("\\W", "");
		for(Bundesland b:map.values())
			if(b.name.replaceAll("\\W", "").equals(name))
				return b;
		return null;
	}
	
	public static void put(int id,Bundesland b){
		map.put(id, b);
	}
	

}
