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
package se.alingsas.alfresco.repo.utils.documentnumber;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.model.AkDmModel;

/**
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class DocumentNumberUtil {

	private static final Logger LOG = Logger.getLogger(DocumentNumberUtil.class);

	private ServiceRegistry serviceRegistry;
	private Repository repositoryHelper;
	private RetryingTransactionHelper retryingTransactionHelper;

	private static NodeRef cachedFileRef;

	public static final String SETTINGS = "Settings";
	public static final String DOCUMENT_NUMBER_SETTINGS = "DocumentNumber.settings";

	/**
	 * Sets the document number on a node if it doesn't already exist
	 * 
	 * Potential exceptions should be caught to make sure that files can always be
	 * created in the system even if generating a document number fails.
	 * 
	 * @param nodeRef
	 */
	public void setDocumentNumber(final NodeRef nodeRef, boolean replace) throws Exception {
		if (LOG.isTraceEnabled()) {
			LOG.trace("setDocumentNumber");
		}
		final NodeService nodeService = serviceRegistry.getNodeService();
		final LockService lockService = serviceRegistry.getLockService();
		if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, AkDmModel.ASPECT_AKDM_COMMON)) {
			String docNumber = (String) nodeService.getProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER);
			if (!StringUtils.hasText(docNumber) || replace == true) {
				// TODO add transaction
				String documentNumber = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
					public String doWork() throws Exception {

						String documentNumber = retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
							public String execute() throws Throwable {
								return getNextDocumentNumber();
							}
						}, false, true);

						if (documentNumber != null && StringUtils.hasText(documentNumber)) {
							lockService.suspendLocks();
							nodeService.setProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER, documentNumber);
							lockService.enableLocks();
							if (LOG.isDebugEnabled())
								LOG.debug("Setting document number for " + documentNumber + " for node " + nodeRef.toString());
						}
						return documentNumber;

					}
				}, AuthenticationUtil.getSystemUserName());

			}
		}
	}

	/**
	 * Generates the next document number. Calling this method will increase the
	 * global counter for document numbers.
	 * 
	 * Potential exceptions should be caught to make sure that files can always be
	 * created in the system even if generating a document number fails.
	 * 
	 * @return Generated document number
	 */
	synchronized public String getNextDocumentNumber() throws Exception {
		// Number format
		// YYYY-MM-DD-XXX-XXX
		if (LOG.isTraceEnabled()) {
			LOG.trace("getNextDocumentNumber");
		}
		NodeService nodeService = serviceRegistry.getNodeService();

		if (cachedFileRef == null) {
			FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
			NodeRef docNumSettingsRef = null;
			NodeRef settingsNodeRef = fileFolderService.searchSimple(repositoryHelper.getCompanyHome(), SETTINGS);
			if (settingsNodeRef == null) {
				LOG.info("Settings folder does not exist, creating it.");
				// Settings folder does not exist, create it
				settingsNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), SETTINGS, ContentModel.TYPE_FOLDER).getNodeRef();
			}
			if (nodeService.exists(settingsNodeRef)) {
				docNumSettingsRef = fileFolderService.searchSimple(settingsNodeRef, DOCUMENT_NUMBER_SETTINGS);
				if (docNumSettingsRef == null) {
					LOG.info("Document numbering setting file does not exist, creating it");
					docNumSettingsRef = fileFolderService.create(settingsNodeRef, DOCUMENT_NUMBER_SETTINGS, ContentModel.TYPE_CONTENT).getNodeRef();
				}
				cachedFileRef = docNumSettingsRef;

			} else {
				throw new Exception("Settings folder does not exist and could not be created");
			}
		}

		if (nodeService.exists(cachedFileRef)) {
			if (!nodeService.hasAspect(cachedFileRef, AkDmModel.ASPECT_AKDM_DOCUMENT_NUMBER_SETTINGS)) {
				LOG.info("Document numbering setting file is missing aspect, adding it.");

				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				nodeService.addAspect(cachedFileRef, AkDmModel.ASPECT_AKDM_DOCUMENT_NUMBER_SETTINGS, aspectProperties);
			}
			return incrementDocumentNumber(cachedFileRef);

		} else {
			throw new Exception("Document number settings does not exist and could not be created");
		}
	}

	/**
	 * Will increment the document number and return it. This method has no
	 * checking if nodes exist but expects this to already have been done.
	 * 
	 * @param nodeRef
	 * @return
	 */
	private String incrementDocumentNumber(NodeRef nodeRef) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("incrementDocumentNumber");
		}

		String result;

		NodeService nodeService = serviceRegistry.getNodeService();

		Map<QName, Serializable> properties = nodeService.getProperties(cachedFileRef);

		SimpleDateFormat df = new SimpleDateFormat((String) properties.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_DATE_PATTERN));
		String currentDate = df.format(new Date());
		Integer counter;
		// TODO investigate how this works in a clustered environment.
		if (!currentDate.equals(properties.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_1))) {
			counter = (Integer) properties.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_2) + 1;
			if (counter >= 999000) {
				// If series run out of space, then reset the numbering. Since a
				// new date is used anyway, the series will not collide.
				counter = 1;
			}
		} else {
			counter = (Integer) properties.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_2) + 1;
			if (LOG.isDebugEnabled())
				LOG.debug("Current date matches stored date, increasing counter to  " + counter);

		}
		properties.put(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_1, currentDate);
		properties.put(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_2, counter);
		nodeService.setProperties(cachedFileRef, properties);

		String leftPad = org.apache.commons.lang.StringUtils.leftPad(counter.toString(), 6, '0');
		result = currentDate + "-" + leftPad.substring(0, 3) + "-" + leftPad.substring(3);
		if (LOG.isDebugEnabled())
			LOG.debug("Generated document number: " + result);

		return result;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}

}
