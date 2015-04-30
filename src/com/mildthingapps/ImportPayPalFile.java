package com.mildthingapps;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class ImportPayPalFile {
	
	private final static Logger logger = Logger.getLogger(ImportPayPalFile.class.getName()); 

	// define the parameters for database connection and set them to their defaults.
	static ArrayList<String> Filenames = new ArrayList<String>();
	
	private static ArrayList<String> outOfStockProducts = new ArrayList<String>();
	
	static {
//		outOfStockProducts.add("Black Polo Shirt Extra small 36\"");
//		outOfStockProducts.add("Green Polo Shirt Age 11/13 34\"");
	}

	
	// Set up the necessary tables to allow the inserting of data.
	// The returned flag indicates whether the table is successfully initialised
	private static boolean initialiseTargetDatabase(){
		return false;
	}
	
	private static void processArgs (String[] args){
		List<String> argsList = Arrays.asList(args);
		for ( int i=0; i<args.length; i++ ) {
			final String arg = args[i];
				
				File testFile = new File(arg);
				if (testFile.isDirectory()||testFile.isFile()){
					Filenames.add(arg);
				} else {
					logger.severe("Error - " + arg + " is not a file or directory.");
			        System.exit(1); 
				}
			}
		
		// Check that we have got all necessary parameters
		if (Filenames.isEmpty())logger.severe("Error - You need to specify the file to read.");
		
	}
	
	// This method determine whether all the products in the array are polos.
	public static boolean isOnlyPolos(ArrayList<Properties> products) {
		for (Properties product : products) {
			String productname = product.getProperty("Item Title");
			if (!(productname.contains("Green Polo") || productname.contains("Black Polo"))){
				return false; // we have a product that is not a polo shirt.
			}
		}
		return true; // all products found are polo shirts.
	}
	
	// This method determine whether any of the products in the order are out of stock.
	public static boolean isOutOfStock(ArrayList<Properties> products) {
		for (Properties product : products) {
			String productName = product.getProperty("Item Title"); //TBD Duplicate code - refactor!!
			
			String option1Name = product.getProperty("Option 1 Name");
			if (option1Name.toLowerCase().contains("size")) {
				String option1Value = product.getProperty("Option 1 Value");
				productName = productName + " " + option1Value;
			}
			productName = productName.replaceAll(" *inch", "\"");
			productName = productName.replaceAll("  *", " "); //remove double spaces - some catalogue items come in like this

			if (outOfStockProducts.contains(productName)){
				return true; // we have a product that is out of stock.
			}
		}
		return false; // none of the products are out of stock.
	}
	
	private static String htmlHeaderText () {
		return ("<style> "+
				"h1 { font-size: 2.5em; }"+
				"h2 { font-size: 1.875em; }"+
				"p { font-size: 0.875;}"+
				"td { font-size: 1.5em;}"+
				"th { font-size: 1.7em;text-align: left;}"+
				"</style>");
	}

	/**
	 * write output html to <Filename>_ordersummaries.html
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException, IOException {
		// parse the parameters.
		processArgs(args);
		
		OrderProcessing database = new OrderProcessing("DB/Uniform2013.mdb");
		
		for (String fileName : Filenames){
			PayPalTransactionHistoryFile payPalFile = new PayPalTransactionHistoryFile(new File(fileName));
			
			PayPalTransactionHistoryFile.PayPalTransactionHistorySummary payPalSummary = payPalFile.getSummary();

			logger.info("File: "+fileName+ 
					((payPalSummary.anyErrors) ?", Errors: "+payPalSummary.errorMessage:
						", number of orders: "+payPalSummary.numberOfOrders +
						", earliest order: "+payPalSummary.earliestOrder + 
						", latest order: "+payPalSummary.latestOrder)
					);
			
			int ordersInserted = 0;
			
			//Read in the "Extra Student Info" file.. TBD read name from input params, make this more generic
			ExtraStudentsFile extraStudents = new ExtraStudentsFile(fileName);
			HashMap<String,String> studentNames = extraStudents.getStudents();
			
			//create output files
			File outfile = new File(fileName+"_ready_orders.html");
			File blankstudentsfile = new File(fileName+"_no_student_details.txt");
			File onHoldFile = new File(fileName+"_on_hold.html");
			File onHoldPolosFile = new File(fileName+"_on_hold_polos.html");
			File polosFile = new File(fileName+"_polos_only.html");
			File productsMissingFile = new File(fileName+"_missingOrderProducts.txt");
			File outOfStockPolosFile = new File(fileName+"_OutOfStock_orders.html");
			File outOfStockEmailsFile = new File(fileName+"_OutOfStock_emails.html");

			
			//overwrite a file if it exists already TBD do something cleverer.
			if (outfile.exists()) {
				outfile.delete();
			}
			outfile.createNewFile();
			
			FileWriter fw = new FileWriter(outfile.getAbsoluteFile());
			BufferedWriter readyOrders = new BufferedWriter(fw);
			readyOrders.write(htmlHeaderText());
			
			//overwrite a file if it exists already TBD do something cleverer.
/*			if (polosFile.exists()) {
				polosFile.delete();
			}
			polosFile.createNewFile();
			
			FileWriter polosFw = new FileWriter(polosFile.getAbsoluteFile());
			BufferedWriter poloOrders = new BufferedWriter(polosFw);
			poloOrders.write(htmlHeaderText());*/
			
			//overwrite a file if it exists already TBD do something cleverer.
			if (onHoldFile.exists()) {
				onHoldFile.delete();
			}
			onHoldFile.createNewFile();
			
			FileWriter onHoldfw = new FileWriter(onHoldFile.getAbsoluteFile());
			BufferedWriter onHoldOrders = new BufferedWriter(onHoldfw);
			onHoldOrders.write(htmlHeaderText());
			
			
			//overwrite a file if it exists already TBD do something cleverer.
/*			if (onHoldPolosFile.exists()) {
				onHoldPolosFile.delete();
			}
			onHoldPolosFile.createNewFile();
			FileWriter onHoldPolosfw = new FileWriter(onHoldPolosFile.getAbsoluteFile());
			BufferedWriter onHoldPoloOrders = new BufferedWriter(onHoldPolosfw);
			onHoldPoloOrders.write(htmlHeaderText());*/

			//overwrite a file if it exists already TBD do something cleverer.
			if (blankstudentsfile.exists()) {
				blankstudentsfile.delete();
			}
			blankstudentsfile.createNewFile();
			FileWriter blank_studs_fw = new FileWriter(blankstudentsfile.getAbsoluteFile());
			BufferedWriter noStudentInfoSummary = new BufferedWriter(blank_studs_fw);
			
			//overwrite a file if it exists already TBD do something cleverer.
			if (productsMissingFile.exists()) {
				productsMissingFile.delete();
			}
			productsMissingFile.createNewFile();
			FileWriter missing_prods_fw = new FileWriter(productsMissingFile.getAbsoluteFile());
			BufferedWriter missing_prods_bw = new BufferedWriter(missing_prods_fw);
			
			//overwrite a file if it exists already TBD do something cleverer.
			if (outOfStockPolosFile.exists()) {
				outOfStockPolosFile.delete();
			}
			outOfStockPolosFile.createNewFile();
			FileWriter out_of_stock_fw = new FileWriter(outOfStockPolosFile.getAbsoluteFile());
			BufferedWriter out_of_stockbw = new BufferedWriter(out_of_stock_fw);
			out_of_stockbw.write(htmlHeaderText());
			
			//overwrite a file if it exists already TBD do something cleverer.
			if (outOfStockEmailsFile.exists()) {
				outOfStockEmailsFile.delete();
			}
			outOfStockEmailsFile.createNewFile();
			FileWriter out_of_stock_emails_fw = new FileWriter(outOfStockEmailsFile.getAbsoluteFile());
			BufferedWriter out_of_stock_emails_bw = new BufferedWriter(out_of_stock_emails_fw);

				
			if (!payPalSummary.anyErrors){
				PayPalTransactionHistoryFile.PayPalOrders payPalOrders = payPalFile.getOrders();

				for (String order : payPalOrders.Orders.keySet()){
					StringBuilder orderDetailsString = new StringBuilder("Importing Order ID: "+order);
					PayPalTransactionHistoryFile.PayPalOrder pporder = payPalOrders.Orders.get(order);
					if (studentNames.containsKey(order)){
						//Insert the student name from the extras file into the order..
						pporder.orderProps.setProperty("Note",studentNames.get(order));
					}
					
					if (!database.ImportToDatabase(pporder).isEmpty()) {
						ordersInserted++;

						// separate those orders where we have the student info
						// separate those orders where we have only polos being ordered.
						if (isOutOfStock(pporder.products)){
							out_of_stock_emails_bw.write(pporder.orderProps.getProperty("From Email Address")+"; \r\n");

							out_of_stockbw.write(pporder.toHtmlPage("OUT OF STOCK"));
						} else if (pporder.orderProps.getProperty("Note").length()>0){
							// separate those orders where we have only polos being ordered.
							
//							if (isOnlyPolos(pporder.products)){
//								poloOrders.write(pporder.toHtmlPage("POLOS"));
//							} else {
								readyOrders.write(pporder.toHtmlPage("READY"));
//							}
						} else {
							// we haven't got any student info, dump the order and the email address to a separate file
							// separate those orders where we have only polos being ordered.
							noStudentInfoSummary.write(pporder.orderProps.getProperty("From Email Address")+"; \r\n");
//							if (isOnlyPolos(pporder.products)){
//								onHoldPoloOrders.write(pporder.toHtmlPage("ON HOLD POLOS"));
//							} else {
								onHoldOrders.write(pporder.toHtmlPage("ON HOLD"));
//							}

						}
						
						// write out any missing products that were found in the order
						ArrayList<String> unresolvedProducts = database.getUnResolvedOrderProducts(order);
						if (unresolvedProducts != null){
							for (String productName : unresolvedProducts){
								missing_prods_bw.append("Order: "+order +", customer: "+pporder.orderProps.getProperty("Name")+", date: "+pporder.orderProps.getProperty("Date")+", product: "+productName+"\n");							
							}
						}

					}
					/*					Properties orderProps = payPalOrders.Orders.get(order).orderProps;
					for (String field : orderProps.stringPropertyNames()) {
						orderDetailsString.append (", "+field+":"+orderProps.get(field));
					}
					System.out.println(orderDetailsString);

					if (pporder.products.isEmpty()) System.out.println("Order "+order+" has no products!!");

					for (Properties product :pporder.products){
						// get the product properties and print them
						StringBuilder productDetailsString = new StringBuilder("Product - Order ID: "+order);
						for (String field : product.stringPropertyNames()) {
							productDetailsString.append (", "+field+":"+product.get(field));
						}
						System.out.println(productDetailsString);
					}		
*/					
					
				
				}
				//write out summary of all products missing

				File allProductsMissingFile = new File(fileName+"_missingProducts.txt");
				//overwrite a file if it exists already TBD do something cleverer.
				if (allProductsMissingFile.exists()) {
					allProductsMissingFile.delete();
				}				
				allProductsMissingFile.createNewFile();
				FileWriter all_missing_prods_fw = new FileWriter(allProductsMissingFile.getAbsoluteFile());
				BufferedWriter all_missing_prods_bw = new BufferedWriter(all_missing_prods_fw);
				Set<String> unresolvedProducts = database.getAllUnResolvedProducts();
				if (unresolvedProducts!=null){
					for (String productName : unresolvedProducts){
						all_missing_prods_bw.append(productName+"\n");							
					}				
				}
				
				readyOrders.close();
				onHoldOrders.close();
//				onHoldPoloOrders.close();
//				poloOrders.close();
				noStudentInfoSummary.close();
				missing_prods_bw.close();
				all_missing_prods_bw.close();
				out_of_stockbw.close();
				out_of_stock_emails_bw.close();

			}
			logger.info("number of orders inserted to database: "+ordersInserted );


		}
	}
}