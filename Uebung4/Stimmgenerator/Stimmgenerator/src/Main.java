import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class Main {
	
	public static String kerg13 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv";
	public static String kerg09 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg2009.csv";
	public static String bewerber13 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/Tab23_Wahlbewerber_a.csv";
	
	
	public static void main(String[] args){
		init();
		
	}
	
	public static void init(){
		try{
			System.out.println("Datei kerg13.csv");
			System.out.println("Erstelle Bundesländer, Wahlkreise und Pareien");
			ReadBundeslaenderWahlkreiseUndParteien13();
			System.out.println("Datei kerg09.csv");
			System.out.println("Erstelle Wahlkreise und Pareien");
			ReadWahlkreiseUndParteien09();
			System.out.println("Datei Tab23_Wahlbewerber_a.csv");
			System.out.println("Erstelle Kandidaten");
			ReadKandidaten13();
			KandidatContainer.map.size();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void ReadBundeslaenderWahlkreiseUndParteien13() throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(kerg13));
		String line = reader.readLine();
		String[] arr = line.split(";|,|\\t");
		for(int i=6;i<arr.length;i+=2)
			ParteiContainer.get(arr[i]);
		ArrayList<Wahlkreis> tmp = new ArrayList<Wahlkreis>();
		while((line = reader.readLine())!=null){
			arr = line.split(";|,|\\t");
			if(arr[2].equals("99")){
				int id = Integer.parseInt(arr[0]);
				Bundesland b = new Bundesland(id, arr[1]);
				BundeslandContainer.put(id, b);
				for(Wahlkreis wk:tmp)
					wk.bundesland = b;
				tmp.clear();
			}
			else if(!arr[0].equals("99")){
				int nummer = Integer.parseInt(arr[0]);
				Wahlkreis wk = new Wahlkreis(nummer, 2013, arr[1]);
				WahlkreisContainer.put(nummer, 2013, wk);
				tmp.add(wk);
			}
		}
		reader.close();
	}
	
	public static void ReadWahlkreiseUndParteien09() throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(kerg09));
		String line = reader.readLine();
		String[] arr = line.split(";|,|\\t");
		for(int i=6;i<arr.length;i+=2)
			ParteiContainer.get(arr[i]);
		while((line = reader.readLine())!=null){
			arr = line.split(";|,|\\t");
			if(!arr[2].equals("99") && !arr[0].equals("99")){
				int nummer = Integer.parseInt(arr[0]);
				Wahlkreis wk = new Wahlkreis(nummer, 2009, arr[1]);
				int blID = Integer.parseInt(arr[2]);
				wk.bundesland = BundeslandContainer.get(blID);
				WahlkreisContainer.put(nummer, 2009, wk);
			}
		}
		reader.close();
	}
	
	public static void ReadKandidaten13() throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(bewerber13));
		String line = reader.readLine();
		int i = 0;
		while((line=reader.readLine())!=null){
			System.out.println(++i);
			String[] arr = line.split(";|,|\\t");
			String name = arr[1] + " " + arr[0];
			name = name.trim();
			Kandidat k = new Kandidat(name);
			KandidatContainer.put(k, 2013);
			k.partei = ParteiContainer.get(arr[3]);
			if(ParteiContainer.get(arr[3]) == null){
				System.err.println("Partei nicht gefunden für Kandidat " + name);
			}
			if(!arr[4].equals("")){
				Wahlkreis wk = WahlkreisContainer.get(Integer.parseInt(arr[4]), 2013);
				if(wk==null)
					System.err.println("Wahlkreis nicht gefunden für Kandidat " + name);
				k.wahlkreis = wk;
			}
			if(arr.length > 6 && !arr[5].equals("") && !arr[6].equals("")){
				Bundesland b = BundeslandContainer.get(ShortToLong(arr[5]));
				if(b==null)
					System.err.println("Bundesland nicht gefunden für Kandidat " + name);
				k.land = b;
				k.listenplatz = Integer.parseInt(arr[6]);
			}
		}
		reader.close();
	}
	
	
	public static String ShortToLong(String str){
		str = str.toLowerCase();
		if(str.equals("by"))
			return "Bayern";
		else if(str.equals("bw"))
			return "Baden-Württemberg";
		else if(str.equals("be"))
			return "Berlin";
		else if(str.equals("bb"))
			return "Brandenburg";
		else if(str.equals("hb"))
			return "Bremen";
		else if(str.equals("hh"))
			return "Hamburg";
		else if(str.equals("he"))
			return "Hessen";
		else if(str.equals("mv"))
			return "Mecklenburg-Vorpommern";
		else if(str.equals("ni"))
			return "Niedersachsen";
		else if(str.equals("nw"))
			return "Nordrhein-Westfalen";
		else if(str.equals("rp"))
			return "Rheinland-Pfalz";
		else if(str.equals("sl"))
			return "Saarland";
		else if(str.equals("sn"))
			return "Sachsen";
		else if(str.equals("st"))
			return "Sachsen-Anhalt";
		else if(str.equals("sh"))
			return "Schleswig-Holstein";
		else if(str.equals("th"))
			return "Thüringen";
		else
			return "notFound";
	}

}
