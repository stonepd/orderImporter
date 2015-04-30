package com.mildthingapps;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//import PayPalTransactionHistoryFile.PayPalOrder;

public class ReadPayPalFile {
	
	// define the parameters for database connection and set them to their defaults.
	static ArrayList<String> Filenames = new ArrayList<String>();
	
	
	private static void processArgs (String[] args){
		List<String> argsList = Arrays.asList(args);
		for ( int i=0; i<args.length; i++ ) {
			final String arg = args[i];
				
				File testFile = new File(arg);
				if (testFile.isDirectory()||testFile.isFile()){
					Filenames.add(arg);
				} else {
					System.out.println("Error - " + arg + " is not a file or directory.");
			        System.exit(1); 
				}
			}
		
		// Check that we have got all necessary parameters
		if (Filenames.isEmpty())System.out.println("Error - You need to specify the file to read.");
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// parse the parameters.
		processArgs(args);
		
		for (String fileName : Filenames){
			PayPalTransactionHistoryFile payPalFile = new PayPalTransactionHistoryFile(new File(fileName));
			
			PayPalTransactionHistoryFile.PayPalTransactionHistorySummary payPalSummary = payPalFile.getSummary();

			System.out.println("File: "+fileName+ 
					((payPalSummary.anyErrors) ?", Errors: "+payPalSummary.errorMessage:
						", number of orders: "+payPalSummary.numberOfOrders +
						", earliest order: "+payPalSummary.earliestOrder + 
						", latest order: "+payPalSummary.latestOrder)
					);

			if (!payPalSummary.anyErrors){
				PayPalTransactionHistoryFile.PayPalOrders payPalOrders = payPalFile.getOrders();

				for (String order : payPalOrders.Orders.keySet()){
					StringBuilder orderDetailsString = new StringBuilder("Order ID: "+order);
					PayPalTransactionHistoryFile.PayPalOrder pporder = payPalOrders.Orders.get(order);
					Properties orderProps = payPalOrders.Orders.get(order).orderProps;
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
				}

			}

		}
	}
}