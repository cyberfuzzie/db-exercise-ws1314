
public class Wahlkreis {
	
	static int nextID = 0;
	
	int id;
	int nummer;
	int jahr;
	String name;
	Bundesland bundesland;
	
	
	public Wahlkreis(int nummer, int jahr, String name){
		this.nummer = nummer;
		this.jahr = jahr;
		this.name = name;
		id = ++nextID;
	}

}
