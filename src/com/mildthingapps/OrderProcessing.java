package com.mildthingapps;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

//import PayPalTransactionHistoryFile.PayPalOrder;

//This class processes orders, importing them into one of the RMS PTA uniform databases

public class OrderProcessing {
	
	private final static Logger logger = Logger.getLogger(OrderProcessing.class.getName()); 
	
	//TBD externalise this to be read from a property file...
	private static HashMap<String, String> catalogueToDatabaseNameMap = new HashMap<String, String>();
	static {
		catalogueToDatabaseNameMap.put("RMS School Tie Green/Silver One Size".toLowerCase(),"Green tie");
		catalogueToDatabaseNameMap.put("Green Polo Shirt Age 9/10 32\"".toLowerCase(),"Green Polo Shirt 32\" (9/10)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt Age 11/13 34\"".toLowerCase(),"Green Polo Shirt 34\" (11/13)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt Extra small 36\"".toLowerCase(),"Green polo shirt 36\" (XS)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt Small 38\"".toLowerCase(),"Green polo shirt 38\" (S)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt Medium 40\"".toLowerCase(),"Green polo shirt 40\" (M)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt LG 42/44\"".toLowerCase(),"Green polo shirt 42/44\" (L)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt XL 46\"".toLowerCase(),"Green polo shirt 46\" (XL)");
		catalogueToDatabaseNameMap.put("Green Polo Shirt XXL 48\"".toLowerCase(),"Green polo shirt 48\"");
		catalogueToDatabaseNameMap.put("Black Polo Shirt Extra small 36\"".toLowerCase(),"Black polo shirt 36\" (XS)");
		catalogueToDatabaseNameMap.put("Black Polo Shirt Small 38\"".toLowerCase(),"Black polo shirt 38\" (S)");
		catalogueToDatabaseNameMap.put("Black Polo Shirt Medium 40\"".toLowerCase(),"Black polo shirt 40\" (M)");
		catalogueToDatabaseNameMap.put("Black Polo Shirt Large 42/44\"".toLowerCase(),"Black polo shirt 42/44\" (L)");
		catalogueToDatabaseNameMap.put("Black Polo Shirt Extra Large 46\"".toLowerCase(),"Black polo shirt 46\" (XL)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Age 9/10 32\"".toLowerCase(),"White Polo Shirt 32\" (9/10)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Age 11/13 34\"".toLowerCase(),"White Polo Shirt 34\" (11/13)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Extra Small 36\"".toLowerCase(),"White polo shirt 36\" (XS)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Small 38\"".toLowerCase(),"White polo shirt 38\" (S)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Medium 40\"".toLowerCase(),"White polo shirt 40\" (M)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Large 42/44\"".toLowerCase(),"White polo shirt 42/44\" (L)");
		catalogueToDatabaseNameMap.put("White Polo Shirt Extra Large 46\"".toLowerCase(),"White polo shirt 46\" (XL)");

		catalogueToDatabaseNameMap.put("GCSE White Polo Shirt Age 11/13 34\"".toLowerCase(),"GCSE Polo Shirt 34\"");
		catalogueToDatabaseNameMap.put("GCSE White Polo Shirt Extra Small 36\"".toLowerCase(),"GCSE polo shirt 36\"");
		catalogueToDatabaseNameMap.put("GCSE White Polo Shirt Small 38\"".toLowerCase(),"GCSE polo shirt 38\"");
		catalogueToDatabaseNameMap.put("GCSE White Polo Shirt Medium 40\"".toLowerCase(),"GCSE polo shirt 40\"");
		catalogueToDatabaseNameMap.put("GCSE White Polo Shirt Large 42/44\"".toLowerCase(),"GCSE polo shirt 42/44\"");
		catalogueToDatabaseNameMap.put("GCSE White Polo Shirt Extra Large 46\"".toLowerCase(),"GCSE polo shirt 46\"");

		catalogueToDatabaseNameMap.put("White Sweatshirt Age 9//11".toLowerCase(),"White sweatshirt 9/11");
		catalogueToDatabaseNameMap.put("White Sweatshirt Age 12/13".toLowerCase(),"White sweatshirt 12/13");
		catalogueToDatabaseNameMap.put("White Sweatshirt Age 14/15".toLowerCase(),"White sweatshirt 14/15");
		catalogueToDatabaseNameMap.put("White Sweatshirt Size L".toLowerCase(),"White sweatshirt 36\"");
		catalogueToDatabaseNameMap.put("White Sweatshirt Size XL".toLowerCase(),"White sweatshirt 38\"");

		catalogueToDatabaseNameMap.put("Black Sweatshirt Age 9/10 32\"".toLowerCase(),"Black sweatshirt 32");
		catalogueToDatabaseNameMap.put("Black Sweatshirt Age 11/13 34\"".toLowerCase(),"Black sweatshirt 34\"");
		catalogueToDatabaseNameMap.put("Black Sweatshirt Extra Small 36\"".toLowerCase(),"Black sweatshirt 36\"");
		catalogueToDatabaseNameMap.put("Black Sweatshirt Small 38\"".toLowerCase(),"Black sweatshirt 38\"");
		catalogueToDatabaseNameMap.put("Black Sweatshirt Medium 40\"".toLowerCase(),"Black sweatshirt 40\"");
		catalogueToDatabaseNameMap.put("Black Sweatshirt Large 42 - 44\"".toLowerCase(),"Black sweatshirt 42/44\"");
		catalogueToDatabaseNameMap.put("Black Sweatshirt Extra Large 46\"".toLowerCase(),"Black sweatshirt 46\"");

		catalogueToDatabaseNameMap.put("Sport Socks - Green Shoe Size Childrens".toLowerCase(),"Green socks 12.5/3.5");
		catalogueToDatabaseNameMap.put("Sport Socks - Green Shoe Size Adults 2-5".toLowerCase(),"Green socks 2-5");
		catalogueToDatabaseNameMap.put("Sport Socks - Green Shoe Size Adults 6-9".toLowerCase(),"Green socks 6-9");
		catalogueToDatabaseNameMap.put("Sport Socks - Green Shoe Size Adults 10-13".toLowerCase(),"Green socks 10-13");

		
		catalogueToDatabaseNameMap.put("RMS School Tie Black/Silver One Size".toLowerCase(),"Black Tie");
		catalogueToDatabaseNameMap.put("Shin pads Suitable for height 135 cm".toLowerCase(), "Shinpads 135cm");
		catalogueToDatabaseNameMap.put("Shin pads Suitable for height 155 cm".toLowerCase(), "Shinpads 155cm");
		catalogueToDatabaseNameMap.put("Shin pads Suitable for height 175 cm".toLowerCase(), "Shinpads 175cm");
		
	}
	
