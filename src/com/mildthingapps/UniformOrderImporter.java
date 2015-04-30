package com.mildthingapps;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.*;

public class UniformOrderImporter {

	//Set file handler to log severe errors to file
	private static Logger packagelogger = Logger.getLogger(UniformOrderImporter.class.getPackage().getName()); 
	
	private final static Logger logger = Logger.getLogger(UniformOrderImporter.class.getName()); 

	private JFrame uniformOrderImporter;
	private final Component horizontalGlue = Box.createHorizontalGlue();
	private final Component verticalGlue_1 = Box.createVerticalGlue();
	
	static File selectedDatabase;
	static File selectedImportFile;
	
    private static Preferences prefs = Preferences.userNodeForPackage(UniformOrderImporter.class);
    private static final String DATABASE_PREF = "last_database";
    private static final String FILE_PREF = "last_file";


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		FileHandler handler;
		try {
			handler = new FileHandler("OrderProcessingErrors%g.log",  1024 * 1024, 10, true);
			packagelogger.addHandler(handler);
			handler.setLevel(Level.SEVERE);
			handler.setFormatter(new SimpleFormatter());
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Try to reinstantiate the previous preferences

			        String DatabaseFilename = prefs.get(DATABASE_PREF, ".");
			        String Import_Filename = prefs.get(FILE_PREF, ".");
			        selectedDatabase = new File(DatabaseFilename);
			        selectedImportFile = new File(Import_Filename);
					UniformOrderImporter window = new UniformOrderImporter();
					window.uniformOrderImporter.setVisible(true);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UniformOrderImporter() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		uniformOrderImporter = new JFrame();
		uniformOrderImporter.setResizable(false);
		uniformOrderImporter.setBounds(100, 100, 355, 200);
		uniformOrderImporter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		uniformOrderImporter.setTitle("Uniform Order Importer");
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		uniformOrderImporter.getContentPane().add(panel, BorderLayout.WEST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0, 222, 0, 97};
		gbl_panel.rowHeights = new int[] {0, 30, 0, 15, 0, 30, 30, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		Component verticalGlue = Box.createVerticalGlue();
		GridBagConstraints gbc_verticalGlue = new GridBagConstraints();
		gbc_verticalGlue.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue.gridx = 1;
		gbc_verticalGlue.gridy = 0;
		panel.add(verticalGlue, gbc_verticalGlue);
		GridBagConstraints gbc_verticalGlue_1 = new GridBagConstraints();
		gbc_verticalGlue_1.gridheight = 4;
		gbc_verticalGlue_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue_1.gridx = 2;
		gbc_verticalGlue_1.gridy = 1;
		panel.add(verticalGlue_1, gbc_verticalGlue_1);
		
		String databaseUILabel = prefs.get(DATABASE_PREF, ".");
		if (databaseUILabel==".") {databaseUILabel="Select Database";} else
		{databaseUILabel=new File(databaseUILabel).getName();};		
		final JLabel DatabaseLabel = new JLabel(databaseUILabel);
		GridBagConstraints gbc_DatabaseLabel = new GridBagConstraints();
		gbc_DatabaseLabel.insets = new Insets(0, 0, 5, 5);
		gbc_DatabaseLabel.gridx = 1;
		gbc_DatabaseLabel.gridy = 2;
		panel.add(DatabaseLabel, gbc_DatabaseLabel);
		
		JButton btnDatabase = new JButton("Database");
		btnDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    JFileChooser chooser;
//			    if (selectedDatabase!=null) {
			    	chooser = new JFileChooser(selectedDatabase);
//			    } else {
//			    	chooser = new JFileChooser(".");
//			    }
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Access Databases", "mdb");
			    chooser.setFileFilter(filter);
			    // start from the directory of the previous file if one exists
//			    chooser.setSelectedFile(selectedImportFile);
			    int returnVal = chooser.showOpenDialog(uniformOrderImporter);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	selectedDatabase = chooser.getSelectedFile();

			    	// display the file name, remember it, the file object and the directory for next time.
//			    	try {
//						FileLabel.setText(chooser.getSelectedFile().getCanonicalPath());
			        try {
						prefs.put(DATABASE_PREF, selectedDatabase.getCanonicalPath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			    	DatabaseLabel.setText(selectedDatabase.getName());
//					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
			       System.out.println("You chose to open this file: " +
			            chooser.getSelectedFile().getName());
			    }
			}
		});

		GridBagConstraints gbc_btnDatabase = new GridBagConstraints();
		gbc_btnDatabase.insets = new Insets(0, 0, 5, 5);
		gbc_btnDatabase.gridx = 3;
		gbc_btnDatabase.gridy = 2;
		panel.add(btnDatabase, gbc_btnDatabase);
		
		String importFileLabel = prefs.get(FILE_PREF, ".");
		if (importFileLabel==".") {importFileLabel="Select File";} else
			{importFileLabel=new File(importFileLabel).getName();};
		final JLabel FileLabel = new JLabel(importFileLabel);
		GridBagConstraints gbc_FileLabel = new GridBagConstraints();
		gbc_FileLabel.insets = new Insets(0, 0, 5, 5);
		gbc_FileLabel.gridx = 1;
		gbc_FileLabel.gridy = 4;
		panel.add(FileLabel, gbc_FileLabel);
		
		JButton btnNewButton = new JButton("File/Directory");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    JFileChooser chooser;
//			    if (selectedImportFile!=null) {
			    	chooser = new JFileChooser(selectedImportFile);
//			    } else {
//			    	chooser = new JFileChooser(".");
//			    }
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Paypal CSV Files", "csv");
			    chooser.setFileFilter(filter);
			    // start from the directory of the previous file if one exists
//			    chooser.setSelectedFile(selectedImportFile);
			    int returnVal = chooser.showOpenDialog(uniformOrderImporter);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	
			    	selectedImportFile = chooser.getSelectedFile();
			        try {
						prefs.put(FILE_PREF, selectedImportFile.getCanonicalPath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			    	// display the file name, remember it, the file object and the directory for next time.
//			    	try {
//						FileLabel.setText(chooser.getSelectedFile().getCanonicalPath());
						FileLabel.setText(selectedImportFile.getName());
//					} catch (IOException e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
			       System.out.println("You chose to open this file: " +
			            chooser.getSelectedFile().getName());
			    }
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 4;
		panel.add(btnNewButton, gbc_btnNewButton);
		
		GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
		gbc_horizontalGlue.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue.gridx = 0;
		gbc_horizontalGlue.gridy = 3;
		panel.add(horizontalGlue, gbc_horizontalGlue);
		

		
		JButton btnScanFiles = new JButton("Scan Files");
		btnScanFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame orderImporter = new OrderImportWindow(selectedDatabase, selectedImportFile);
				orderImporter.setVisible(true);
			}
		});
		GridBagConstraints gbc_btnScanFiles = new GridBagConstraints();
		gbc_btnScanFiles.fill = GridBagConstraints.BOTH;
		gbc_btnScanFiles.gridwidth = 3;
		gbc_btnScanFiles.gridheight = 2;
		gbc_btnScanFiles.insets = new Insets(0, 0, 5, 5);
		gbc_btnScanFiles.gridx = 1;
		gbc_btnScanFiles.gridy = 6;
		panel.add(btnScanFiles, gbc_btnScanFiles);
	}

}
