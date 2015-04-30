package com.ibm.gaiandb.apps;

import java.util.List;

//import com.ibm.gaiandb.tools.SQLDerbyRunner;

public class InsertRowsBuffer {
	String insertQueryHeader = "insert into localSensorReadings values ";
	StringBuffer insertQuery = new StringBuffer (insertQueryHeader);
	int insertCount = 0;
	private int batchSize = 100;
	
//	static SQLDerbyRunner sdr;
	/**
	 * 
	 */
	public InsertRowsBuffer(int portToQuery, String hostToQuery, String databaseName, String databaseUser, String databasePassword) {
		
		String[] args = { "-h", hostToQuery, "-p", Integer.toString(portToQuery), "-usr", databaseUser, "-pwd", databasePassword, "" };

//		sdr = new SQLDerbyRunner();
//		sdr.processArgsWithoutClosingConnection(args);
		

	}
	
	
	public void insertRowsSoFar() {
		// TODO Auto-generated method stub
		if (insertCount > 0) {
			insertQuery.append(";");

			try {
//				sdr.processSQLs(insertQuery.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//reinitialise the query string for more rows
			insertQuery.setLength(insertQueryHeader.length());
			insertCount = 0;
		}

	}
	
	
	public void insertFinalRows() {
		// TODO Auto-generated method stub
		insertRowsSoFar();
	}
	public void add(String valuesText) {
		if (insertCount!=0){
			insertQuery.append(",");
		}
		insertQuery.append(valuesText);
		insertCount++;
		if (insertCount>= batchSize ) {
			insertRowsSoFar();
		}
		
	}

	
	

}