	private static HashMap<String, String> catalogueToDatabaseReplacements = new HashMap<String, String>();
	static {
		catalogueToDatabaseReplacements.put("Black V Neck Jumper","Black jumper");
		catalogueToDatabaseReplacements.put("Green V Neck Jumper","Green jumper");
		catalogueToDatabaseReplacements.put("Tracksuit Trousers","Tracksuit bottoms");
		catalogueToDatabaseReplacements.put("Rugby Shirt","Green rugby shirt");
//		catalogueToDatabaseReplacements.put("Shin Pads","Shinpads");
//		catalogueToDatabaseReplacements.put("Black SKORT","Black Skort");
		catalogueToDatabaseReplacements.put("Black PE Shorts","Black shorts");
	}
	
	//Map of product name to product ID TBD expand to include price, banking group etc and extract
	private HashMap<String, String> productIDLookup = new HashMap<String, String>();
	
	private void populateProductCache (){
		// Query the database and extract out all the names/IDs.
		try {
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("select * from Product");
			while (rs.next()){
				String productID = rs.getString(1);
				String productName = rs.getString(2);
				if (productID!=null&&productName!=null){
					productIDLookup.put(productName.toLowerCase(), productID);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean productCacheIsPopulated (){
		return !productIDLookup.isEmpty();
	}
	
	private void clearProductCache (){
		productIDLookup = new HashMap<String, String>();
	}	
	
	private String productIDFromCache (String productName){
		return productIDLookup.get(productName.toLowerCase());
	}
	
	private String getProductID (String productName){
		if (!productCacheIsPopulated()){
			populateProductCache();
		}
		//tidy up the name
		productName = productName.replaceAll(" *inch", "\"");
		productName = productName.replaceAll("  *", " "); //remove double spaces - some catalogue items come in like this

	    //Map from shop product descriptions into database product names
		if (catalogueToDatabaseNameMap.containsKey(productName.toLowerCase())){
			productName = catalogueToDatabaseNameMap.get(productName.toLowerCase());
		} else {
			for (String pattern : catalogueToDatabaseReplacements.keySet()) {
				if (productName.contains(pattern) )
					productName=productName.replaceAll(pattern, catalogueToDatabaseReplacements.get(pattern));
			}
		}
		
		logger.fine("looking for product: "+productName);

		return productIDFromCache(productName);
	}
	
	// Keep record of which products couldnt be inserted for which orders
	private HashSet<String> unResolvedProducts = new HashSet<String>();
	private HashMap<String,ArrayList<String>> unResolvedOrderProducts  = new HashMap<String,ArrayList<String>>();
	
	// Event classification - for polo only orders
	public boolean isOnlyPolos(ArrayList<Properties> products) {
		
		for (Properties product : products) {
			String productname = product.getProperty("Item Title");
			if (!(productname.contains("Green Polo") || productname.contains("Black Polo"))){
				return false; // we have a product that is not a polo shirt.
			}
		}
		return true; // all products found are polo shirts.
	}
	
	public String autoClassifyProduct (String productname){
		String overAllEventID = null;

			if (productname.contains ("Green Polo")) {
				return ("4"); //TBD constantise
			} else if (productname.contains("Black Polo")) {
				return("8"); //TBD constantise
			} else if (productname.contains("Black V Neck Jumper")) {
				return ("3"); //TBD constantise
			} else {
				return("1");// return if we have an unclassified order
			}

	}

	
	public String autoClassifyOrder (ArrayList<Properties> products){
		String overAllEventID = null;

		// Go through each product, classify according to the first, then check to see if the rest are the same.
		for (Properties product : products) {
			String productname = product.getProperty("Item Title");
			String thisProductEventID = autoClassifyProduct(productname);
			if (thisProductEventID == "1") { return "1";};
			if (overAllEventID==null){
				// take the first product event, whatever that is
				overAllEventID=thisProductEventID;
			} else {
				if (thisProductEventID!=overAllEventID){
					if (overAllEventID=="4"&&thisProductEventID=="8") {
						//categorise a mix of black and green polos as "4" - polos
						overAllEventID="4";
					} else if (overAllEventID=="8"&&thisProductEventID=="4") {
						//categorise a mix of black and green polos as "4" - polos
						overAllEventID="4";
					} else {
					// If the new product is different the others, set the event to "Unclassified".
					return("1");// return if we have an unclassified order
					}
				}
			}
		}	
		return(overAllEventID);// return if we have an unclassified order
	}
	
	private void addEventProductCategory(
			HashMap<String, ArrayList<String>> productsPerEventID,
			String eventID, String productID) {
		if (!productsPerEventID.containsKey(eventID)){
			productsPerEventID.put(eventID, new ArrayList<String>());
		}
		productsPerEventID.get(eventID).add(productID);
	}
	
	
	private void addToUnResolvedProducts (String orderID, String productName){
		unResolvedProducts.add(productName);
		if (!unResolvedOrderProducts.containsKey(orderID)){
			unResolvedOrderProducts.put(orderID, new ArrayList<String>());
		}
		unResolvedOrderProducts.get(orderID).add(productName);
	}
	
	public void clearUnResolvedProducts() {
		unResolvedProducts = new HashSet<String>();
		unResolvedOrderProducts  = new HashMap<String,ArrayList<String>>();
	}
	
	public ArrayList<String> getUnResolvedOrderProducts (String orderID) {
		return unResolvedOrderProducts.get(orderID);
	}
	
	public Set<String> getAllUnResolvedProducts (){
		return unResolvedProducts;
	}
	
	// We set, and connect to the database when the object is created, these member attributes
	// provide access.
	private final String databaseFileName;
	private final Connection conn;
	
	// Prepared Statements
	PreparedStatement productQuery;
	PreparedStatement orderQuery;
	PreparedStatement orderAccountEventQuery;
	Statement statemnt;

	
	// 
//	boolean categorisePolosOnly = false;
	
	// Connect to the named database as part of the constructor
//	public OrderProcessing(String databaseFileName, boolean categorisePolosOnly) throws ClassNotFoundException, SQLException {
	public OrderProcessing(String databaseFileName) throws ClassNotFoundException, SQLException {
		super();
		this.databaseFileName = databaseFileName;

		Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		conn=DriverManager.getConnection("jdbc:ucanaccess://"+this.databaseFileName); 
		
		prepareQueries();
	}
	
	private void prepareQueries(){
		//Get a statement from the shared connection.
		try {
			productQuery = conn.prepareStatement("select ID from Product where ProductName = ?");
			orderAccountEventQuery = conn.prepareStatement("select ID from Order where AcctName = ? and EventsID = ?");
			orderQuery = conn.prepareStatement("select ID from Order where AcctName = ?");
			statemnt = conn.createStatement();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	// A test to check if we are validly connected
	public boolean isConnected(){
		return (conn!=null);
	}
	
	private String trimToLength (String text, int length){
		if (text.length()>length){
			return text.substring(0, length);
		} else {
			return text;
		}
	}
	
	public boolean RemoveFromDatabase(String orderID) {
		//Get a statement from the shared connection.
		try {
		ResultSet rs;

	//	String orderQuery = "select ID from Order where AcctName = '"+orderID+"'";
		orderQuery.setString(1, orderID);
			rs = orderQuery.executeQuery();
			// Get the Order ID of the matching order to check for duplicates
			if (rs.next()){
				//Order has been found so remove it
			
				// delete related orderedproducts.
				statemnt.execute("delete from orderedproduct op where exists (select * from order o where  AcctName = '"+orderID+"' and op.orderid=o.id)");
	
				// delete the order record
				statemnt.execute("delete from Order where AcctName = '"+orderID+"'");
				
				return true;
			} else {
				logger.fine("Order not found, not deleted!");
				return false; // we didn't insert the order.
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	// For the order, identify Customer, Student, order summary, ordered products.
	// try and find a matching student, customer, and ordered products. Insert new Customer/student if they don't exist. Flag error if a product is not found!!
	// we will need to match on product name and /or product ID.
	// if they are all OK, insert the orders into the access database (this can be configured/selected) and defaults/remembers between runs.
	// The returned List of Strings are the database orderIDs inserted, and is empty if there is a duplicate, or an error.
	public ArrayList<String> ImportToDatabase(PayPalTransactionHistoryFile.PayPalOrder order) throws ClassNotFoundException, SQLException, ParseException {
		return(ImportToDatabase(order, "0")); //0 is the auto-classify option
	}

	public ArrayList<String> ImportToDatabase(PayPalTransactionHistoryFile.PayPalOrder order, String eventID) throws ClassNotFoundException, SQLException, ParseException {

		ArrayList<String> dbOrderIDs = new ArrayList<String>();
		
		boolean error_occured = false;
		
		//Get a statement from the shared connection.
		Statement statemnt = conn.createStatement();
		ResultSet rs;

		//First check if we have already inserted the order.. 
		String transactionID = order.orderProps.getProperty("Transaction ID");
		
//		String orderQuery = "select ID from Order where AcctName = '"+transactionID+"'";
		orderQuery.setString(1, transactionID);
		rs = orderQuery.executeQuery();

		// Get the Order ID of the matching order to check for duplicates
		String orderID ="";
		if (rs.next()){
			logger.fine("Order is a duplicate, no insertion!");
			return dbOrderIDs; // will be empty
		}

		// Handle the customer elements

		// The order property " Name" should be split into firstnames and surname
		String customerName = order.orderProps.getProperty("Name");
		String[] names = customerName.split(" ");

		String customerSurname = names[names.length-1].trim();
		int surnameLength = names[names.length-1].length();
		String customerFirstnames = customerName.substring(0, customerName.length()-surnameLength).trim();

		String customerTelNo = order.orderProps.getProperty("Contact Phone Number")+" "+ order.orderProps.getProperty("From Email Address");

		String customerQuery = "select ID from Customer where Surname = '"+SQLString(customerSurname)+"' and FirstName = '"+SQLString(customerFirstnames)+"' and TelNo = '"+SQLString(customerTelNo)+"'";
		rs = statemnt.executeQuery(customerQuery);

		// Get the customer ID of the matching customer - if found, otherwise insert the customer as new!
		String customerID="";
		if (rs.next()){
			customerID = rs.getString(1);
			logger.fine("Customer ID: "+customerID+", existing");
		} else {
			//Insert the new customer record
			String customerInsert = "insert into Customer values (0,'"+SQLString(customerSurname)+"','"+SQLString(customerFirstnames)+"','"+SQLString(customerTelNo)+"')";
			statemnt.execute(customerInsert);

			// Now re-query to get the ID back..
			rs = statemnt.executeQuery(customerQuery);
			if (rs.next()){
				customerID = rs.getString(1);
				logger.fine("Customer ID: "+customerID+", newly created");
			} else {
				//TBD ERROR Shouldn't get here!!
				logger.severe("Error, failed to insert or find the customer record");
				error_occured = true;
			}
		}

		// Handle the student elements
		// The order property " Note" should be split into firstnames, surname and class
		String studentDetails = order.orderProps.getProperty("Note");
		String[] studNames = studentDetails.split(" ");

		String tutorGroup="", studfirstName="", studLastname = "";
		if (studentDetails.length()==0){
			// No details entered
			studfirstName = "Unknown, Order "+ transactionID;
		} else if (studNames.length==3){
			// three words, assume it's first name, surname, tutor group.
			studfirstName = studNames[0];
			studLastname = studNames[1];
			tutorGroup = studNames[2];
		} else {
			// TBD For now, put everything in the "Firstname" - find a way to cleanse.
			studfirstName = trimToLength(studentDetails,50);
		}

		String studentQuery = "select ID from Student where Surname = '"+SQLString(studLastname)+"' and FirstName = '"+SQLString(studfirstName)+"' and TutorGp = '"+SQLString(tutorGroup)+"'";
		logger.fine("looking for student: "+studentQuery);
		rs = statemnt.executeQuery(studentQuery);

		// Get the customer ID of the matching customer - if found, otherwise insert the customer as new!
		String studentID="";
		if (rs.next()){
			studentID = rs.getString(1);
			logger.fine("Student ID: "+studentID+", existing");
		} else {
			//Insert the new student record
			String studentInsert = "insert into Student values (0,'"+SQLString(studLastname)+"','"+SQLString(studfirstName)+"','"+SQLString(tutorGroup)+"')";
			statemnt.execute(studentInsert);

			// Now re-query to get the ID back..
			rs = statemnt.executeQuery(studentQuery);
			if (rs.next()){
				studentID = rs.getString(1);
				logger.fine("Student ID: "+studentID+", newly created");
			} else {
				//TBD ERROR Shouldn't get here!!
				logger.severe("Error, failed to insert or find the student record");
				error_occured = true;
			}
		}	
		
		//Now find products!! TBD do this first just in case they don't exist!!
		
		//key: product Id, value: quantity ordered.
		HashMap<String, Integer> productsOrdered = new HashMap<String, Integer>();
		HashMap<String, ArrayList<String>> productsPerEventID = new HashMap<String, ArrayList<String>>();
		
		for (Properties product :order.products){
			// get the product name and size option, if applicable
			String rawProductName = product.getProperty("Item Title");
			String productName = rawProductName;
			String option1Name = product.getProperty("Option 1 Name");
			if (option1Name.toLowerCase().contains("size")) {
				String option1Value = product.getProperty("Option 1 Value");
				productName = productName + " " + option1Value;
			}

			// Get the product ID of the matching product 
			// TBD - I could cache these in a local hashmap!!
			String productID = getProductID(productName);
			if (productID!=null){
				logger.fine("Product ID: "+productID+", existing");
				
				String QuantityString = product.getProperty("Quantity");
				int productsOrderedThisTime=Integer.parseInt(QuantityString);
				if (!productsOrdered.containsKey(productID)){
					productsOrdered.put(productID, productsOrderedThisTime);
					if (eventID == "0") {// autoclassify TBD extract into a final or enum
						//split the order into different product sets
						addEventProductCategory(productsPerEventID,autoClassifyProduct(rawProductName),productID);
					} else {
						addEventProductCategory(productsPerEventID,eventID,productID);
					}
				} else {
					int productQuantitySoFar = productsOrdered.get(productID);
					productsOrdered.put(productID, productQuantitySoFar+productsOrderedThisTime);
				}
			} else {
				//If we get here then either the product is not in the database, or the name in the catalog doesn't match the uniform database.
				addToUnResolvedProducts(transactionID, productName);
				logger.severe("NO VALID PRODUCT FOUND for " + productName + ", name from Paypal: "+rawProductName+", Order: "+transactionID);
//				error_occured = true; // I don't treat this as an error anymore - still import the order, but report on the products missing!!
			}	
		}
		
		if (error_occured) return dbOrderIDs; // this will be empty.
		
		// Now process the overall order. -> TBD check for duplicates using order date and customer ID - I'll put the paypal transaction into the "cheque no" field

		// TBD - check that Currency is GBP, status is completed, 
		
		String orderAmount = order.orderProps.getProperty("Net");
		
		// formatter object for extracting order dates
		DateFormat payPalFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		DateFormat dBFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String dateToken = order.orderProps.getProperty("Date");
		String timeToken = order.orderProps.getProperty("Time");
		Date orderDate = payPalFormat.parse(dateToken+' '+timeToken);
		
		// Insert different orders for different sets of products.
		// This means that we can split polo orders from others to be fulfilled separately if necessary.
		
		String thisEventID = eventID;
		
/*		if (eventID == "0") {// autoclassify TBD extract into a final or enum
			//split the order into different product sets
			
			thisEventID = autoClassifyOrder(order.products);
		}		
*/
		for (String subEventID : productsPerEventID.keySet()){
			// We want to insert an order for each different "event" category, e.g. polos, black polos, others etc.
			//Insert the new order record
			// TBD second order date shold be supply date!!
			String orderInsert = "insert into Order "+ 
					"(ID, STUDENTID, CUSTOMERID, ORDERDATE, SUPPLYDATE, AMOUNTENC, PAYMENTMETHOD, CHEQUENO, ACCTNAME, EVENTSID, SPECIALSID) "+
					"values (0,'"+studentID+"','"+customerID+"','"+dBFormat.format(orderDate)+"','"+dBFormat.format(orderDate)+"','"+orderAmount+"',3,'','"+transactionID+"',"+subEventID+",1)";

			statemnt.execute(orderInsert);
			orderAmount= "0"; //TBD do something cleverer to apportion the amount of the order!!

			// Now re-query to get the ID back..
//			orderQuery = "select ID from Order where AcctName = '"+transactionID+"' and EventsID='"+subEventID+"'";
			orderAccountEventQuery.setString(1, transactionID);
			orderAccountEventQuery.setString(2, subEventID);
			
			rs = orderAccountEventQuery.executeQuery();
			if (rs.next()){
				orderID = rs.getString(1);
				logger.fine("Order ID: "+orderID+", newly created");
				dbOrderIDs.add(orderID);
				
			} else {
				//TBD ERROR Shouldn't get here!!
				logger.severe("Error, failed to insert the order record");
//				return dbOrderIDs; // we failed to insert the order.
			}
			
			// Finally insert the order products.
//			for (String productid : productsOrdered.keySet()){
			for (String productid : productsPerEventID.get(subEventID)){
				//Insert the new customer record
				String productInsert = "insert into OrderedProduct values (0,'"+orderID+"','"+productid+"',"+productsOrdered.get(productid)+")";
				statemnt.execute(productInsert);
			}
		}
			
		return dbOrderIDs; // we successfully inserted the order.
		
	}

	private String SQLString(String unescapedString) {
		// TODO Auto-generated method stub
		return unescapedString.replace("\'", "\'\'");
	}

}
