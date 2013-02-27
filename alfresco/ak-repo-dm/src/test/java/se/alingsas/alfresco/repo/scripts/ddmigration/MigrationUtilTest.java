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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class MigrationUtilTest {

	@Test
	public void testReadDocuments() {
		MigrationUtil mu = new MigrationUtil();
		MigrationCollection readDocuments = mu.readDocuments(null, null);
		assertNotNull(readDocuments);		
		assertEquals(0,readDocuments.noOfDocuments());
		assertEquals(0,readDocuments.noOfDocumentVersions());
		readDocuments = mu.readDocuments(new File("src/test/resources/ddmigration/file_metadata_test_small.txt"), null);
		assertNotNull(readDocuments);
		assertEquals(3,readDocuments.noOfDocuments());
		assertEquals(6,readDocuments.noOfDocumentVersions());
	}

	@Test
	public void testParseKnownQNames() {
		MigrationUtil mu = new MigrationUtil();
		try {
			mu.parseKnownQNames("non-exisiting-type");
			assertTrue("Expected exception to be thrown", false);
		} catch(IllegalArgumentException e) {
			
		}
		assertEquals(AkDmModel.TYPE_AKDM_INSTRUCTION, mu.parseKnownQNames("Anvisning"));
		assertEquals(AkDmModel.TYPE_AKDM_ORDINANCE, mu.parseKnownQNames("Författning"));
		assertEquals(AkDmModel.TYPE_AKDM_MANUAL, mu.parseKnownQNames("Handbok"));
		assertEquals(AkDmModel.TYPE_AKDM_PROTOCOL, mu.parseKnownQNames("Protokoll"));
		assertEquals(AkDmModel.TYPE_AKDM_GENERAL_DOC, mu.parseKnownQNames("Generellt Dokument"));
		assertEquals(AkDmModel.TYPE_AKDM_ECONOMY_DOC, mu.parseKnownQNames("Ekonomidokument"));
	}
	
	@Test
	public void testValidateFiles() {
		MigrationUtil mu = new MigrationUtil();
		MigrationCollection readDocuments = mu.readDocuments(new File("src/test/resources/ddmigration/file_metadata_test_small.txt"), "src/test/resources/ddmigration/Files/");
		Set<AlingsasDocument> allVersions = readDocuments.getAllDocumentsAndVersions();
		Iterator<AlingsasDocument> it = allVersions.iterator();
		while (it.hasNext()) {
			AlingsasDocument next = it.next();
			next.fileExtension = "txt";
			assertTrue(mu.validateFile(next));
		}
		
	}
	
}
