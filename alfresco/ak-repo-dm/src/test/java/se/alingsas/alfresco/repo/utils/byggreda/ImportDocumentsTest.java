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

package se.alingsas.alfresco.repo.utils.byggreda;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class ImportDocumentsTest {
	final Mockery context = new Mockery();
	final SiteInfo siteInfo = context.mock(SiteInfo.class);
	final StoreRef storeRef = new StoreRef("workspace://SpacesStore");
	final String dummyNodeId = "cafebabe-cafe-babe-cafe-babecafebabe";
	final NodeRef dummyNodeRef = new NodeRef(storeRef, dummyNodeId);
	final FileFolderService fileFolderService = context
			.mock(FileFolderService.class);
	
	final Set<ByggRedaDocument> documents = new HashSet<ByggRedaDocument>();
	static final int NUM_SUCCESSFUL_DOCS = 10;
	static final int NUM_FAILED_DOCS = 2;
	static final String destinationPath = "Dest";

	@Before
	public void setUp() throws Exception {

		for (int i = 0; i < NUM_SUCCESSFUL_DOCS; i++) {
			ByggRedaDocument document = new ByggRedaDocument();
			document.readSuccessfully = true;
			document.film = Integer.toString(i);
			document.serialNumber = Integer.toString(i);
			document.recordNumber = Integer.toString(i) + "."
					+ Integer.toString(i);
			document.buildingDescription = "A";
			document.lastBuildingDescription = "A";
			document.address = "A";
			document.lastAddress = "A";
			document.decision = "A";
			document.forA = "A";
			document.issuePurpose = "A";
			document.note = "A";
			document.records = "A";
			document.fileName = "test.pdf";
			documents.add(document);
		}

		for (int i = 0; i < NUM_FAILED_DOCS; i++) {
			ByggRedaDocument document = new ByggRedaDocument();
			document.readSuccessfully = false;
			document.statusMsg = "Error";
			document.film = Integer.toString(i);
			document.serialNumber = Integer.toString(i);
			document.recordNumber = Integer.toString(i) + "."
					+ Integer.toString(i);
			document.buildingDescription = "A";
			document.lastBuildingDescription = "A";
			document.address = "A";
			document.lastAddress = "A";
			document.decision = "A";
			document.forA = "A";
			document.issuePurpose = "A";
			document.note = "A";
			document.records = "A";
			document.fileName = "test.pdf";
			documents.add(document);
		}
		
	}

	/*@Test
	public void testImportDocuments() {
		assertEquals(NUM_SUCCESSFUL_DOCS + NUM_FAILED_DOCS, documents.size());
		ImportDocuments id = new ImportDocuments();
		id.setFileFolderService(fileFolderService);
		
		Set<ByggRedaDocument> importDocuments = id.importDocuments(siteInfo, destinationPath, documents);
		assertNotNull(importDocuments);
		assertEquals(documents.size(), importDocuments.size());
		Iterator<ByggRedaDocument> it = documents.iterator();
		while (it.hasNext()) {
			ByggRedaDocument next = it.next();
			if (next.readSuccessfully) {
				assertEquals(dummyNodeRef, next.nodeRef);
			} else {
				assertNull(next.nodeRef);
			}
		}		
	}*/

}
