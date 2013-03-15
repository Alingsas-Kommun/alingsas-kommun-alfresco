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

package se.alingsas.alfresco.repo.scripts.ddmigration;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds a collection of documents and versions with ordering intact
 * @author mars
 *
 */
public class MigrationCollection {
	private Map<String, Set<AlingsasDocument>> collection;
	public MigrationCollection() {
		collection = new HashMap<String, Set<AlingsasDocument>>();
	}
	
	public void put(String key, AlingsasDocument value) {
		if (collection.containsKey(key)) {
			Set<AlingsasDocument> list = collection.get(key);
			list.add(value);
		} else {
			Comparator<AlingsasDocument> comparator = new VersionComparator();
			Set<AlingsasDocument> newList = new TreeSet<AlingsasDocument>(comparator);
			newList.add(value);
			collection.put(key, newList);
		}
	}
	
	public Set<AlingsasDocument> get(String key) {
		return collection.get(key);
	}
	
	public Set<String> getKeys() {
		return collection.keySet();
	}
	
	public Set<AlingsasDocument> getAllDocumentsAndVersions() {
		Comparator<AlingsasDocument> comparator = new VersionComparator();
		Set<AlingsasDocument> newList = new TreeSet<AlingsasDocument>(comparator);
		
		Iterator<Set<AlingsasDocument>> it = collection.values().iterator();
		while (it.hasNext()) {			
			newList.addAll(it.next());
		}
		
		return newList;
	}
	
	public Set<AlingsasDocument> remove(String key) {
		return collection.remove(key);
	}
	
	public int noOfDocuments() {
		return collection.size();
	}
	
	public int noOfDocumentVersions() {
		Iterator<Set<AlingsasDocument>> it = collection.values().iterator();
		int counter = 0;
		while (it.hasNext()) {
			Set<AlingsasDocument> next = it.next();
			counter += next.size();
		}
		return counter;
	}
}
