import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;

import com.opencsv.CSVReader;

public class Server {
	
	private final static String space = ", ";
	
	private final Connection conn;
	private CSVReader reader;
	
	
	public Server() throws Exception	{
		Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection("jdbc:postgresql://192.168.1.11:5432/transportDatabase");
        
        reader = new CSVReader(new FileReader("Stops.csv"));
        ArrayList<Integer> columns = new ArrayList<>();
        columns.add(0);
        columns.add(1);
        columns.add(4);
        columns.add(10);
        columns.add(19);
        columns.add(29);
        columns.add(30);
        
        String [] nextLine = reader.readNext();
        while((nextLine = reader.readNext()) != null){
        	readLineCSV(nextLine, columns);
        }
        
        
        
	}
	
	public void readAll(Connection c) throws SQLException	{
		Statement stmt = c.createStatement();
		String sql = "SELECT * FROM trains;";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next())	{
			int train_id = rs.getInt("Train_id");
			String origin = rs.getString("Origin");
			String destination = rs.getString("Destination");
			Time departure_time = rs.getTime("Departure_time");
			Time arrival_time = rs.getTime("Arrival_time");
			String noOfCarriages = rs.getString("numberOfCarriages");
			System.out.println( "ID = " + train_id );
            System.out.println( "Origin = " + origin );
            System.out.println( "Destination = " + destination );
            System.out.println( "Departure Time = " + departure_time );
            System.out.println( "Arrival Time = " + arrival_time );
            System.out.println( "Carriages = " + noOfCarriages );
            System.out.println();
		}
		rs.close();
		stmt.close();
		
	}
	
	public String[] readLineCSV(String[] nextLine, ArrayList<Integer> columns) throws Exception	{
		String[] selectedColumns = new String[columns.size()];
		int i = 0;
		while(i < selectedColumns.length)	{
			selectedColumns[i] = nextLine[columns.get(i)];
			i++;
		}
        populateBusCodes(selectedColumns);
		return nextLine;
	}
	
	public void populateBusCodes(String[] selectedColumns) throws Exception	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < selectedColumns.length; i++) {
			   sb.append("'" + selectedColumns[i].trim() +"',");
		}
		String values = sb.toString().substring(0, sb.length() - 1);
		
		if(checkIfTableExists("busCodes"))	{
			dropTable("busCodes");
		}
		
		Statement stmt = conn.createStatement();
		String sql = "INSERT into busCodes VALUES (" + values + ");";
		System.out.println(sql);
		stmt.execute(sql);
	}

	public static void start() {
//		readLineCSV(columns);
	      
	      try {
	         Server s = new Server();
	         
//	         readCSV("Stops.csv");
	         
//	         Statement stmt = c.createStatement();
//	 		String sql = "SELECT * FROM trains;";
//	 		ResultSet rs = stmt.executeQuery(sql);
//	 		
//	 		while(rs.next())	{
//	 			int train_id = rs.getInt("Train_id");
//	 			String origin = rs.getString("Origin");
//	 			String destination = rs.getString("Destination");
//	 			Time departure_time = rs.getTime("Departure_time");
//	 			Time arrival_time = rs.getTime("Arrival_time");
//	 			String noOfCarriages = rs.getString("numberOfCarriages");
//	 			System.out.println( "ID = " + train_id );
//	             System.out.println( "Origin = " + origin );
//	             System.out.println( "Destination = " + destination );
//	             System.out.println( "Departure Time = " + departure_time );
//	             System.out.println( "Arrival Time = " + arrival_time );
//	             System.out.println( "Carriages = " + noOfCarriages );
//	             System.out.println();
//	 		}
//	 		rs.close();
//	 		stmt.close();
	 		
	 		
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");
	   }
		
	public void dropTable (String tableName) throws Exception{
		Statement stmt = conn.createStatement();
		String sql = "DROP TABLE IF EXISTS " + tableName + ";";
		stmt.execute(sql);
		stmt.close();
	}
	
	public boolean checkIfTableExists(String tableName) throws Exception	{
		Statement stmt = conn.createStatement();
		String sql = "select * from pg_tables where schemaname='" + tableName + "';";
		ResultSet rs = stmt.executeQuery(sql);
		
		stmt.close();
		return true;
		
	}
	
}
