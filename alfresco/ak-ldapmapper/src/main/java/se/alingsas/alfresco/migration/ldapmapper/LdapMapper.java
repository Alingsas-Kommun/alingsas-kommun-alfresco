package se.alingsas.alfresco.migration.ldapmapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Scanner;

import org.springframework.ldap.core.LdapTemplate;

public class LdapMapper {

	private String sourceFile;
	private String destinationFile;
	private LdapTemplate ldapTemplate;
	private final String stringSeparator = "#!#";

	/**
	 * Constructor
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 * @param properties
	 */
	public LdapMapper(String sourceFile, String destinationFile) {
		this.sourceFile = sourceFile;
		this.destinationFile = destinationFile;
	}

	/**
	 * Perform a ldap search for each user to get the relevant properties
	 * 
	 * @param commonName
	 * @param base
	 * @return
	 */
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

	/**
	 * Implode an array of strings with a separator of choise
	 * 
	 * @param strArr
	 * @param separator
	 * @return
	 */
	private String implodeStringArray(String[] strArr, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String str : strArr) {
			sb.append(str);
			sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Take the Notes id of a user and lookup its uid
	 * 
	 * @param row
	 *            A single row of data separated by "stringSeparator"
	 * @return The row with the uid instead of the Notes id
	 */
	private String extractAndReplaceUid(String row) {
		String[] columns = row.split(stringSeparator);
		String notesUserPath = columns[1];
		String commonName = notesUserPath.substring(3,
				notesUserPath.indexOf("/OU"));
		String base = notesUserPath.substring(notesUserPath.indexOf("/OU") + 1)
				.replace("/", ",");
		LotusUser user = getUser(commonName, base);
		columns[1] = user.getUid();
		return implodeStringArray(columns, stringSeparator);
	}

	/**
	 * Triggers the function to read from source file and write to destination
	 * file translating the notes user id into uid
	 */
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

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
}
