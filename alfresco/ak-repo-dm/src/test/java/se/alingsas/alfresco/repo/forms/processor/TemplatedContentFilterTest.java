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

/**
 * 
 */
package se.alingsas.alfresco.repo.forms.processor;

import static org.junit.Assert.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class TemplatedContentFilterTest {
	final Mockery context = new Mockery();

	final ContentReader contentReader = context.mock(ContentReader.class);
	final NodeService nodeService = context.mock(NodeService.class);
	final ServiceRegistry serviceRegistry = context.mock(ServiceRegistry.class);
	final ContentWriter contentWriter = context.mock(ContentWriter.class);
	final FileFolderService fileFolderService = context
			.mock(FileFolderService.class);
	final StoreRef storeRef = new StoreRef("workspace://SpacesStore");
	final String dummyNodeId1 = "cafebabe-cafe-babe-cafe-babecafebab1";
	final String dummyNodeId2 = "cafebabe-cafe-babe-cafe-babecafebab2";
	final NodeRef exitingNodeRef = new NodeRef(storeRef, dummyNodeId1);
	final NodeRef nonExitingNodeRef = new NodeRef(storeRef, dummyNodeId2);
	final String validFileName = "filename.txt";
	final String incompleteFilename = "filename";
	TemplatedContentFilter tcf;
	/**
	 * @throws java.lang.Exception
	 */

	
	@Before
	public void setUp() throws Exception {
		tcf = new TemplatedContentFilter();
		tcf.setServiceRegistry(serviceRegistry);
		context.checking(new Expectations() {
			{
				allowing(serviceRegistry).getNodeService();
				will(returnValue(nodeService));
				allowing(serviceRegistry).getFileFolderService();
				will(returnValue(fileFolderService));
				allowing(nodeService).exists(exitingNodeRef);
				will(returnValue(true));
				allowing(nodeService).exists(nonExitingNodeRef);
				will(returnValue(false));
				allowing(nodeService).getProperty(exitingNodeRef, ContentModel.PROP_NAME);
				will(returnValue(validFileName));
				allowing(fileFolderService).getReader(exitingNodeRef);
				will(returnValue(contentReader));
				allowing(fileFolderService).getWriter(exitingNodeRef);
				will(returnValue(contentWriter));
				allowing(contentWriter).putContent(contentReader);
				allowing(nodeService).removeAssociation(with(any(NodeRef.class)), with(any(NodeRef.class)), with(any(QName.class)));
				allowing(nodeService).hasAspect(with(any(NodeRef.class)), with(any(QName.class)));
				will(returnValue(true));
				allowing(nodeService).removeAspect(with(any(NodeRef.class)), with(any(QName.class)));
			}
		});
	}

	@Test
	public void testBeforePersist() {
		FormData formData = new FormData();
		
		//Test that extension gets properly added when missing
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.beforePersist(null, formData);
		assertEquals(validFileName, formData.getFieldData(TemplatedContentFilter.PROP_NAME).getValue());
		
		//Test missing name
		//formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.beforePersist(null, formData);
		assertEquals(validFileName, formData.getFieldData(TemplatedContentFilter.PROP_NAME).getValue());
				
		//Test that extension is left alone if it matches the templated one
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, validFileName, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.beforePersist(null, formData);
		assertEquals(validFileName, formData.getFieldData(TemplatedContentFilter.PROP_NAME).getValue());
		
		//Test missing template association
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		//formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.beforePersist(null, formData);
		assertEquals(incompleteFilename, formData.getFieldData(TemplatedContentFilter.PROP_NAME).getValue());
		
		//Test non-existing template
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, nonExitingNodeRef.toString(), true);
		tcf.beforePersist(null, formData);
		assertEquals(incompleteFilename, formData.getFieldData(TemplatedContentFilter.PROP_NAME).getValue());
		
		//Test invalid noderef template
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, dummyNodeId1, true);
		tcf.beforePersist(null, formData);
		assertEquals(incompleteFilename, formData.getFieldData(TemplatedContentFilter.PROP_NAME).getValue());
		
	}

	@Test
	public void testAfterPersist() {
		
		//Test its working when all fields exist
		FormData formData = new FormData();		
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, validFileName, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.afterPersist(null, formData, exitingNodeRef);
		
		//Test missing name
		//formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.afterPersist(null, formData, exitingNodeRef);
				
		//Test that extension is left alone if it matches the templated one
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, validFileName, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.afterPersist(null, formData, exitingNodeRef);
		
		//Test missing template association
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		//formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, exitingNodeRef.toString(), true);
		tcf.afterPersist(null, formData, exitingNodeRef);
		
		//Test non-existing template
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, nonExitingNodeRef.toString(), true);
		tcf.afterPersist(null, formData, exitingNodeRef);
		
		//Test invalid noderef template
		formData = new FormData();
		formData.addFieldData(TemplatedContentFilter.PROP_NAME, incompleteFilename, true);
		formData.addFieldData(TemplatedContentFilter.ASSOC_TEMPLATE, dummyNodeId1, true);
		tcf.afterPersist(null, formData, exitingNodeRef);
		

	}
}
