import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;


public class Generate13 {
	
	public static void main(String[] args){
		Connection con = null;
		try{
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost:5512/wis";
			con = DriverManager.getConnection(url,"wis","wis");
			generateStimmsummen(con);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(con!=null){
				try{
					con.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void generateKandidaten(Connection con) throws Exception{
		BufferedReader Kanreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/Tab23_Wahlbewerber_a.csv"), "ISO-8859-15"));
		BufferedReader Kergreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		String line = Kanreader.readLine();
		String sql = "insert into kandidat(parteiid,name) select parteiid, ? from partei where name = ?";
		PreparedStatement s = con.prepareStatement(sql);
		int count = 0;
		while((line = Kanreader.readLine()) != null){
			String[] arr = line.split(";|,");
			String kname = arr[1].trim() + " " + arr[0].trim();
			String pname = arr[3].trim();
			s.setString(1, kname);
			s.setString(2, pname);
			count += s.executeUpdate();
		}
		String sql1 = "insert into kandidat(name) values(?)";
		PreparedStatement s1 = con.prepareStatement(sql1);
		int stop = Kergreader.readLine().split("\\t").length -1;
		while((line = Kergreader.readLine()) != null){
			String[] arr = line.split("\\t");
			if(arr.length > stop){
				for(int i=stop+1;i<arr.length;i+=2){
					s1.setString(1, arr[i].trim());
					count += s1.executeUpdate();
				}
			}
		}
		Kanreader.close();
		Kergreader.close();
		System.out.println(count + " Kandidaten erstellt");
	}
	
	
	public static void generatePartei(Connection con) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/Tab23_Wahlbewerber_a.csv"), "ISO-8859-15"));
		Vector<String> parteien = new Vector<String>();
		String line = reader.readLine();
		while((line = reader.readLine()) != null){
			String name = line.split(";|,")[3].trim();
			if(!parteien.contains(name))
				parteien.add(name);
		}
		PreparedStatement s = con.prepareStatement("insert into partei(name) values(?)");
		int count = 0;
		for(String str:parteien){
			s.setString(1, str);
			count += s.executeUpdate();
		}
		System.out.println(count + " Parteien erstellt");
		reader.close();
	}
	
	public static void generateDirektKandidaten(Connection con) throws Exception{
		BufferedReader Kanreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/Tab23_Wahlbewerber_a.csv"), "ISO-8859-15"));
		BufferedReader Kergreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		String line = Kanreader.readLine();
		String sql = "insert into direktkandidat(kandidatid,wahlkreisid) select k.kandidatid,w.wahlkreisid from kandidat k,wahlkreis w,partei p where k.parteiid = p.parteiid and k.name = ? and w.jahr = 2013 and w.wahlkreisnummer = ? and p.name = ?";
		PreparedStatement s = con.prepareStatement(sql);
		int count = 0;
		while((line=Kanreader.readLine()) != null){
			String[] arr = line.split(";|,");
			int wkNummer = parse(arr[4].trim());
			if(wkNummer == 0)
				continue;
			String name = arr[1].trim() + " " + arr[0].trim();
			String parteiname = arr[3].trim();
			s.setString(1, name);
			s.setInt(2, wkNummer);
			s.setString(3, parteiname);
			count += s.executeUpdate();
		}
		Kanreader.close();
		String sql1 = "insert into direktkandidat(kandidatid,wahlkreisid) select k.kandidatid, w.wahlkreisid from kandidat k,wahlkreis w where k.name = ? and w.wahlkreisnummer = ? and w.jahr = 2013";
		PreparedStatement s1 = con.prepareStatement(sql1);
		int stop = Kergreader.readLine().split("\\t").length -1;
		while((line=Kergreader.readLine()) != null){
			String[] arr = line.split("\\t");
			if(arr.length > stop){
				int wkNummer = parse(arr[0].trim());
				for(int i=stop+1;i<arr.length;i+=2){
					String kname = arr[i].trim();
					s1.setString(1, kname);
					s1.setInt(2, wkNummer);
					count += s1.executeUpdate();
				}
			}
		}
		Kergreader.close();
		System.out.println(count + " direktkandidateneinträge generiert");
	}
	
	public static void generateListenplätze(Connection con) throws Exception{
		BufferedReader Kanreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/Tab23_Wahlbewerber_a.csv"), "ISO-8859-15"));
		String line = Kanreader.readLine();
		String sql = "insert into listenplatz(kandidatid,parteiid,bundeslandid,nummer,jahr) " + 
					"select k.kandidatid, p.parteiid, b.bundeslandid, ?, 2013 " +
					"from kandidat k, partei p, bundesland b " + 
					"where k.parteiid = p.parteiid and b.name = ? and k.name = ? and p.name = ?";
		PreparedStatement s = con.prepareStatement(sql);
		int count = 0;
		while((line=Kanreader.readLine())!=null){
			String[] arr = line.split(",|;");
			if(arr.length > 5){
				String parteiname = arr[3].trim();
				String kname = arr[1].trim() + " " + arr[0].trim();
				String bname = ShortToLong(arr[5].trim());
				int platz = parse(arr[6].trim());
				s.setInt(1, platz);
				s.setString(2, bname);
				s.setString(3, kname);
				s.setString(4, parteiname);
				count += s.executeUpdate();
			}
		}
		Kanreader.close();
		System.out.println(count + " Listenplätze generiert");
	}
	
	public static void generateStimmsummen(Connection con) throws Exception{
		BufferedReader Kergreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		String[] firstLine = Kergreader.readLine().split("\\t");
		int stop = firstLine.length -1;
		HashMap<String, Integer> mapPID = getParteiID(firstLine, con);
		String line;
		String sql = "select wahlkreisid from wahlkreis where wahlkreisnummer = ? and jahr = 2013";
		String sql2 = "select k.kandidatid from kandidat k, direktkandidat d where k.kandidatid = d.kandidatid and d.wahlkreisid = ? and k.parteiid = ?";
		String sql1 = "insert into summeerststimmen(kandidatid,wahlkreisid,anzahlstimmen) values(?,?,?)";
		String sql3 = "insert into summezweitstimmen(parteiid,wahlkreisid,anzahlstimmen) values(?,?,?)";
		String sql4 = "select kandidatid from kandidat where name = ? and parteiid is null";
		PreparedStatement sWkID = con.prepareStatement(sql);
		PreparedStatement sKID = con.prepareStatement(sql2);
		PreparedStatement sKID2 = con.prepareStatement(sql4);
		PreparedStatement insertE = con.prepareStatement(sql1);
		PreparedStatement insertZ = con.prepareStatement(sql3);
		while((line = Kergreader.readLine()) != null){
			int countE = 0;
			int countZ = 0;
			String[] arr = line.split("\\t");
			sWkID.setInt(1, parse(arr[0].trim()));
			ResultSet result = sWkID.executeQuery();
			if(!result.next()){
				System.err.println("Wahlkreisid von Wahlkreis " + parse(arr[0].trim()) + " nicht gefunden");
				continue;
			}
			int wkID = result.getInt(1);
			for(int i=6;!arr[i].equals("Stop");i+=2){
				Integer pID = mapPID.get(firstLine[i].trim());
				if(pID==null){
					System.err.println("Partei " + firstLine[i].trim() + " nicht gefunden");
					continue;
				}
				int summeErststimme = parse(arr[i]);
				int summeZweitstimme = parse(arr[i+1]);
				if(summeZweitstimme!=0){
					insertZ.setInt(1, pID);
					insertZ.setInt(2, wkID);
					insertZ.setInt(3, summeZweitstimme);
					countZ += insertZ.executeUpdate();
				}
				sKID.setInt(1, wkID);
				sKID.setInt(2, pID);
				result = sKID.executeQuery();
				if(!result.next()){
					if(parse(arr[i])!=0)
						System.err.println("Kandidat für Wahlkreis " + parse(arr[0].trim()) + " und Partei " + firstLine[i].trim() + " nicht gefunden");
					continue;
				}
				int kID = result.getInt(1);
				
				if(summeErststimme!=0){
					insertE.setInt(1, kID);
					insertE.setInt(2, wkID);
					insertE.setInt(3, summeErststimme);
					countE += insertE.executeUpdate();
				}
			}
			if(arr.length > stop){
				for(int i=stop+1;i<arr.length;i+=2){
					int stimmen = parse(arr[i-1].trim());
					String name = arr[i].trim();
					sKID2.setString(1, name);
					result = sKID2.executeQuery();
					if(!result.next()){
						System.err.println("Kandidat für Wahlkreis " + parse(arr[0].trim()) + " nicht gefunden");
						continue;
					}
					int kID = result.getInt(1);
					insertE.setInt(1, kID);
					insertE.setInt(2, wkID);
					insertE.setInt(3, stimmen);
					countE += insertE.executeUpdate();
				}
			}
			System.out.println("Wahlkreis " + parse(arr[0]) + " generiert, Einträge summeerststimmen: " + countE + " Einträge summezweitstimme: " + countZ + " erstellt");
		}
		Kergreader.close();
	}
	
	public static void generateStimmsummen(Connection con, int wkNummer) throws Exception{
		BufferedReader Kergreader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		String[] firstLine = Kergreader.readLine().split("\\t");
		int stop = firstLine.length -1;
		HashMap<String, Integer> mapPID = getParteiID(firstLine, con);
		String line;
		String sql = "select wahlkreisid from wahlkreis where wahlkreisnummer = ? and jahr = 2013";
		String sql2 = "select k.kandidatid from kandidat k, direktkandidat d where k.kandidatid = d.kandidatid and d.wahlkreisid = ? and k.parteiid = ?";
		String sql1 = "insert into summeerststimmen(kandidatid,wahlkreisid,anzahlstimmen) values(?,?,?)";
		String sql3 = "insert into summezweitstimmen(parteiid,wahlkreisid,anzahlstimmen) values(?,?,?)";
		String sql4 = "select kandidatid from kandidat where name = ? and parteiid is null";
		PreparedStatement sWkID = con.prepareStatement(sql);
		PreparedStatement sKID = con.prepareStatement(sql2);
		PreparedStatement sKID2 = con.prepareStatement(sql4);
		PreparedStatement insertE = con.prepareStatement(sql1);
		PreparedStatement insertZ = con.prepareStatement(sql3);
		int countE = 0;
		int countZ = 0;
		while((line = Kergreader.readLine()) != null){
			String[] arr = line.split("\\t");
			if(parse(arr[0])!=wkNummer)
				continue;
			sWkID.setInt(1, parse(arr[0].trim()));
			ResultSet result = sWkID.executeQuery();
			if(!result.next()){
				System.err.println("Wahlkreisid von Wahlkreis " + parse(arr[0].trim()) + " nicht gefunden");
				continue;
			}
			int wkID = result.getInt(1);
			for(int i=6;!arr[i].equals("Stop");i+=2){
				Integer pID = mapPID.get(firstLine[i].trim());
				if(pID==null){
					System.err.println("Partei " + firstLine[i].trim() + " nicht gefunden");
					continue;
				}
				int summeErststimme = parse(arr[i]);
				int summeZweitstimme = parse(arr[i+1]);
				if(summeZweitstimme!=0){
					insertZ.setInt(1, pID);
					insertZ.setInt(2, wkID);
					insertZ.setInt(3, summeZweitstimme);
					countZ += insertZ.executeUpdate();
				}
				sKID.setInt(1, wkID);
				sKID.setInt(2, pID);
				result = sKID.executeQuery();
				if(!result.next()){
					if(parse(arr[i])!=0)
						System.err.println("Kandidat für Wahlkreis " + parse(arr[0].trim()) + " und Partei " + firstLine[i].trim() + " nicht gefunden");
					continue;
				}
				int kID = result.getInt(1);
				
				if(summeErststimme!=0){
					insertE.setInt(1, kID);
					insertE.setInt(2, wkID);
					insertE.setInt(3, summeErststimme);
					countE += insertE.executeUpdate();
				}
			}
			if(arr.length > stop){
				for(int i=stop+1;i<arr.length;i+=2){
					int stimmen = parse(arr[i-1].trim());
					String name = arr[i].trim();
					sKID2.setString(1, name);
					result = sKID2.executeQuery();
					if(!result.next()){
						System.err.println("Kandidat für Wahlkreis " + parse(arr[0].trim()) + " nicht gefunden");
						continue;
					}
					int kID = result.getInt(1);
					insertE.setInt(1, kID);
					insertE.setInt(2, wkID);
					insertE.setInt(3, stimmen);
					countE += insertE.executeUpdate();
				}
			}
			System.out.println("Wahlkreis " + parse(arr[0]) + " generiert, Einträge summeerststimmen: " + countE + " Einträge summezweitstimme: " + countZ + " erstellt");
		}
		Kergreader.close();
	}
	
	public static HashMap<String,Integer> getParteiID(String[] arr,Connection con) throws Exception{
		HashMap<String, Integer> ret = new HashMap<String,Integer>();
		String sql = "select parteiid from partei where name = ?";
		PreparedStatement s = con.prepareStatement(sql);
		for(int i=6;!arr[i].equals("Stop");i+=2){
			String name = arr[i].trim();
			s.setString(1, name);
			ResultSet result = s.executeQuery();
			if(!result.next()){
				System.err.println("Partei nicht gefunden");
				continue;
			}
			int id = result.getInt(1);
			ret.put(name, id);
		}
		return ret;
	}
	
	public static int parse(String str){
		if(str.equals(""))
			return 0;
		else
			return Integer.parseInt(str);
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
