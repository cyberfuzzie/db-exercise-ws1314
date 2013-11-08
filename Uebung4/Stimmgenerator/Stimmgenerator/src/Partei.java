
public class Partei {
	
	static int nextID = 0;
	
	int id;
	String name;
	
	
	public Partei(String name){
		this.name = name;
		id = ++nextID;
	}

}
