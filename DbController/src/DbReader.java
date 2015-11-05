import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

public class DbReader {
	public static void main(String args[]) {
	      Connection c = null;
	      
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager.getConnection("jdbc:postgresql://192.168.1.11:5432/transportDatabase");
	         
	         
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
	 		
	 		c.close();
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");
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
	
	
}
