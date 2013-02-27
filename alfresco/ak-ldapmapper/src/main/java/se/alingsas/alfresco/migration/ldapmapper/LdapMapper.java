/*
 * Copyright (C) 2012-2013 Alingsås Kommun
 *
 * This file is part of Alfresco customizations made for Alingsås Kommun
 *
 * The Alfresco customizations made for Alingsås Kommun is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Alfresco customizations made for Alingsås Kommun is distributed in the 
 * hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Alfresco customizations made for Alingsås Kommun. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package se.alingsas.alfresco.migration.ldapmapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

public class LdapMapper {

	private String sourceFile;
	private String destinationFile;
	private LdapTemplate ldapTemplate;
	private final String stringSeparator = "#!#";
	private Map<String, LotusUser> cachedUserRegistry;

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
		cachedUserRegistry = new HashMap<String, LotusUser>();
	}

	/**
	 * Perform a ldap search for each user to get the relevant properties
	 * 
	 * @param commonName
	 * @param base
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public LotusUser getUser(String commonName, String base) {
		System.out.println("Looking up user " + commonName + " at " + base
				+ " in LDAP");
		UserAttributesMapper mapper = new UserAttributesMapper();
		HashSet<LotusUser> hashSet = new HashSet<LotusUser>();
		try {
			AndFilter filterObject = new AndFilter();
			filterObject.and(new EqualsFilter("objectClass", "inetOrgPerson"));
			filterObject.and(new EqualsFilter("cn", commonName));
			List search = ldapTemplate.search(base, filterObject.encode(),
					mapper);
			hashSet.addAll(search);
		} catch (NameNotFoundException e) {
			System.out.println("No user with CN=" + commonName
					+ " found, setting name to CN + base");
			String ou = "";
			try {
				ou = " (" + base.substring(3, base.indexOf(",")) + ")";
			} catch (StringIndexOutOfBoundsException e1) {
				// Do nothing, just ignore it
			}
			LotusUser lotusUser = new LotusUser();
			lotusUser.setCommonName(commonName);
			lotusUser.setUid(commonName + ou);
			// e.printStackTrace();
			return lotusUser;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
		if (hashSet.size() == 1) {
			return (LotusUser) hashSet.toArray()[0];
		} else if (hashSet.size() == 0) {
			System.out.println("No user with CN=" + commonName
					+ " found, setting name to CN + base");
			String ou = "";
			try {
				ou = " (" + base.substring(3, base.indexOf(",")) + ")";
			} catch (StringIndexOutOfBoundsException e1) {
				// Do nothing, just ignore it
			}
			LotusUser lotusUser = new LotusUser();
			lotusUser.setCommonName(commonName);
			lotusUser.setUid(commonName + ou);
			return lotusUser;
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

		if (notesUserPath.length() > 0) {
			String commonName;
			String base;
			if (notesUserPath.indexOf("CN=") == -1) {
				String[] split = notesUserPath.split("/");
				if (split.length==3) {
					commonName = split[0];
					base = "OU="+split[1]+",O="+split[2];
				} else {
					commonName = notesUserPath;
					base = "";
				}
			} else {
				commonName = notesUserPath.substring(3,
						notesUserPath.indexOf("/"));
				base = notesUserPath.substring(
						notesUserPath.indexOf("/") + 1).replace("/", ",");
			}
			LotusUser user;
			if (!cachedUserRegistry.containsKey(notesUserPath)) {
				user = getUser(commonName, base);
				cachedUserRegistry.put(notesUserPath, user);
			} else {
				user = cachedUserRegistry.get(notesUserPath);
			}
			if (null != user) {
				if (user.getUid().equalsIgnoreCase("administ")) {
					user.setUid("admin");
				}
				columns[1] = user.getUid();
			}
		} else { // If author is not set, then set it to admin
			columns[1] = "admin";
		}

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
						String nextLine = scanner.nextLine();
						if (nextLine.length() == 0) {
							continue;
						}
						String fixedLine = extractAndReplaceUid(nextLine);
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
