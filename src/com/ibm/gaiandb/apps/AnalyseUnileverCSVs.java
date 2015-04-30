package com.ibm.gaiandb.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyseUnileverCSVs {
	
	// define the parameters for database connection and set them to their defaults.
	static int portToQuery = 6414;
	static String hostToQuery = "localhost";
	static String databaseName = "gaiandb";
	static String databaseUser = "gaiandb";
	static String databasePassword = "passw0rd";
	
	static String ingestFileRoot = null;
	static String siteName = null;  // The name of the pilot plant site.
	
	static final Pattern timestampColumnName = Pattern.compile("Zeitstempel (\\w*)");
	static final Pattern valueColumnName = Pattern.compile("Wert (\\w*)");
	
	static InsertRowsBuffer insertRowsBuffer = null;
	
	// Set up the necessary tables to allow the inserting of data.
	// The returned flag indicates whether the table is successfully initialised
	private static boolean initialiseTargetDatabase(){
		return false;
	}
	
	private static void processArgs (String[] args){
		List<String> argsList = Arrays.asList(args);
		for ( int i=0; i<args.length; i++ ) {
			final String arg = args[i];
			// port flag
			if ( "-p".equals( arg ) ) {
				String portString = args[++i];
				portToQuery = Integer.parseInt(portString);
				databaseName = "gaiandb"+portToQuery;
			}
			else if ( "-h".equals( arg ) ) {
				hostToQuery = args[++i];
			}
			else if ( "-d".equals( arg ) ) { 
				databaseName = args[++i]; 
			}
			else if ( "-s".equals( arg ) ) {
				siteName = args[++i];
			}
			else if ( "-usr".equals( arg ) ) {
				databaseUser = args[++i];
			}
			else if ( "-pwd".equals( arg ) ) {
				databasePassword = args[++i];
			}
			else {
				// We must have the file name now.. check to see if it is valid
				
				File testFile = new File(arg);
				if (testFile.isDirectory()||testFile.isFile()){
					ingestFileRoot = arg;
				} else {
					System.out.println("Error - " + arg + " is not a file or directory.");
			        System.exit(1); 
				}
			}
		}
		
		// Check that we have got all necessary parameters
		if (ingestFileRoot==null||siteName==null){
			if (ingestFileRoot==null)System.out.println("Error - You need to specify the file or directory.");
			if (siteName==null)System.out.println("Error - You need to specify the Site.");
			System.exit(1); 
		}
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// parse the parameters.
		processArgs(args);
		
		insertRowsBuffer = new InsertRowsBuffer(portToQuery, hostToQuery, databaseName, databaseUser, databasePassword);

		// parse the file(s)
		File testFile = new File(ingestFileRoot);
		if (testFile.isDirectory()) {
			scanDirectory(testFile);
		} else {
			ingestFile(testFile);	
		}
		
		insertRowsBuffer.insertFinalRows();

	}

	private static void ingestFile(File sourceFile) {
//		Logger.getAnonymousLogger().info("Ingesting "+sourceFile.getPath());

		// Find the machine name from the file name
		// File name example is "20140520_101615_Amixon_Ruberg_VNR_11_Start_20140519_Werte.csv" - date_time_machineName_VNR*.csv
		int machineNameStart = 16;
		int machineNameEnd = sourceFile.getName().indexOf("VNR")-2;

		String machineName = sourceFile.getName().substring(machineNameStart, machineNameEnd+1);
//		Logger.getAnonymousLogger().info("machineName: "+machineName);		

		int trialNameStart = machineNameEnd+2;
		int trialNameEnd = sourceFile.getName().indexOf("Start")-2;
		String trialName = sourceFile.getName().substring(trialNameStart, trialNameEnd+1);

		// Prepare to read the file
		FileInputStream fis = null;
		BufferedReader reader = null;
		try {
			fis = new FileInputStream(sourceFile);
			reader = new BufferedReader(new InputStreamReader(fis));

			// Get the first row from the file which will tell us which sensor readings follow.
			String line = reader.readLine();
			String lastLine = line;

			ArrayList<String> Sensors = null;
			// check that the header row is in the expected format 
			// which is pairs of timestamp/value columns, semicolon separated
			if (isExpectedFormat(line)){
//				Logger.getAnonymousLogger().info("Query file has a valid header line");
				// read the individual lines and interpret them as readings
//				Sensors = getSensorList(line);

				line = reader.readLine();
				boolean firstLine=true; // for debug output purposes only
				
				String startTimestamp ="";
				
				while(line != null){
					//Add the line to the output set of queries
					//			queryText.add(line);

					if (firstLine) {
						String[] fields = line.split(";"); // semicolon separated values
						String rawTS = fields[0];
						startTimestamp=rawTS.substring(6,10)+'-'+rawTS.substring(3,5)+'-'+rawTS.substring(0,2)+' '+rawTS.substring(11); //Slice and dice the assumeed format to the required format.
					}
					/*					if (fields.length == 2*Sensors.size()){ //check we have the right number of columns - 2 for each sensor.
						int fieldIndex=0;
						for (String sensorName : Sensors){
							//Get Timestamp 	
							//need to rearrange the fields, TBD - this is just hacked for now
							
							//String sensorTimestamp=fields[fieldIndex].replace('.', '-'); // The native format as periods between date tokens, derby required hyphens
							String rawTS = fields[fieldIndex];
							String sensorTimestamp=rawTS.substring(6,10)+'-'+rawTS.substring(3,5)+'-'+rawTS.substring(0,2)+' '+rawTS.substring(11); //Slice and dice the assumeed format to the required format.
							
							//Get value
							String sensorValue=fields[fieldIndex+1].replace(',', '.'); // The native format has commas for decimal places, derby required periods.
							
							String valuesText="('"+siteName+"','"+machineName+"','"+sensorName+"','"+sensorTimestamp+"','"+sensorValue+"')";
							
							insertRowsBuffer.add(valuesText);
							if (firstLine) {
								Logger.getAnonymousLogger().info("Values: " + valuesText);
							}
							fieldIndex+=2;
						}
					}*/
					firstLine=false;
					
					line = reader.readLine();
					if (line!=null && line.trim().length()>0) lastLine = line;
				}
				if (lastLine != null){
					String[] fields = lastLine.split(";"); // semicolon separated values
					String rawTS = fields[0];
					String endTimestamp=rawTS.substring(6,10)+'-'+rawTS.substring(3,5)+'-'+rawTS.substring(0,2)+' '+rawTS.substring(11); //Slice and dice the assumeed format to the required format.
					System.out.println("Trial Name, " + trialName +", Machine Name, " + machineName + ", Start Time, "+startTimestamp+", End time, "+endTimestamp);
				}
			}       
		} catch (FileNotFoundException ex) {
			Logger.getAnonymousLogger().severe("Cannot find the query file " + sourceFile.getName()+" " + ex.getMessage());
		} catch (IOException ex) {
			Logger.getAnonymousLogger().severe("IO exception with the query file " + sourceFile.getName()+" " + ex.getMessage());
		} finally {
			try {
				if (reader != null) reader.close();
				if (fis != null) fis.close();
			} catch (Exception ex) {
				Logger.getAnonymousLogger().severe("Exception closing the query file " + sourceFile.getName()+" " + ex.getMessage());
			}
		}


	}

	private static ArrayList<String> getSensorList(String line) {
		ArrayList<String> SensorList = new ArrayList<String>();
		String[] columnNames =line.split(";");
		for (int i=0;i<columnNames.length;i=i+2){
			//look at the ith and i+1th column - they should be a timestamp/value pair.
			Matcher matchTimestamp = timestampColumnName.matcher(columnNames[i]);
			Matcher matchValue = valueColumnName.matcher(columnNames[i+1]);
			//check if the columns are timestamp and value
			if (!matchTimestamp.matches()||!matchValue.matches()){
				return null;
			} else 
				//check if the columns have the same sensor name.
				if (!matchTimestamp.group(1).equals(matchValue.group(1))){
					return null;
				} else {
					SensorList.add(matchTimestamp.group(1));
				}
		}
		// we have passed all the tests the format is as expected!
		return SensorList;
	}

	private static boolean isExpectedFormat(String line) {
		ArrayList<String> SensorList = new ArrayList<String>();
		String[] columnNames =line.split(";");
		for (int i=0;i<columnNames.length;i=i+2){
			//look at the ith and i+1th column - they should be a timestamp/value pair.
			Matcher matchTimestamp = timestampColumnName.matcher(columnNames[i]);
			Matcher matchValue = valueColumnName.matcher(columnNames[i+1]);
			//check if the columns are timestamp and value
			if (!matchTimestamp.matches()||!matchValue.matches()){
				return false;
			} else 
				//check if the columns have the same sensor name.
				if (!matchTimestamp.group(1).equals(matchValue.group(1))){
					return false;
				} else {
					SensorList.add(matchTimestamp.group(1));
				}
		}
		// we have passed all the tests the format is as expected!
		return true;
	}

	private static void scanDirectory(File directory) {
		// TODO Auto-generated method stub
		for (File candidate : directory.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory() || file.getName().toLowerCase().endsWith("_werte.csv");
					}
				})){
			if (candidate.isDirectory()) scanDirectory(candidate);//recursive decent into the directories
			else ingestFile(candidate);
		}
	}

}
