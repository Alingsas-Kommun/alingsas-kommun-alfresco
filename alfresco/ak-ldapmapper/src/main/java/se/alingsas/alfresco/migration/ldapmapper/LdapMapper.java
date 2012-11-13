package se.alingsas.alfresco.migration.ldapmapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;

import org.springframework.ldap.core.LdapTemplate;

public class LdapMapper {

	private String sourceFile;
	private String destinationFile;
	private Properties properties;
	private LdapTemplate ldapTemplate;

	public LdapMapper(String sourceFile, String destinationFile,
			Properties properties) {
		this.sourceFile = sourceFile;
		this.destinationFile = destinationFile;
		this.properties = properties;
	}

	private LotusUser getUser(String commonName, String base) {
		System.out.println("Looking up user " + commonName + " at " + base
				+ " in LDAP");
		UserAttributesMapper mapper = new UserAttributesMapper();
		HashSet<LotusUser> hashSet = new HashSet<LotusUser>(
				ldapTemplate
						.search(base, "(objectClass=inetOrgPerson)", mapper));
		if (hashSet.size() == 1) {
			return (LotusUser) hashSet.toArray()[0];
		} else if (hashSet.size() == 0) {
			System.out.println("No user with CN=" + commonName
					+ " found, returning null");
			return null;
		} else {
			System.out.println("More than one user with CN=" + commonName
					+ " found, returning null");
			return null;
		}
	}

	private String extractAndReplaceUid(String row) {
		String[] columns = row.split("#!#");
		String notesUserPath = columns[1];
		String commonName = notesUserPath.substring(2, notesUserPath.indexOf("/OU"));
		String base = notesUserPath.substring(notesUserPath.indexOf("/OU")+1).replace("/",",");
		LotusUser user = getUser(commonName, base);
		columns[1] = user.getUid();
		
		return "";
	}

	public void run() {
		try {
			FileInputStream fis = new FileInputStream(sourceFile);
			Scanner scanner = new Scanner(fis, "UTF-8");
			FileOutputStream fos = new FileOutputStream(destinationFile);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			String NL = System.getProperty("line.separator");
			System.out.println("Reading from file " + sourceFile);
			System.out.println("Writing to file " + destinationFile);
			try {
				try {
					while (scanner.hasNextLine()) {
						String fixedLine = extractAndReplaceUid(scanner
								.nextLine());
						out.write(fixedLine + NL);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					scanner.close();
				}
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
