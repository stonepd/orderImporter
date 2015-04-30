package com.mildthingapps;
import java.io.File;
import java.io.FileFilter;


// This class will check for .csv files in the specified data directory
public class PayPalExportDirectoryScan {
	
	private final File directory;
	
	
	public PayPalExportDirectoryScan (String directoryName){
		File testDir = new File(directoryName);
		if (testDir.isDirectory()) {
			directory=testDir;
		} else {
			directory=null;
		}
	}
	
	public Array<File> scanForPayPalFiles(boolean includeFilesWithErrors){
		for (File candidate : directory.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv");
					}
				})){
			if (candidate.isDirectory()) scanForPayPalFiles(candidate);//recursive decent into the directories
			else ingestFile(candidate);		
	}



}
