package se.alingsas.alfresco.migration.ldapmapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length<3) {
			System.out.println("Error: Invalid arguments");
			System.out.println("Usage: java -cp ldapmapper-1.0.0.jar se.alingsas.alfresco.migration.ldapmapper.Main sourcefile targetfile propertyfile.properties");
		} else {
			String sourceFile = args[0];
			String destinationFile = args[1];
			String propertyfile = args[2];
			
			Properties properties = new Properties();
			
			try {
				properties.load(new FileInputStream(propertyfile));
			} catch (FileNotFoundException e) {
				System.out.println("Error: Could not find property file "+ propertyfile);
			} catch (IOException e) {					
				System.out.println("Error: Error reading "+ propertyfile);
				e.printStackTrace();
				return;
			}
			
			File file=new File(sourceFile);
			if (!file.exists()) {
				System.out.println("Error: Cound not find source file "+ sourceFile);
				return;
			}
			
			file=new File(destinationFile);
			if (file.exists()) {
				System.out.println("Error: Destionation file already exists "+ destinationFile);
				return;
			}
			
			LdapMapper ldapMapper = new LdapMapper(sourceFile, destinationFile, properties);
			ldapMapper.run();
		}
		
	}

}
