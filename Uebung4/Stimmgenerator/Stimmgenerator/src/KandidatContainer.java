import java.util.Hashtable;


public class KandidatContainer {
	
	static Hashtable<String, Kandidat> map = new Hashtable<String, Kandidat>();
	
	public static Kandidat get(String name, int jahr){
		Kandidat k = map.get(name + jahr);
		return k;
	}
	
	public static void put(Kandidat k, int jahr){
		String key = k.name + jahr;
		map.put(key, k);
	}

}
