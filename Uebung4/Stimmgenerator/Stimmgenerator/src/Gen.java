import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;


public class Gen {
	
	public static void main(String[] args){
		try{
			kandidatenOhnePartei();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void kandidatenOhnePartei() throws Exception {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:9912/wis";
		Connection con = DriverManager.getConnection(url,"wis","wis");
		PreparedStatement partei = con.prepareStatement("insert into partei(name) values (?)");
		PreparedStatement parteiid = con.prepareStatement("select parteiid from partei where name = ?");
		PreparedStatement kandidat = con.prepareStatement("insert into kandidat(parteiid,name) values(?,?)");
		PreparedStatement kandidatid = con.prepareStatement("select kandidatid from kandidat where parteiid = ?");
		PreparedStatement wahlkreisid = con.prepareStatement("select wahlkreisid from wahlkreis where wahlkreisnummer = ?");
		PreparedStatement direkt = con.prepareStatement("insert into direktkandidat(kandidatid,wahlkreisid) values (?,?)");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg.csv"), "ISO-8859-15"));
		int stopIndex = reader.readLine().split(";|,|\\t").length - 1;
		String line;
		int count = 0;
		ResultSet r;
		while((line = reader.readLine()) != null){
			String[] arr = line.split(";|,|\\t");
			if(arr.length > stopIndex){
				count++;
				int pCount = 0;
				int kCount = 0;
				int dCount = 0;
				System.out.println("Wahlkreisnummer: " + arr[0]);
				for(int i = stopIndex + 1;i<arr.length;i+=2){
					partei.setString(1, arr[i]);
					pCount += partei.executeUpdate();
					parteiid.setString(1, arr[i]);
					r = parteiid.executeQuery();
					if (!r.next()) {
						System.err.println("Parteiid für Kandidat " + arr[i] + " nicht gefunden");
						continue;
					}
					int pID = r.getInt(1);
					kandidat.setInt(1, pID);
					kandidat.setString(2, arr[i]);
					kCount += kandidat.executeUpdate();
					kandidatid.setInt(1, pID);
					r = kandidatid.executeQuery();
					if(!r.next()){
						System.err.println("Kandidatid für Kandidat " + arr[i] + " nicht gefunden");
						continue;
					}
					int kID = r.getInt(1);
					wahlkreisid.setInt(1,Integer.parseInt(arr[0]));
					r = wahlkreisid.executeQuery();
					if(!r.next()){
						System.err.println("Wahlkreis " + Integer.parseInt(arr[0]) + " nicht gefunden");
						continue;
					}
					int wID = r.getInt(1);
					direkt.setInt(1, kID);
					direkt.setInt(2, wID);
					dCount += direkt.executeUpdate();
				}
				System.out.println(pCount + " Parteien erstellt " + kCount + " Kandidaten erstellt " + dCount + " direkteinträge erstellt");
			}
		}
		System.out.println("Anzahl: " + count);
		reader.close();
	}
	
}
