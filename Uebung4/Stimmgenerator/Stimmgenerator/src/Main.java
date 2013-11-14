import java.awt.Insets;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;


public class Main {
	
	public static String kerg13 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv";
	public static String kerg09 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg2009.csv";
	public static String bewerber13 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/Tab23_Wahlbewerber_a.csv";
	public static String bewerber09 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/wahlbewerber.csv";
	public static String landesliste09 = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/landeslisten.csv";
	public static String bew2land = "/home/tamel/Workspace/DBProjekt/Uebung4/Daten/listenplaetze.csv";
	
	
	public static void main(String[] args){
		init();
//		writeToDB();
		try{
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost:9912/wis";
			Connection con = DriverManager.getConnection(url,"wis","wis");
//			writePartei(con);
//			writeKandidat(con);
			writeDirektKandidat(con);
			writeListe(con);
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
			System.out.println("Dateien wahlbewerber.csv, landeslisten.csv, listenplaetze.csv");
			System.out.println("Erstelle Kandidaten");
			ReadKandidaten09();
			BundeslandContainer.map.size();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void ReadBundeslaenderWahlkreiseUndParteien13() throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(kerg13), "ISO-8859-15"));
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
				b.einwohner2013 = Integer.parseInt(arr[3]);
				BundeslandContainer.put(id, b);
				for(Wahlkreis wk:tmp)
					wk.bundesland = b;
				tmp.clear();
			}
			else if(!arr[1].equals("Bundesgebiet")){
				int nummer = Integer.parseInt(arr[0]);
				Wahlkreis wk = new Wahlkreis(nummer, 2013, arr[1]);
				WahlkreisContainer.put(nummer, 2013, wk);
				tmp.add(wk);
			}
		}
		reader.close();
	}
	
	public static void ReadWahlkreiseUndParteien09() throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(kerg09), "ISO-8859-15"));
		String line = reader.readLine();
		String[] arr = line.split(";|,|\\t");
		for(int i=6;i<arr.length;i+=2)
			ParteiContainer.get(arr[i]);
		while((line = reader.readLine())!=null){
			arr = line.split(";|,|\\t");
			if(!arr[2].equals("99") && !arr[1].equals("Bundesgebiet")){
				int nummer = Integer.parseInt(arr[0]);
				Wahlkreis wk = new Wahlkreis(nummer, 2009, arr[1]);
				int blID = Integer.parseInt(arr[2]);
				wk.bundesland = BundeslandContainer.get(blID);
				WahlkreisContainer.put(nummer, 2009, wk);
			}
			if(arr[2].equals("99")){
				Bundesland b = BundeslandContainer.get(arr[1]);
				b.einwohner2009 = Integer.parseInt(arr[3]);
			}
		}
		reader.close();
	}
	
	public static void ReadKandidaten13() throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bewerber13), "ISO-8859-15"));
		String line = reader.readLine();
		while((line=reader.readLine())!=null){
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
	
	public static void ReadKandidaten09() throws Exception{
		BufferedReader bewerber = new BufferedReader(new InputStreamReader(new FileInputStream(bewerber09), "ISO-8859-15"));
		BufferedReader landeslisten = new BufferedReader(new InputStreamReader(new FileInputStream(bew2land), "ISO-8859-15"));
		String bewLine = bewerber.readLine();
		String landLine = landeslisten.readLine();
		landLine = landeslisten.readLine();
		String[] landArr = landLine.split(";");
		while((bewLine = bewerber.readLine())!=null){
			String arr[] = bewLine.split(";|,|\\t");
			String name = arr[0] + " " + arr[1] + " " + arr[2];
			name = name.trim();
			Kandidat k = new Kandidat(name);
			Partei p = ParteiContainer.get(arr[3]);
			if(p==null)
				System.err.println("Partei nicht gefunden für Kandidat " + name);
			if(arr[5].equals(landArr[1])){
				k.listenplatz = Integer.parseInt(landArr[2]);
				Bundesland b = getLand(landArr[0]);
				if(b==null)
					System.err.println("Bundesland nicht gefunden für Kandidat " + name);
				k.land = b;
				landLine = landeslisten.readLine();
				if(landLine != null)
					landArr = landLine.split(";");
			}
		}
		bewerber.close();
		landeslisten.close();
	}
	
	public static Bundesland getLand(String nr) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(landesliste09));
		String line = reader.readLine();
		while((line = reader.readLine()) != null){
			String arr[] = line.split(";|,|\\t");
			if(arr[0].equals(nr)){
				Bundesland b = BundeslandContainer.get(arr[1]);
				reader.close();
				return b;
			}
		}
		reader.close();
		return null;
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
	
	public static void writeToDB(){
		try{
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://cyberfuzzie.suroot.com/wis";
			Connection con = DriverManager.getConnection(url,"wis","wis");
			writeBundeslaender(con);
			writeWahlkreise(con);
			writePartei(con);
			writeKandidat(con);
			writeListenplatz(con);
			writeEinwohner(con);
//			createStimmen(con);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void writeBundeslaender(Connection con) throws Exception{
		System.out.println("Schreibe Bundesländer");
		PreparedStatement insertBL = con.prepareStatement("insert into Bundesland values (?,?)");
		for(Bundesland b:BundeslandContainer.map.values()){
			insertBL.setInt(1, b.id);
			insertBL.setString(2, b.name);
			if(insertBL.executeUpdate() == 1)
				System.out.println("Bundesland " + b.name + " erfolgreich hinzugefügt");
		}
	}
	
	public static void writeWahlkreise(Connection con) throws Exception{
		System.out.println("Schreibe Wahlkreise " + WahlkreisContainer.map.size());
		PreparedStatement insertWk = con.prepareStatement("insert into Wahlkreis values (?,?,?,?)");
		int sum = 0;
		for(Wahlkreis wk:WahlkreisContainer.map.values()){
			insertWk.setInt(1, wk.id);
			insertWk.setInt(2, wk.nummer);
			insertWk.setString(3, wk.name);
			insertWk.setInt(4, wk.jahr);
			insertWk.setInt(5, wk.bundesland.id);
			sum += insertWk.executeUpdate();
		}
		System.out.println(sum + " Wahlkreise eingetragen");
	}
	
	public static void writePartei(Connection con) throws Exception{
		System.out.println("Schreibe Parteien " + ParteiContainer.map.size());
		PreparedStatement insertP = con.prepareStatement("insert into Partei(name) values (?)");
		int sum = 0;
		for(Partei p:ParteiContainer.map.values()){
			insertP.setString(1, p.name);
			sum += insertP.executeUpdate();
		}
		System.out.println(sum + " Parteien eingetragen");
	}
	
	public static void writeKandidat(Connection con) throws Exception{
		System.out.println("Schreibe Kandidaten " + KandidatContainer.map.size());
		PreparedStatement insertK = con.prepareStatement("insert into Kandidat(parteiid,name) values (?,?)");
		PreparedStatement selectPID = con.prepareStatement("select parteiid from partei where name = ?");
		int sum = 0;
		for(Kandidat k:KandidatContainer.map.values()){
			selectPID.setString(1, k.partei.name);
			ResultSet res = selectPID.executeQuery();
			res.next();
			int id = res.getInt(1);
			insertK.setInt(1, id);
			insertK.setString(2, k.name);
			sum += insertK.executeUpdate();
			System.out.println(sum);
		}
		System.out.println(sum + " Kandiaten eingetragen");
	}
	
	
	
	public static void writeListenplatz(Connection con) throws Exception{
		System.out.println("Schreibe Listenplätze");
		PreparedStatement insertL = con.prepareStatement("insert into Listenplatz values (?,?,?,?)");
		int sum = 0;
		for(Kandidat k:KandidatContainer.map.values()){
			if(k.land != null){
				insertL.setInt(1, k.land.id);
				insertL.setInt(2, k.partei.id);
				insertL.setInt(3, k.listenplatz);
				insertL.setInt(4, k.id);
				sum += insertL.executeUpdate();
			}
		}
		System.out.println(sum + "Listeneinträge erstellt");
	}
	
	public static void writeEinwohner(Connection con) throws Exception{
		System.out.println("Schreibe Einwohnerzahlen");
		PreparedStatement insertE = con.prepareStatement("insert into Einwohnerzahl values (?,?,?,?)");
		int nextID = 0;
		int sum = 0;
		for(Bundesland b:BundeslandContainer.map.values()){
			insertE.setInt(1, ++nextID);
			insertE.setInt(2, b.einwohner2009);
			insertE.setInt(3, 2009);
			insertE.setInt(4, b.id);
			sum += insertE.executeUpdate();
			insertE.setInt(2, b.einwohner2013);
			insertE.setInt(3, 2013);
			sum += insertE.executeUpdate();
		}
		System.out.println(sum + " Einträge erstellt");
	}
	
	public static void writeListe(Connection con) throws Exception{
		Statement batch = con.createStatement();
		PreparedStatement selectKan = con.prepareStatement("select kandidatid,parteiid from kandidat where name = ?");
		PreparedStatement selectBu = con.prepareStatement("select bundeslandid from bundesland where name = ?");
		int sum = 0;
		for(Kandidat k:KandidatContainer.map.values()){
			if(k.land != null){
				selectBu.setString(1, k.land.name);
				ResultSet result = selectBu.executeQuery();
				result.next();
				int buID = result.getInt(1);
				selectKan.setString(1, k.name);
				result = selectKan.executeQuery();
				result.next();
				int kanID = result.getInt(1);
				int partDI = result.getInt(2);
				batch.addBatch("insert into listenplatz (kandidatid,parteiid,bundeslandid,nummer) values (" + kanID + "," + partDI + "," + buID + "," + k.listenplatz + ")");
				sum++;
//				System.out.println(sum);
				if(sum%100==0){
					batch.executeBatch();
					System.out.println(sum);
				}
			}
		}
		batch.executeBatch();
	}
	
	public static void writeDirektKandidat(Connection con) throws Exception{
		Statement batch = con.createStatement();
		PreparedStatement selectWKID = con.prepareStatement("select wahlkreisid from wahlkreis where wahlkreisnummer = ? and jahr = ?");
		PreparedStatement selectKID = con.prepareStatement("select k.kandidatid from kandidat k join partei p on k.parteiid = p.parteiid where k.name = ? and p.name = ?");
		int sum = 0;
		for(Kandidat k:KandidatContainer.map.values()){
			if(k.wahlkreis != null){
				selectWKID.setInt(1, k.wahlkreis.nummer);
				selectWKID.setInt(2, 2013);
				ResultSet result = selectWKID.executeQuery();
				result.next();
				int wkID = result.getInt(1);
				selectKID.setString(1, k.name);
				selectKID.setString(2, k.partei.name);
				result = selectKID.executeQuery();
				result.next();
				int kID = result.getInt(1);
				batch.addBatch("insert into direktkandidat (kandidatid,wahlkreisid) values (" + kID + "," + wkID + ")");
				sum++;
				if(sum%100==0){
					batch.executeBatch();
					System.out.println(sum);
				}
			}
		}
		batch.executeBatch();
	}
	
	public static void createStimmen(Connection con) throws Exception{
		int erst = 0;
		int zweit = 0;
		PreparedStatement insertE = con.prepareStatement("insert into Erststimme(kandidatid,kandidatid) values (?,?)");
		PreparedStatement insertZ = con.prepareStatement("insert into Zweitstimme(parteiid,wahlkreisid) values (?,?)");
		PreparedStatement selectWkID = con.prepareStatement("select wahlkreisid from wahlkreis where wahlkreisnummer = ? and jahr = ?");
		PreparedStatement selectParteiid = con.prepareStatement("select parteiid from partei where name = ?");
		PreparedStatement selectKanId = con.prepareStatement("select kandidatid from kandidat where name = ? and parteiid = ?");
		BufferedReader reader = new  BufferedReader(new FileReader(kerg13));
		String[] firstLine = reader.readLine().split(";|,|\\t");
		String line;
		while((line = reader.readLine()) != null){
			String[] arr = line.split(";|,|\\t");
			if(arr[2].equals("99") || arr[1].equals("Bundesgebiet"))
				continue;
			for(int i=6;i<arr.length;i+=2){
				selectWkID.setInt(1, parse(arr[0]));
				selectWkID.setInt(2, 2013);
				ResultSet result;
				result = selectWkID.executeQuery();
				result.next();
				int wkID = result.getInt(1);
				selectParteiid.setString(1, firstLine[i]);
				result = selectParteiid.executeQuery();
				result.next();
				int parteiID = result.getInt(1);
			}
		}
		reader.close();
		System.out.println("Erststimmen: " + erst + " Zweitstimmen: " + zweit + " eingefügt für Jahr 2013");
	}
	
	public static int parse(String str){
		if(str.equals(""))
			return 0;
		else
			return Integer.parseInt(str);
	}

}
