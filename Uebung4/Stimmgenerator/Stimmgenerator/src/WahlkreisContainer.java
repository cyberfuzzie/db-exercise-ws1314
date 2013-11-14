import java.util.Hashtable;


public class WahlkreisContainer {
	
	static Hashtable<Integer, Wahlkreis> map = new Hashtable<Integer, Wahlkreis>();
	
	public static Wahlkreis get(int nummer, int jahr){
		int key = nummer * 10000 + jahr;
		Wahlkreis w = map.get(key);
		return w;
	}
	
	public static void put(int nummer, int jahr, Wahlkreis w){
		int key = nummer * 10000 + jahr;
		map.put(key, w);
	}

}
