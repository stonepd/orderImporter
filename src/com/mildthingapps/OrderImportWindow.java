package com.mildthingapps;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.mildthingapps.PayPalTransactionHistoryFile.PayPalOrder;

//import PayPalTransactionHistoryFile.PayPalOrder;

public class OrderImportWindow<E> extends JFrame {
	
	private final static Logger logger = Logger.getLogger(OrderImportWindow.class.getName()); 
	
	File selectedImportFile;
	private HashMap<String,String> extraStudentNames = null; 
	
	
	private String checkForExtraStudent (String orderID){
		if (extraStudentNames==null){
			//Read in the "Extra Student Info" file.. TBD read name from input params, make this more generic
			ExtraStudentsFile extraStudents;
			try {
				extraStudents = new ExtraStudentsFile(selectedImportFile.getPath());
				extraStudentNames = extraStudents.getStudents();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return(extraStudentNames.get(orderID));

	}
	
	private void clearExtraStudentCache() {
		extraStudentNames=null;
	}
	
	class ComboItem
	{
	    private String key;
	    private String value;

	    public ComboItem(String key, String value)
	    {
	        this.key = key;
	        this.value = value;
	    }

	    @Override
	    public String toString()
	    {
	        return key;
	    }

	    public String getKey()
	    {
	        return key;
	    }

	    public String getValue()
	    {
	        return value;
	    }
	}
	
	OrderProcessing database;
	ArrayList<String> orderIDsInList = new ArrayList<String>();
	
	PayPalTransactionHistoryFile.PayPalOrders payPalOrders;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OrderImportWindow frame = new OrderImportWindow(new File("DB/Uniform2013.mdb"), new File("JUnit/TestData/Download20150318b.csv"));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @param selectedImportFile 
	 * @param selectedDatabase 
	 */
	public OrderImportWindow(File selectedDatabase, File selectedImportFile) {
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setTitle("Select Orders to Import.");

		this.selectedImportFile = selectedImportFile;
		
		final DefaultListModel<String> listModel = new DefaultListModel<String>();
		
		final JList<String> orderList = new JList<String>(listModel);
		
		final JComboBox eventCombo = new JComboBox();


		setBounds(100, 100, 600, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		final JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {50, 0, 0, 60, 21};
		gbl_panel.rowHeights = new int[]{22, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnSelectAll = new JButton("Select All");
		GridBagConstraints gbc_btnSelectAll = new GridBagConstraints();
		gbc_btnSelectAll.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnSelectAll.insets = new Insets(0, 0, 0, 5);
		gbc_btnSelectAll.gridx = 0;
		gbc_btnSelectAll.gridy = 0;
		panel.add(btnSelectAll, gbc_btnSelectAll);
		btnSelectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				orderList.setSelectionInterval(0, listModel.size()-1);
			}
		});

		
		JButton btnImportOrders = new JButton("Import Orders");
		GridBagConstraints gbc_btnImportOrders = new GridBagConstraints();
		gbc_btnImportOrders.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnImportOrders.insets = new Insets(0, 0, 0, 5);
		gbc_btnImportOrders.gridx = 1;
		gbc_btnImportOrders.gridy = 0;
		panel.add(btnImportOrders, gbc_btnImportOrders);
		btnImportOrders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String eventID = ((ComboItem)eventCombo.getSelectedItem()).getValue();
				String importSummary = importSelectedOrders(orderList,eventID);
				JOptionPane messagePane = new JOptionPane();
				messagePane.showMessageDialog(null, importSummary);
				orderList.clearSelection();
			}

		});		
		
		JButton btnRemoveOrders = new JButton("Remove Orders");
		GridBagConstraints gbc_btnRemoveOrders = new GridBagConstraints();
		gbc_btnRemoveOrders.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveOrders.insets = new Insets(0, 0, 0, 5);
		gbc_btnRemoveOrders.gridx = 2;
		gbc_btnRemoveOrders.gridy = 0;
		panel.add(btnRemoveOrders, gbc_btnRemoveOrders);
		btnRemoveOrders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String importSummary = removeSelectedOrders(orderList);
				JOptionPane messagePane = new JOptionPane();
				messagePane.showMessageDialog(null, importSummary);
				orderList.clearSelection();
			}

		});		
		
		
		
		JLabel lblEvent = new JLabel("Event");
		GridBagConstraints gbc_lblEvent = new GridBagConstraints();
		gbc_lblEvent.insets = new Insets(0, 0, 0, 5);
		gbc_lblEvent.anchor = GridBagConstraints.EAST;
		gbc_lblEvent.gridx = 3;
		gbc_lblEvent.gridy = 0;
		panel.add(lblEvent, gbc_lblEvent);
		
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox.gridx = 4;
		gbc_comboBox.gridy = 0;
		panel.add(eventCombo, gbc_comboBox);
		populateEventCombo(eventCombo);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(orderList);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		this.setVisible(true);
		
		//Populate the list with orders from the processed file
		populateOrderList(listModel, selectedDatabase, selectedImportFile);
	}
	
	//get the valid event values from the selected database
	private void populateEventCombo(JComboBox eventCombo) {
		// TBD get values from DB. 
		// TBD default this to a preference.
		// from diana - We ONLY use September Sales (all orders from 1/8 - 30/9 in each year), 
		// Polo/Black Polo Shirts, New Intake, Black Jumpers self explanatory, Summer for Y7-10) 
		// after request to order for September, only orders marked as urgent will be  dealt with 
		// as Unclassified and Unclassified for everything else.  No longer Uniform Sale, Xmas, GCSE)
		eventCombo.addItem(new ComboItem("Autoclassify", "0"));
		eventCombo.addItem(new ComboItem("Unclassified", "1"));
		eventCombo.addItem(new ComboItem("New Intake", "2"));
		eventCombo.addItem(new ComboItem("Black jumpers", "3"));
		eventCombo.addItem(new ComboItem("Polo Shirts", "4"));
		eventCombo.addItem(new ComboItem("Uniform sale", "5"));
		eventCombo.addItem(new ComboItem("Xmas fair", "6"));
		eventCombo.addItem(new ComboItem("GCSE polo shirts", "7"));
		eventCombo.addItem(new ComboItem("Black polo shirts", "8"));
		eventCombo.addItem(new ComboItem("September sales", "9"));
		eventCombo.addItem(new ComboItem("Summer orders", "10"));
		
		// TODO Auto-generated method stub
		
	}

	private void populateOrderList(DefaultListModel<String> listModel,
			File selectedDatabase, File selectedImportFile) {
		try {
			database = new OrderProcessing(selectedDatabase.getPath());
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PayPalTransactionHistoryFile payPalFile = new PayPalTransactionHistoryFile(selectedImportFile);
		PayPalTransactionHistoryFile.PayPalTransactionHistorySummary payPalSummary = payPalFile.getSummary();
	
		if (!payPalSummary.anyErrors){
			payPalOrders = payPalFile.getOrders();
			
			// create a date index

			// formatter object for extracting order dates
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date orderDate;

			// structure to hold the date sorted orderIDs.
			TreeMap <Date,String> ordersByDate = 
					new TreeMap<Date,String>();

			for (String orderID : payPalOrders.Orders.keySet()){
				PayPalTransactionHistoryFile.PayPalOrder ppOrder = payPalOrders.Orders.get(orderID);
				String dateToken = ppOrder.orderProps.getProperty("Date");
				String timeToken = ppOrder.orderProps.getProperty("Time");

				try {
					orderDate = format.parse(dateToken+' '+timeToken);
					while (ordersByDate.containsKey(orderDate)){
						orderDate.setTime(orderDate.getTime()+1);//add a millisecond to make the time unique. otherwise the orders will overwrite in the treemap.
					}
					ordersByDate.put(orderDate, orderID);
				} catch (ParseException e) {
					// TODO Auto-generated catch block TBD log error
					e.printStackTrace();
				}
			}
						

//			for (String order : payPalOrders.Orders.keySet()){ // order by order id
			for (Date thisOrderDate : ordersByDate.keySet()){ //order by date
				String order = ordersByDate.get(thisOrderDate);
				StringBuilder orderDetailsString = new StringBuilder("Importing Order ID: "+order);
				PayPalTransactionHistoryFile.PayPalOrder ppOrder = payPalOrders.Orders.get(order);
				
				// TBD Look for the student name in the "extra" files
				String studentName = ppOrder.orderProps.getProperty("Note");
				if (studentName.length()==0){
					String newStudentDetails = checkForExtraStudent(order);
					if (newStudentDetails!=null){
						//Insert the student name from the extras file into the order..
						ppOrder.orderProps.setProperty("Note",newStudentDetails);
						studentName=newStudentDetails;
					} else {

						studentName="Unknown Student";							
					}
				}


				listModel.addElement(order+", "+ppOrder.orderProps.getProperty("Date")+", "+ppOrder.orderProps.getProperty("Name")+", "+studentName);
				orderIDsInList.add(listModel.size()-1, order); //keep this is step with the list TBD probably not best practice
			}
		}
	}
	
	// This returns a summary of the orders imported - to be displayed to the user.
	private String importSelectedOrders(JList<String> orderList, String eventID) {
		int ordersInserted = 0;
		int noStudentdata = 0;
		String firstOrderID = null;
		String lastOrderID = null;

		
		database.clearUnResolvedProducts();
		
		//Sort the orders into date sequence, so I insert the earliest order first.

		// structure to hold the date sorted orders.
		TreeMap <Date,PayPalTransactionHistoryFile.PayPalOrder> ordersByDate = OrderDateIndex(orderList);
						
		// Now insert the orders into the database.
		for (Date thisOrderDate : ordersByDate.keySet()){
			PayPalTransactionHistoryFile.PayPalOrder ppOrder = ordersByDate.get(thisOrderDate);
			String orderID = ppOrder.orderProps.getProperty("Transaction ID");
			
			try {
				ArrayList<String> importedOrderIDs = database.ImportToDatabase(ppOrder, eventID); //TBD process any errors
				if (!importedOrderIDs.isEmpty()){
					ordersInserted++;
					//Keep track of the first and last inserted orderIDs.
					if (firstOrderID==null) firstOrderID = importedOrderIDs.get(0);
					lastOrderID = importedOrderIDs.get(importedOrderIDs.size()-1);
					
					if (payPalOrders.Orders.get(orderID).orderProps.getProperty("Note").length()==0){
						noStudentdata++;
						logger.severe("Missing Student Info, Order: "+orderID +", customer: "+ppOrder.orderProps.getProperty("Name")+", date: "+ppOrder.orderProps.getProperty("Date")+"\n");
					}
				}
			} catch (ClassNotFoundException | SQLException | ParseException e) { //TBD
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayList<String> unresolvedProducts = database.getUnResolvedOrderProducts(orderID);
			if (unresolvedProducts != null){
				for (String productName : unresolvedProducts){
					logger.severe("Missing Product, Order: "+orderID +", customer: "+ppOrder.orderProps.getProperty("Name")+", date: "+ppOrder.orderProps.getProperty("Date")+", product: "+productName+"\n");							
				}
			}

		}
		
		StringBuilder resultString = new StringBuilder("Imported "+ordersInserted+" paypal order"+((ordersInserted==1)?"":"s"));
		if (firstOrderID!=null)
			resultString.append("\ndatabase orderIDs: "+firstOrderID+ ((!firstOrderID.equals(lastOrderID))?" to "+lastOrderID:"")+".");
		
		
		// Report on any missing student information
		if (noStudentdata !=0){
			resultString.append("\n"+noStudentdata+" student record"+((noStudentdata==1)?"":"s")+ " missing.");
		}

		// Report on any products that we couldn't find
		Set<String> unresolvedProducts = database.getAllUnResolvedProducts();
		if (unresolvedProducts!=null&&unresolvedProducts.size()>0){
			resultString.append("\nProducts Missing:");
			for (String productName : unresolvedProducts){
				resultString.append("\n"+productName);							
			}				
		}
		
		// TBD Log the order import failures to file... use the logger?
		
		return resultString.toString();
		
	}

	private TreeMap<Date, PayPalOrder> OrderDateIndex(JList<String> orderList) {
		// formatter object for extracting order dates
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date orderDate;

		// structure to hold the date sorted orders.
		TreeMap <Date,PayPalTransactionHistoryFile.PayPalOrder> ordersByDate = 
				new TreeMap<Date,PayPalTransactionHistoryFile.PayPalOrder>();

		for (int selectedOrder : orderList.getSelectedIndices()){
			String orderID = orderIDsInList.get(selectedOrder);
			PayPalTransactionHistoryFile.PayPalOrder ppOrder = payPalOrders.Orders.get(orderID);
			String dateToken = ppOrder.orderProps.getProperty("Date");
			String timeToken = ppOrder.orderProps.getProperty("Time");

			try {
				orderDate = format.parse(dateToken+' '+timeToken);
				while (ordersByDate.containsKey(orderDate)){
					orderDate.setTime(orderDate.getTime()+1);//add a millisecond to make the time unique. otherwise the orders will overwrite in the treemap.
				}
				ordersByDate.put(orderDate, ppOrder);
			} catch (ParseException e) {
				// TODO Auto-generated catch block TBD log error
				e.printStackTrace();
			}
		}
		
		return ordersByDate;
	}

	// This returns a summary of the orders impported - to be displayed to the user.
	private String removeSelectedOrders(JList<String> orderList) {
		int ordersRemoved = 0;
		for (int selectedOrder : orderList.getSelectedIndices()){
			String orderID = orderIDsInList.get(selectedOrder);

				boolean removed = database.RemoveFromDatabase(orderID); //TBD process any errors
				if (removed){
					ordersRemoved++;
				}
		}
		
		StringBuilder resultString = new StringBuilder("Removed "+ordersRemoved+" order"+((ordersRemoved==1)?"":"s"));
	
		return resultString.toString();
		
	}
}
