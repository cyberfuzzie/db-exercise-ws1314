import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class StimmenGenerator {
	
	
	public static void main(String[] args){
		try{
			generate();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void generate() throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		String[] firstLine = reader.readLine().split(";|,|\\t");
		int stop = firstLine.length -1 ;
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:9912/wis";
		Connection con = DriverManager.getConnection(url,"wis","wis");
		String line;
		String[] arr;
		PreparedStatement selectWkID = con.prepareStatement("select wahlkreisid from wahlkreis where wahlkreisnummer = ? and jahr = 2013");
		PreparedStatement selectP = con.prepareStatement("select parteiid from partei where name = ?");
		PreparedStatement selectKID = con.prepareStatement("select k.kandidatid from kandidat k join direktkandidat d on k.kandidatid = d.kandidatid where d.wahlkreisid = ? and k.parteiid = ?");
		PreparedStatement insertErststimmen = con.prepareStatement("with numbers as(select generate_series(1,?)), eintrag as(select ? as kandidatid, ? as wahlkreisid) insert into erststimme(kandidatid,wahlkreisid) select eintrag.* from eintrag,numbers");
		PreparedStatement insertZweitstimmen = con.prepareStatement("with numbers as(select generate_series(1,?)), eintrag as(select ? as parteiid, ? as wahlkreisid) insert into zweitstimme(parteiid,wahlkreisid) select eintrag.* from eintrag,numbers");
		PreparedStatement insertSumme = con.prepareStatement("insert into stimmsumme(parteiid,wahlkreisid,anzahlerststimmen,anzahlzweitstimmen) values (?,?,?,?)");
		while((line = reader.readLine()) != null){
			System.out.println("Nexter Eintrag");
			arr = line.split(";|,|\\t");
			int wkNummer = parse(arr[0]);
			selectWkID.setInt(1, wkNummer);
			ResultSet result = selectWkID.executeQuery();
			if(!result.next()){
				System.err.println("Wahlkreis " + wkNummer + " konnte nicht gefunden werden");
				continue;
			}
			int wkID = result.getInt(1);
			System.out.println("Wahlkreisid: " + wkID);
			for(int i=6;!arr[i].equals("Stop");i+=2){
				boolean erststimme = true;
				String parteiName = firstLine[i];
				selectP.setString(1, parteiName);
				result = selectP.executeQuery();
				if(!result.next()){
					System.err.println("Partei " + parteiName + " konnte nicht gefunden werden");
					continue;
				}
				int pID = result.getInt(1);
				System.out.println("Parteiid: " + pID);
				selectKID.setInt(1, wkID);
				selectKID.setInt(2, pID);
				result = selectKID.executeQuery();
				if(!result.next()){
					System.err.println("Kandidat nicht gefunden für Partei " + parteiName + " und Wahlkreis " + wkNummer);
					erststimme = false;
				}
				int erststimmen = parse(arr[i]);
				int zweitstimmen = parse(arr[i+1]);
				System.out.println("Erststimmen " + erststimmen + " Zweitstimmen " + zweitstimmen);
				if(erststimme && erststimmen!=0){
					int kID = result.getInt(1);
					System.out.println("Kandidatid: " + kID);
					insertErststimmen.setInt(1, erststimmen);
					insertErststimmen.setInt(2, kID);
					insertErststimmen.setInt(3, wkID);
					insertErststimmen.executeUpdate();
				}
				if(zweitstimmen != 0){
					insertZweitstimmen.setInt(1, zweitstimmen);
					insertZweitstimmen.setInt(2, pID);
					insertZweitstimmen.setInt(3, wkID);
					insertZweitstimmen.executeUpdate();
				}
				if(erststimmen != 0 || zweitstimmen != 0){
					insertSumme.setInt(1, pID);
					insertSumme.setInt(2, wkID);
					insertSumme.setInt(3, erststimmen);
					insertSumme.setInt(4, zweitstimmen);
					insertSumme.executeUpdate();
				}
			}
			if(arr.length > stop){
				for(int i = stop; i<arr.length;i+=2){
					int stimmen = parse(arr[i]);
					String name = arr[i+1];
					selectP.setString(1, name);
					result = selectP.executeQuery();
					if(!result.next()){
						System.err.println("Partei für Kandidat " + name + " nicht gefunden");
						continue;
					}
					int pID = result.getInt(1);
					selectKID.setInt(1, wkID);
					selectKID.setInt(2,pID);
					result = selectKID.executeQuery();
					if(!result.next()){
						System.err.println("Direktkandidateneintrag nicht gefunden, WKID " + wkID + " PID " + pID);
						continue;
					}
					int kID = result.getInt(1);
					insertErststimmen.setInt(1, stimmen);
					insertErststimmen.setInt(2, kID);
					insertErststimmen.setInt(3, wkID);
					insertErststimmen.executeUpdate();
					insertSumme.setInt(1, pID);
					insertSumme.setInt(2, wkID);
					insertSumme.setInt(3, stimmen);
					insertSumme.setInt(4, 0);
					insertSumme.executeUpdate();
				}
			}
		}
		reader.close();
	}
	
	public static void generate(int wahlkreisnummer) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		String[] firstLine = reader.readLine().split(";|,|\\t");
		int stop = firstLine.length -1 ;
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:9912/wis";
		Connection con = DriverManager.getConnection(url,"wis","wis");
		String line;
		String[] arr;
		PreparedStatement selectWkID = con.prepareStatement("select wahlkreisid from wahlkreis where wahlkreisnummer = ? and jahr = 2013");
		PreparedStatement selectP = con.prepareStatement("select parteiid from partei where name = ?");
		PreparedStatement selectKID = con.prepareStatement("select k.kandidatid from kandidat k join direktkandidat d on k.kandidatid = d.kandidatid where d.wahlkreisid = ? and k.parteiid = ?");
		PreparedStatement insertErststimmen = con.prepareStatement("with numbers as(select generate_series(1,?)), eintrag as(select ? as kandidatid, ? as wahlkreisid) insert into erststimme(kandidatid,wahlkreisid) select eintrag.* from eintrag,numbers");
		PreparedStatement insertZweitstimmen = con.prepareStatement("with numbers as(select generate_series(1,?)), eintrag as(select ? as parteiid, ? as wahlkreisid) insert into zweitstimme(parteiid,wahlkreisid) select eintrag.* from eintrag,numbers");
		PreparedStatement insertSumme = con.prepareStatement("insert into stimmsumme(parteiid,wahlkreisid,anzahlerststimmen,anzahlzweitstimmen) values (?,?,?,?)");
		while((line = reader.readLine()) != null){
			System.out.println("Nexter Eintrag");
			arr = line.split(";|,|\\t");
			int wkNummer = parse(arr[0]);
			if(wkNummer!=wahlkreisnummer)
				continue;
			selectWkID.setInt(1, wkNummer);
			ResultSet result = selectWkID.executeQuery();
			if(!result.next()){
				System.err.println("Wahlkreis " + wkNummer + " konnte nicht gefunden werden");
				continue;
			}
			int wkID = result.getInt(1);
			System.out.println("Wahlkreisid: " + wkID);
			for(int i=6;!arr[i].equals("Stop");i+=2){
				boolean erststimme = true;
				String parteiName = firstLine[i];
				selectP.setString(1, parteiName);
				result = selectP.executeQuery();
				if(!result.next()){
					System.err.println("Partei " + parteiName + " konnte nicht gefunden werden");
					continue;
				}
				int pID = result.getInt(1);
				System.out.println("Parteiid: " + pID);
				selectKID.setInt(1, wkID);
				selectKID.setInt(2, pID);
				result = selectKID.executeQuery();
				if(!result.next()){
					System.err.println("Kandidat nicht gefunden für Partei " + parteiName + " und Wahlkreis " + wkNummer);
					erststimme = false;
				}
				int erststimmen = parse(arr[i]);
				int zweitstimmen = parse(arr[i+1]);
				System.out.println("Erststimmen " + erststimmen + " Zweitstimmen " + zweitstimmen);
				if(erststimme && erststimmen!=0){
					int kID = result.getInt(1);
					System.out.println("Kandidatid: " + kID);
					insertErststimmen.setInt(1, erststimmen);
					insertErststimmen.setInt(2, kID);
					insertErststimmen.setInt(3, wkID);
					insertErststimmen.executeUpdate();
				}
				if(zweitstimmen != 0){
					insertZweitstimmen.setInt(1, zweitstimmen);
					insertZweitstimmen.setInt(2, pID);
					insertZweitstimmen.setInt(3, wkID);
					insertZweitstimmen.executeUpdate();
				}
				if(erststimmen != 0 || zweitstimmen != 0){
					insertSumme.setInt(1, pID);
					insertSumme.setInt(2, wkID);
					insertSumme.setInt(3, erststimmen);
					insertSumme.setInt(4, zweitstimmen);
					insertSumme.executeUpdate();
				}
			}
			if(arr.length > stop){
				for(int i = stop; i<arr.length;i+=2){
					int stimmen = parse(arr[i]);
					String name = arr[i+1];
					selectP.setString(1, name);
					result = selectP.executeQuery();
					if(!result.next()){
						System.err.println("Partei für Kandidat " + name + " nicht gefunden");
						continue;
					}
					int pID = result.getInt(1);
					selectKID.setInt(1, wkID);
					selectKID.setInt(2,pID);
					result = selectKID.executeQuery();
					if(!result.next()){
						System.err.println("Direktkandidateneintrag nicht gefunden, WKID " + wkID + " PID " + pID);
						continue;
					}
					int kID = result.getInt(1);
					insertErststimmen.setInt(1, stimmen);
					insertErststimmen.setInt(2, kID);
					insertErststimmen.setInt(3, wkID);
					insertErststimmen.executeUpdate();
					insertSumme.setInt(1, pID);
					insertSumme.setInt(2, wkID);
					insertSumme.setInt(3, stimmen);
					insertSumme.setInt(4, 0);
					insertSumme.executeUpdate();
				}
			}
		}
		reader.close();
	}
	
	public static int parse(String str){
		if(str.equals(""))
			return 0;
		else
			return Integer.parseInt(str);
	}

}
