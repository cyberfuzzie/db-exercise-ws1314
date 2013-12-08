import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.TreeMap;


public class Generate09 {

	
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
	
	public static void generateStimmsummen(Connection con) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg2009.csv"), "ISO-8859-15"));
		String[] firstLine = reader.readLine().split("\\t");
		int stop = firstLine.length-1;
		String sqlIEM = "with tmp as(select generate_series(4533,7978) as kandidatid) " + 
					"insert into summeerststimmen(kandidatid,wahlkreisid,anzahlstimmen) " + 
					"select k.kandidatid, w.wahlkreisid, ? " + 
					"from kandidat k, partei p, direktkandidat d, wahlkreis w " + 
					"where k.parteiid = p.parteiid and k.kandidatid = d.kandidatid and d.wahlkreisid = w.wahlkreisid " + 
					"and w.jahr = 2009 and w.wahlkreisnummer = ? and p.name = ? and k.kandidatid not in (select * from tmp)";
		String sqlIEO = "with tmp as(select generate_series(4533,7978) as kandidatid) " + 
					"insert into summeerststimmen(kandidatid, wahlkreisid, anzahlstimmen) " +
					"select k.kandidatid, w.wahlkreisid, ?" + 
					"from kandidat k, direktkandidat d, wahlkreis w " + 
					"where k.kandidatid = d.kandidatid and d.wahlkreisid = w.wahlkreisid " +
					"and k.parteiid is null and w.jahr = 2009 and w.wahlkreisnummer = ? and k.name = ? and k.kandidatid not in (select * from tmp)";
		String sqlIZ = "insert into summezweitstimmen(wahlkreisid,parteiid,anzahlstimmen) " +
						"select w.wahlkreisid, p.parteiid, ? " +
						"from wahlkreis w, partei p " + 
						"where w.jahr = 2009 and w.wahlkreisnummer = ? and p.name = ?";
		PreparedStatement iem = con.prepareStatement(sqlIEM);
		PreparedStatement ieo = con.prepareStatement(sqlIEO);
		PreparedStatement iz = con.prepareStatement(sqlIZ);
		String line;
		int countE = 0;
		int countZ = 0;
		while((line=reader.readLine()) != null){
			int tmpE = 0;
			int tmpZ = 0;
			String[] arr = line.split("\\t");
			int wkN = parse(arr[0]);
			for(int i=6;!arr[i].equals("Stop");i+=2){
				String partei = firstLine[i];
				int erststimmen = parse(arr[i]);
				int zweitstimmen = parse(arr[i+1]);
				if(erststimmen!=0){
					iem.setInt(1, erststimmen);
					iem.setInt(2, wkN);
					iem.setString(3, partei);
					tmpE += iem.executeUpdate();
				}
				if(zweitstimmen!=0){
					iz.setInt(1, zweitstimmen);
					iz.setInt(2, wkN);
					iz.setString(3, partei);
					tmpZ += iz.executeUpdate();
				}
			}
			if(arr.length>stop){
				for(int i=stop+1;i<arr.length;i+=2){
					int erststimmen = parse(arr[i-1]);
					String name = arr[i];
					ieo.setInt(1, erststimmen);
					ieo.setInt(2, wkN);
					ieo.setString(3, name);
					tmpE += ieo.executeUpdate();
				}
			}
			System.out.println("Wahlkreis " + wkN + " E: " + tmpE + " Z: " + tmpZ);
			countE += tmpE;
			countZ += tmpZ;
		}
		System.out.println(countE + " Erststimmen " + countZ + " Zweitstimmen");
		iem.close();
		ieo.close();
		iz.close();
		reader.close();
	}
	
	public static void generateStimmsummen(Connection con, int wkNummer) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg2009.csv"), "ISO-8859-15"));
		String[] firstLine = reader.readLine().split("\\t");
		int stop = firstLine.length-1;
		String sqlIEM = "with tmp as(select generate_series(4533,7978) as kandidatid) " + 
					"insert into summeerststimmen(kandidatid,wahlkreisid,anzahlstimmen) " + 
					"select k.kandidatid, w.wahlkreisid, ? " + 
					"from kandidat k, partei p, direktkandidat d, wahlkreis w " + 
					"where k.parteiid = p.parteiid and k.kandidatid = d.kandidatid and d.wahlkreisid = w.wahlkreisid " + 
					"and w.jahr = 2009 and w.wahlkreisnummer = ? and p.name = ? and k.kandidatid not in (select * from tmp)";
		String sqlIEO = "with tmp as(select generate_series(4533,7978) as kandidatid) " + 
					"insert into summeerststimmen(kandidatid, wahlkreisid, anzahlstimmen) " +
					"select k.kandidatid, w.wahlkreisid, ?" + 
					"from kandidat k, direktkandidat d, wahlkreis w " + 
					"where k.kandidatid = d.kandidatid and d.wahlkreisid = w.wahlkreisid " +
					"and k.parteiid is null and w.jahr = 2009 and w.wahlkreisnummer = ? and k.name = ? and k.kandidatid not in (select * from tmp)";
		String sqlIZ = "insert into summezweitstimmen(wahlkreisid,parteiid,anzahlstimmen) " +
						"select w.wahlkreisid, p.parteiid, ? " +
						"from wahlkreis w, partei p " + 
						"where w.jahr = 2009 and w.wahlkreisnummer = ? and p.name = ?";
		PreparedStatement iem = con.prepareStatement(sqlIEM);
		PreparedStatement ieo = con.prepareStatement(sqlIEO);
		PreparedStatement iz = con.prepareStatement(sqlIZ);
		String line;
		int countE = 0;
		int countZ = 0;
		while((line=reader.readLine()) != null){
			int tmpE = 0;
			int tmpZ = 0;
			String[] arr = line.split("\\t");
			int wkN = parse(arr[0]);
			if(wkN!=wkNummer)
				continue;
			for(int i=6;!arr[i].equals("Stop");i+=2){
				String partei = firstLine[i];
				int erststimmen = parse(arr[i]);
				int zweitstimmen = parse(arr[i+1]);
				if(erststimmen!=0){
					iem.setInt(1, erststimmen);
					iem.setInt(2, wkN);
					iem.setString(3, partei);
					tmpE += iem.executeUpdate();
				}
				if(zweitstimmen!=0){
					iz.setInt(1, zweitstimmen);
					iz.setInt(2, wkN);
					iz.setString(3, partei);
					tmpZ += iz.executeUpdate();
				}
			}
			if(arr.length>stop){
				for(int i=stop+1;i<arr.length;i+=2){
					int erststimmen = parse(arr[i-1]);
					String name = arr[i];
					ieo.setInt(1, erststimmen);
					ieo.setInt(2, wkN);
					ieo.setString(3, name);
					tmpE += ieo.executeUpdate();
				}
			}
			System.out.println("Wahlkreis " + wkN + " E: " + tmpE + " Z: " + tmpZ);
			countE += tmpE;
			countZ += tmpZ;
		}
		System.out.println(countE + " Erststimmen " + countZ + " Zweitstimmen");
		iem.close();
		ieo.close();
		iz.close();
		reader.close();
	}
	
	public static void generateDirektkandidaten(Connection con) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/wahlbewerber2009_mod.csv"), "UTF-8"));
		String sql = "with tmp as(select generate_series(4533,7978) as kandidatid) " +
					"insert into direktkandidat(kandidatid,wahlkreisid) select k.kandidatid, w.wahlkreisid " +
					"from kandidat k,wahlkreis w, partei p where k.parteiid = p.parteiid and k.name = ? and " + 
					"w.wahlkreisnummer = ? and w.jahr = 2009 and p.name = ? and k.kandidatid not in (select * from tmp)";
		String sql1 = "with tmp as(select generate_series(4533,7978) as kandidatid) " +
					"insert into direktkandidat(kandidatid,wahlkreisid) select k.kandidatid, w.wahlkreisid " +
					"from kandidat k,wahlkreis w where k.name = ? and " + 
					"w.wahlkreisnummer = ? and w.jahr = 2009 and k.parteiid is null and k.kandidatid not in (select * from tmp)";
		PreparedStatement mp = con.prepareStatement(sql);
		PreparedStatement op = con.prepareStatement(sql1);
		String line = reader.readLine();
		int count = 0;
		while((line=reader.readLine())!=null){
			String[] arr = line.split("\\t");
			int wknummer = parse(arr[4]);
			if(wknummer==0)
				continue;
			String name = arr[0] + " " + arr[1];
			String partei = arr[3];
			if(partei.equals("")){
				op.setString(1,name);
				op.setInt(2, wknummer);
				count += op.executeUpdate();
			}
			else{
				mp.setString(1, name);
				mp.setInt(2, wknummer);
				mp.setString(3, partei);
				count += mp.executeUpdate();
			}
		}
		System.out.println(count);
		mp.close();
		op.close();
		reader.close();
	}
	
	public static void generateKandidaten09(Connection con) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/wahlbewerber2009_mod.csv"), "UTF-8"));
		String sql = "select * from kandidat k, partei p where k.parteiid = p.parteiid and k.name = ? and p.name = ? and kandidatid < 4533";
		String sql1 = "select * from kandidat where name = ? and parteiid is null and kandidatid < 4533";
		String sql2 = "insert into kandidat(parteiid,name) select parteiid, ? from partei where name = ?";
		String sql3 = "insert into kandidat(name) values(?)";
		PreparedStatement sm = con.prepareStatement(sql);
		PreparedStatement so = con.prepareStatement(sql1);
		PreparedStatement im = con.prepareStatement(sql2);
		PreparedStatement io = con.prepareStatement(sql3);
		String line = reader.readLine();
		int count  = 0;
		while((line=reader.readLine())!=null){
			String[] arr = line.split("\\t");
			String name = arr[0] + " " + arr[1];
			String partei = arr[3];
			System.out.println(name + " " + partei);
			ResultSet r;
			if(partei.equals("")){
				so.setString(1, name);
				r = so.executeQuery();
			}
			else{
				sm.setString(1, name);
				sm.setString(2, partei);
				r = sm.executeQuery();
			}
			if(!r.next()){
				if(partei.equals("")){
					io.setString(1, name);
					count += io.executeUpdate();
				}
				else{
					im.setString(1, name);
					im.setString(2, partei);
					count += im.executeUpdate();
				}
			}
			r.close();	
		}
		System.out.println(count);
		sm.close();
		so.close();
		im.close();
		io.close();
		reader.close();
	}
	
	
	public static void generateWahlkreise09(Connection con) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/kerg2009.csv"), "ISO-8859-15"));
		String line = reader.readLine();
		String sql = "insert into wahlkreis(bundeslandid,wahlkreisnummer,name,jahr) values(?,?,?,2009)";
		PreparedStatement s = con.prepareStatement(sql);
		int count = 0;
		while((line=reader.readLine()) != null){
			String[] arr = line.split("\\t");
			s.setInt(1, parse(arr[2]));
			s.setInt(2, parse(arr[0]));
			s.setString(3, arr[1]);
			System.out.println("Bundesland " + parse(arr[2]) + " Nummer " + parse(arr[0]) + " Name " + arr[1]);
			count += s.executeUpdate();
		}
		System.out.println(count + " Wahlkreise erstellt");
		reader.close();
	}
	
	
	public static int parse(String str){
		if(str.equals(""))
			return 0;
		else
			return Integer.parseInt(str);
	}
	
	public static HashMap<KanPar,String> mapKandidaten(Connection con,HashMap<String, Integer> pID) throws Exception{
		HashMap<KanPar, String> ret = new HashMap<KanPar, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/tamel/Workspace/DBProjekt/Uebung4/Daten/wahlbewerber2009_mod.csv"), "UTF-8"));
		String sql = "select name from kandidat where (name = ? or name = ?) and parteiid = ? order by kandidatid";
		String sql1 = "select name from kandidat where (name = ? or name = ?) and parteiid is null order by kandidatid";
		PreparedStatement sm = con.prepareStatement(sql);
		PreparedStatement so = con.prepareStatement(sql1);
		String line = reader.readLine();
//		int lineCount = 1;
		while((line=reader.readLine())!=null){
//			lineCount++;
//			if(lineCount<3332)
//				continue;
			String[] arr = line.split("\\t");
			String name = arr[0] + " " + arr[1];
			String partei = arr[3];
			ResultSet r;
			if(partei.equals("")){
				so.setString(1, name);
				so.setString(2, " " + name);
				r = so.executeQuery();
			}
			else{
				sm.setString(1, name);
				sm.setString(2, " " + name);
				sm.setInt(3, pID.get(arr[3]));
				r = sm.executeQuery();
			}
			System.out.println(name + " " + partei);
			r.next();
			ret.put((new KanPar(name, arr[3])), r.getString(1));
		}
		so.close();
		sm.close();
		reader.close();
		return ret;
	}
	
	public static HashMap<String, Integer> getParteiid(Connection con) throws Exception{
		String sql = "select * from partei";
		Statement s = con.createStatement();
		ResultSet r = s.executeQuery(sql);
		HashMap<String, Integer> ret = new HashMap<String,Integer>();
		while(r.next()){
			ret.put(r.getString(2), r.getInt(1));
		}
		r.close();
		s.close();
		return ret;
	}
	
	public static String getName(HashMap<KanPar, String> map,String name, String partei){
		KanPar tmp = new KanPar(name,partei);
		for(KanPar kp:map.keySet())
			if(kp.equals(tmp))
				return map.get(kp);
		return null;
	}
	
	public static class KanPar{
		public String name;
		public String partei;
		
		public KanPar(String name,String partei){
			this.name = name;
			this.partei = partei;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof KanPar){
				boolean ne = name.equals(((KanPar) obj).name);
				boolean np = partei.equals(((KanPar) obj).partei);
				return ne && np;
			}
			else
				return false;
		}
	}
}
