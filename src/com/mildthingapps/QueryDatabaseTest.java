package com.mildthingapps;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryDatabaseTest {
	
	public static void printRS (ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        StringBuffer row = new StringBuffer("");
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            String name=rsmd.getColumnName(i);
            row.append(name+',');
        }           
        System.out.println(row.toString());
        
        while (rs.next()) {
   	
        	StringBuilder Datarow=new StringBuilder("");
            int numCols = rsmd.getColumnCount();
            
	            for ( int i=1; i <= numCols; i++ ) {
	            	
	            	String colvalue = rs.getString(i);
            	
	                if (colvalue == null) colvalue="-";
	                Datarow.append(colvalue+',');                       
	            }
            
	            System.out.println(Datarow);
        }//endwhile 
		
	}

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
		 Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		 Connection conn=DriverManager.getConnection("jdbc:ucanaccess://DB/Uniform2013.mdb"); 
		 Statement statemnt = conn.createStatement();
		 ResultSet rs = statemnt.executeQuery("select count(*) from customer");
		 printRS (rs);
		 rs = statemnt.executeQuery("select * from order");
		 printRS (rs);		 
//		 rs = statemnt.executeQuery("select * from product");
//		 printRS (rs);		
		 
		 //now insert a test order
		 statemnt.execute("insert into order values (0,1,1,\"2015-02-20 00:00:00.0\",\"2015-02-20 00:00:00.0\",30.0,1,123456,\"Paul Stone\",10,1)");
	 
		 rs = statemnt.executeQuery("select * from order");
		 printRS (rs);		 
  
	}

}
