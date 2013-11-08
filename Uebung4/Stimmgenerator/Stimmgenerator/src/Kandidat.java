
public class Kandidat {
	
	static int nextID = 0;
	
	int id;
	String name;
	Wahlkreis wahlkreis;
	int listenplatz;
	Bundesland land;
	Partei partei;
	
	public Kandidat(String name){
		this.name = name;
		id = ++nextID;
	}

}
