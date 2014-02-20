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

package se.alingsas.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class TemplatedContentFilter extends AbstractFilter<Object, NodeRef> {
	private static final Logger LOG = Logger
			.getLogger(TemplatedContentFilter.class);
	private static ServiceRegistry serviceRegistry;

	public static final String PROP_NAME = "prop_cm_name";
	public static final String ASSOC_TEMPLATE = "assoc_akdm_basedOnTemplate_added";

	/**
	 * Will copy template contents into the newly created node if a template was
	 * set
	 */
	@Override
	public void afterPersist(Object item, FormData data, NodeRef persistedObject) {
	  LOG.trace("afterPersist begin");
	  NodeService nodeService = serviceRegistry.getNodeService();
		FieldData fieldData = data.getFieldData(ASSOC_TEMPLATE);
		if (fieldData != null
				&& NodeRef.isNodeRef((String) fieldData.getValue())) {
			NodeRef templateNode = new NodeRef((String) fieldData.getValue());
			LOG.debug("Updating node " + persistedObject.toString()
					+ " based on template " + templateNode);
			if (nodeService.exists(templateNode)) {
				FileFolderService fileFolderService = serviceRegistry
						.getFileFolderService();
				ContentReader reader = fileFolderService
						.getReader(templateNode);
				ContentWriter writer = fileFolderService
						.getWriter(persistedObject);
				writer.putContent(reader);
			} else {
				LOG.warn("The template " + templateNode
						+ " could not be found.");
			}
			nodeService.removeAssociation(persistedObject, templateNode, AkDmModel.ASSOC_AKDM_BASEDONTEMPLATE);
			if (nodeService.hasAspect(persistedObject, AkDmModel.ASPECT_AKDM_TEMPLATEDASPECT)) {
				nodeService.removeAspect(persistedObject, AkDmModel.ASPECT_AKDM_TEMPLATEDASPECT);
			}
				
		}
		LOG.trace("afterPersist end");
	}

	@Override
	public void afterGenerate(Object item, List fields, List forcedFields,
			Form form, Map context) {
	}

	@Override
	public void beforeGenerate(Object item, List fields, List forcedFields,
			Form form, Map context) {
	}

	/**
	 * Will make sure the name contains the same document extension as the
	 * template
	 */
	@Override
	public void beforePersist(Object item, FormData data) {
	  LOG.trace("beforePersist begin");	  
		FieldData fieldData = data.getFieldData(ASSOC_TEMPLATE);
		if (fieldData != null
				&& NodeRef.isNodeRef((String) fieldData.getValue())) {
			NodeRef templateNode = new NodeRef((String) fieldData.getValue());
			NodeService nodeService = serviceRegistry.getNodeService();
			fieldData = data.getFieldData(PROP_NAME);
			String name = (String) fieldData.getValue();
			
			if (nodeService.exists(templateNode)) {
				String templateName = (String) nodeService.getProperty(
						templateNode, ContentModel.PROP_NAME);
				String templateExtension = FilenameUtils
						.getExtension(templateName);
				String thisExtension = FilenameUtils.getExtension(name);
				if (!templateExtension.equalsIgnoreCase(thisExtension)) {
					LOG.debug("Detected that file extension of new document does not match the one from the template. Changing the file extension to "
							+ templateExtension);
					name = name + "." + templateExtension;
					data.addFieldData(PROP_NAME, name, true);
				}
				
			} else {
				LOG.warn("The template " + templateNode
						+ " could not be found.");
			}
		}
		LOG.trace("beforePersist end");
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		TemplatedContentFilter.serviceRegistry = serviceRegistry;
	}
}
