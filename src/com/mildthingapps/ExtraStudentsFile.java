package com.mildthingapps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class ExtraStudentsFile {
	
	private final static Logger logger = Logger.getLogger(ExtraStudentsFile.class.getName()); 

	private static final String SHOPPING_CART_ITEM = "Shopping Cart Item";
	private static final String SHOPPING_CART_PAYMENT_RECEIVED = "Shopping Cart Payment Received";

	public class PayPalTransactionHistorySummary {
		Date earliestOrder = new Date();
		Date latestOrder = new Date();
		Integer	numberOfOrders = -1;
		Boolean	anyErrors = false;
		String errorMessage = "";
	}
	
	public class PayPalOrder {
		Properties orderProps = new Properties();
		ArrayList<Properties> products = new ArrayList<Properties>();
//		Boolean anyErrors = false;
//		String errorMessage = "";		
		
		
		//This 
		public String toHtmlPage (String headerTag){
			StringBuffer pageMarkup = new StringBuffer();
			
			//Header line with the order number
			pageMarkup.append("<H1>Uniform Order \t("+orderProps.getProperty("Transaction ID")+") \t"+
				headerTag+"</H1>\n");

			//Now put a table with the order summary data - customer, student name etc.
			pageMarkup.append("<table>"+
					"<tr><td>Customer Name: </td><td>"+orderProps.getProperty("Name")+"</td></tr>"+
					"<tr><td>Contact Details: </td><td>"+orderProps.getProperty("Contact Phone Number")+" "+ orderProps.getProperty("From Email Address")+"</td></tr>"+
					"<tr><td>Student: </td><td>"+orderProps.getProperty("Note")+"</td></tr>"+
					"<tr><td>Order Date and Time: </td><td>"+orderProps.getProperty("Date")+" "+orderProps.getProperty("Time")+"</td></tr>"+
					"</table></h3>");
			
			//Header line with the order number. put a table with a row for each ordered product.
			pageMarkup.append("<H2>Ordered Products</H2>\n");
			//Now put a table with a row for each ordered product.
			pageMarkup.append("<table><tr><th>Product</th><th>Quantity</th></tr>");
			
			for (Properties product : products){
				// get the product name and size option, if applicable
				String productName = product.getProperty("Item Title");
				String option1Name = product.getProperty("Option 1 Name");
				if (option1Name.toLowerCase().contains("size")) {
					String option1Value = product.getProperty("Option 1 Value");
					productName = productName + " " + option1Value;
					//tidy up the name TBD make common code!!
					productName = productName.replaceAll(" *inch", "\"");
				}
				pageMarkup.append("<tr><td>"+productName+"</td><td align=center>"+product.getProperty("Quantity")+"</td></tr>");
			}
			pageMarkup.append("</table>");
			
			
			// Put a page break at the bottom of the page.
			pageMarkup.append("<p style=\"page-break-after: always\"/>");
			
			return pageMarkup.toString();
		}
	}
	

	public class PayPalOrders {
		// Orders hashmap - key is the transactionID, the value contains name-value pairs for each order field for that transaction.
		HashMap<String,PayPalOrder> Orders = new HashMap<String,PayPalOrder>();
		
		Boolean	anyErrors = false;
		String errorMessage = "";
	
	}

	File csvFile;
	HashMap<String,String> summary = null;
	PayPalOrders orders = null;
	
	

	
	// This method will extract summary information from the order file.
	// Will flag an if the order file does not have the correct header line or
	// if there are no Product lines in the file (it is possible to generate a file with no products so we check here
	// 
	private void getStudentMap(){
		summary = new HashMap<String,String>();

		// Prepare to read the file
		FileInputStream fis = null;
		BufferedReader reader = null;
		// Open File
		try {
			fis = new FileInputStream(csvFile);
			reader = new BufferedReader(new InputStreamReader(fis));

			// Get the first row from the file which will tell us which sensor readings follow.
			String line;

				// Iterate through lines
				while ((line = reader.readLine()) != null){
					// Look through the file to find orders and product item lines.
					String [] lineTokens = line.split(","); // I include quotes in this as some fields, such as addresses can contain commas!!
					if (line.trim().length()==0){}
					else if (lineTokens.length==2){
						summary.put(lineTokens[0].trim(),lineTokens[1].trim());
					} else {
						logger.severe("Extra name needs to be two fields, comma separated: "+line);
					}
				}
		}catch (FileNotFoundException ex) {
			logger.severe("Cannot find the file " + csvFile.getName()+" " + ex.getMessage());
		} catch (IOException ex) {
			logger.severe("IO exception with the file " + csvFile.getName()+" " + ex.getMessage());
		} finally {
			if (reader !=null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
			
	}

	public ExtraStudentsFile(String transactionFile) throws IOException {
		super();
		csvFile = new File (new File(transactionFile).getParent()+"\\ExtraNames.csv"); //TBD Improve this
	}
	
	//The returned hashmap is keyed off order id (transaction ID) and values are the associated student name
	public HashMap<String,String> getStudents () {
		if (summary==null){
			getStudentMap();
		}
		return summary;
	}

	 
}
