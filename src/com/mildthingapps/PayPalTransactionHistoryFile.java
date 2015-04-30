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
import java.util.TreeMap;
import java.util.logging.Logger;

public class PayPalTransactionHistoryFile {
	
	private final static Logger logger = Logger.getLogger(PayPalTransactionHistoryFile.class.getName()); 

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
		// Orders Map - key is the transactionID, the value contains name-value pairs for each order field for that transaction.
		// I chose a treemap so that the orders will be ordered on the transactionID.
		TreeMap<String,PayPalOrder> Orders = new TreeMap<String,PayPalOrder>();
		
		Boolean	anyErrors = false;
		String errorMessage = "";
	
	}
	
	public PayPalTransactionHistoryFile() {
		super();
	}

	File TxFile;
	PayPalTransactionHistorySummary summary = null;
	PayPalOrders orders = null;
	
	//these are the fields that we need in the file to be able to fully process it.
	String[] requiredFields = {"Date", "Time", "Name", "Type", "Status", "Currency", "Gross", "Fee", "Net", "Note", "From Email Address", "Transaction ID", "Item Title", "Item ID", "Option 1 Name", "Option 1 Value", "Quantity", "Contact Phone Number"};

	//these are the fields needed for the order detail lines,
	String[] orderFields = {"Date", "Time", "Name", "Type", "Status", "Currency", "Gross", "Fee", "Net", "Note", "From Email Address", "Transaction ID", "Quantity", "Contact Phone Number"};
	//these are the fields needed for the product detail lines,
	String[] productFields = {"Transaction ID", "Item Title", "Item ID", "Option 1 Name", "Option 1 Value", "Quantity"};

	private boolean isExpectedHeader(String line) {

		String[] columnNames = line.split(", *");
		HashSet<String> columnNameSet = new HashSet<String>(Arrays.asList(columnNames));
		if (!columnNameSet.containsAll(Arrays.asList(requiredFields))) {
			 ArrayList<String> Remainder = new ArrayList(Arrays.asList(requiredFields));
			 Remainder.removeAll(columnNameSet);
			 logger.severe("Fields are missing from the file: " + Remainder.toString());
			return false;
		} else {
		 	// we have all the fields required so the format is as expected!
			return true;
		}

	}
	
	// The hashmap returned has keys of each required field name and value is the column index (0 based) of the 
	// corresponding column.
	private HashMap<String,Integer> getFieldIndexMap(String line) {

		HashMap<String,Integer> fieldIndexMap = new HashMap<String,Integer>();
		
		String[] columnNames = line.split(", *");
		ArrayList<String> requiredFieldsList = new ArrayList<String>(Arrays.asList(requiredFields));
		for (int i=0;i<columnNames.length;i++){
			String columnName = columnNames[i];
			if (requiredFieldsList.contains(columnName)){
				fieldIndexMap.put(columnName, i);
			}
		}

		// we have all the fields and their indexes in the map.
		return fieldIndexMap;
	}
	
	// This method will extract summary information from the order file.
	// Will flag an if the order file does not have the correct header line or
	// if there are no Product lines in the file (it is possible to generate a file with no products so we check here
	// 
	private void getSummaryForPayPalFile(){
		summary = new PayPalTransactionHistorySummary();

		// Prepare to read the file
		FileInputStream fis = null;
		BufferedReader reader = null;
		// Open File
		try {
			fis = new FileInputStream(TxFile);
			reader = new BufferedReader(new InputStreamReader(fis));

			// Get the first row from the file which will tell us which sensor readings follow.
			String line = reader.readLine();
			String previousLine = line;

			// check that the header row is in the expected format 
			if (isExpectedHeader(line)){
				summary.numberOfOrders = 0;
				int OrderItemLineCount = 0;
				
				//work out which columns contain which properties
				HashMap<String,Integer> fieldIndexMap = getFieldIndexMap(line);
				
				int typeFieldIndex = fieldIndexMap.get("Type"); // this is the field that indicated the type of each line
				int dateFieldIndex = fieldIndexMap.get("Date"); // this is the field that indicated the date of each order
				int timeFieldIndex = fieldIndexMap.get("Time"); // this is the field that indicated the date of each order
				
				// formatter object for extracting order dates
				DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

				// Iterate through lines
				while ((line = reader.readLine()) != null){
					// Look through the file to find orders and product item lines.
					String [] lineTokens = line.split("\",\""); // I include quotes in this as some fields, such as addresses can contain commas!!
					String typeToken = lineTokens[typeFieldIndex].replace("\"",""); //strip out surrounding quotes
					if (typeToken.equals(SHOPPING_CART_PAYMENT_RECEIVED)){
						// We have a new order line
						// get the order date and see if it is the earliest or latest
						String dateToken = lineTokens[dateFieldIndex].replace("\"",""); //strip out surrounding quotes
						String timeToken = lineTokens[timeFieldIndex].replace("\"","");
						Date orderDate;
						try {
							orderDate = format.parse(dateToken+' '+timeToken);
							summary.numberOfOrders++;
							
							if (summary.numberOfOrders==1){
								//the first order so the date is the earliest and the latest
								summary.earliestOrder = orderDate;
								summary.latestOrder = orderDate;
							} else {
								// check to see if the date is the earliest or the latest.
								if (orderDate.after(summary.latestOrder)) summary.latestOrder = orderDate;
								if (orderDate.before(summary.earliestOrder)) summary.earliestOrder = orderDate;
							}

						} catch (ParseException e) {
							// We failed to get the date!! p
							e.printStackTrace();
							summary.anyErrors = true;
							summary.errorMessage = "file format incorrect - Dates in the order file are invalid";
							return;
						}
					}					  
					else if (typeToken.equals(SHOPPING_CART_ITEM))
						// we have a product line item
						OrderItemLineCount++;
				}

				// Did we find any Cart items? if not then the export is invalid!
				if (OrderItemLineCount==0){
					summary.anyErrors = true;
					summary.errorMessage = "file format incorrect - Please include Shopping Cart details in the PayPal file";
				}
				
			} else {
				//header row is not correct
				summary.anyErrors = true;
				summary.errorMessage = "file format incorrect - first line is not as expected.";
			}

		}catch (FileNotFoundException ex) {
			Logger.getAnonymousLogger().severe("Cannot find the file " + TxFile.getName()+" " + ex.getMessage());
		} catch (IOException ex) {
			Logger.getAnonymousLogger().severe("IO exception with the file " + TxFile.getName()+" " + ex.getMessage());
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

	// This method will extract the orders and product details from the order file.
	// Will flag an if the order file does not have the correct header line,
	// if there are no Product lines in the file (it is possible to generate a file with no products so we check here)
	// or if any of the fields are invalid.
	// 
	private void getOrdersFromPayPalFile(){
		orders = new PayPalOrders();

		// Prepare to read the file
		FileInputStream fis = null;
		BufferedReader reader = null;
		// Open File
		try {
			fis = new FileInputStream(TxFile);
			reader = new BufferedReader(new InputStreamReader(fis));

			// Get the first row from the file which will tell us which sensor readings follow.
			String line = reader.readLine();
			String previousLine = line;

			// check that the header row is in the expected format 
			if (isExpectedHeader(line)){

				//work out which columns contain which properties
				HashMap<String,Integer> fieldIndexMap = getFieldIndexMap(line);

				int typeFieldIndex = fieldIndexMap.get("Type"); // this is the field that indicated the type of each line

				PayPalOrder order;
//				String transactionID;
				// Iterate through lines
				while ((line = reader.readLine()) != null){
					// Look through the file to find orders and product item lines.
					String [] lineTokens = line.split("\",\""); // I include quotes in this as some fields, such as addresses can contain commas!!
					String typeToken = lineTokens[typeFieldIndex].replace("\"",""); //strip out surrounding quotes
					if (typeToken.equals(SHOPPING_CART_PAYMENT_RECEIVED)){
						// We have a new order line get all the order detail fields..
						Properties orderProperties = new Properties();
						for (String orderField : orderFields){
							int fieldIndex = fieldIndexMap.get(orderField);
							String fieldValue = lineTokens[fieldIndex].replace("\"",""); //strip out surrounding quotes)
							orderProperties.setProperty(orderField, fieldValue);
						}
						// get the transaction ID and add the properties to the order.
						String transactionID = orderProperties.getProperty("Transaction ID");
						//TBD check for duplicates??
						
						order = new PayPalOrder();
						order.orderProps = orderProperties;
						orders.Orders.put(transactionID, order);
					}					  
					else if (typeToken.equals(SHOPPING_CART_ITEM)) {
						// we have a product line item, get all the product detail fields..
						Properties productProperties = new Properties();
						for (String productField : productFields){
							int fieldIndex = fieldIndexMap.get(productField);
							String fieldValue = lineTokens[fieldIndex].replace("\"",""); //strip out surrounding quotes)
							productProperties.setProperty(productField, fieldValue);
						}
						// get the transaction ID and add the properties to the order.
						String transactionID = productProperties.getProperty("Transaction ID");
						//TBD check for duplicates??
						if (!orders.Orders.containsKey(transactionID)){
							logger.severe("error, can't find order "+transactionID+" to add product");
							orders.anyErrors=true;
							orders.errorMessage=orders.errorMessage+" : can't find order "+transactionID+" to add product";
							
//							orders.OrderProducts.put(transactionID,new ArrayList<Properties>());
						};
						
						if (productProperties.getProperty("Item Title").startsWith("Student Name")){
							// Insert the name and class into the main order record.
							orders.Orders.get(transactionID).orderProps.setProperty("Note", productProperties.getProperty("Option 1 Value"));
						} else {
							orders.Orders.get(transactionID).products.add(productProperties);
						}
					}
				}

			} else {
				//header row is not correct
				orders.anyErrors = true;
				orders.errorMessage = "file format incorrect - first line is not as expected.";
			}

		}catch (FileNotFoundException ex) {
			Logger.getAnonymousLogger().severe("Cannot find the file " + TxFile.getName()+" " + ex.getMessage());
		} catch (IOException ex) {
			Logger.getAnonymousLogger().severe("IO exception with the file " + TxFile.getName()+" " + ex.getMessage());
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
	public PayPalTransactionHistoryFile(File transactionFile) {
		super();
		TxFile = transactionFile;
	}
	
	public PayPalTransactionHistorySummary getSummary () {
		if (summary==null){
			getSummaryForPayPalFile();
		}
		return summary;
	}
	
	public PayPalOrders getOrders () {
		if (orders==null){
			getOrdersFromPayPalFile();
		}
		return orders;
	}
	
	 
}
