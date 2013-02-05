/**
 * 
 */
package se.alingsas.alfresco.repo.utils.documentnumber;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import se.alingsas.alfresco.repo.model.AkDmModel;

/**
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class DocumentNumberUtil {

	private static final Logger LOG = Logger
			.getLogger(DocumentNumberUtil.class);

	private static ServiceRegistry serviceRegistry;
	private static Repository repositoryHelper;

	private static NodeRef cachedFileRef;

	private static final String SETTINGS = "Settings";
	private static final String DOCUMENT_NUMBER_SETTINGS = "DocumentNumber.settings";

	/**
	 * Sets the document number on a node if it doesn't already exist
	 * 
	 * Potential exceptions should be caught to make sure that files can always
	 * be created in the system even if generating a document number fails.
	 * 
	 * @param nodeRef
	 */
	public void setDocumentNumber(NodeRef nodeRef, boolean replace)
			throws Exception {
		NodeService nodeService = serviceRegistry.getNodeService();

		if (nodeService.exists(nodeRef)
				&& nodeService.hasAspect(nodeRef, AkDmModel.ASPECT_AKDM_COMMON)) {
			String docNumber = (String) nodeService.getProperty(nodeRef,
					AkDmModel.PROP_AKDM_DOC_NUMBER);
			if (StringUtils.isEmpty(docNumber) || replace == true) {
				String documentNumber = AuthenticationUtil.runAs(
						new AuthenticationUtil.RunAsWork<String>() {
							public String doWork() throws Exception {
								return getNextDocumentNumber();
							}
						}, AuthenticationUtil.getSystemUserName());
				nodeService.setProperty(nodeRef,
						AkDmModel.PROP_AKDM_DOC_NUMBER, documentNumber);

				if (LOG.isDebugEnabled())
					LOG.debug("Setting document number for " + documentNumber
							+ " for node " + nodeRef.toString());
			}
		}
	}

	/**
	 * Generates the next document number. Calling this method will increase the
	 * global counter for document numbers.
	 * 
	 * Potential exceptions should be caught to make sure that files can always
	 * be created in the system even if generating a document number fails.
	 * 
	 * @return Generated document number
	 */
	synchronized public String getNextDocumentNumber() throws Exception {
		// Number format
		// YYYY-MM-DD-XXX-XXX

		NodeService nodeService = serviceRegistry.getNodeService();

		if (cachedFileRef == null) {
			FileFolderService fileFolderService = serviceRegistry
					.getFileFolderService();
			NodeRef docNumSettingsRef = null;
			NodeRef settingsNodeRef = fileFolderService.searchSimple(
					repositoryHelper.getCompanyHome(), SETTINGS);
			if (settingsNodeRef == null) {
				// Settings folder does not exist, create it
				settingsNodeRef = fileFolderService.create(
						repositoryHelper.getCompanyHome(), SETTINGS,
						ContentModel.TYPE_FOLDER).getNodeRef();
			}
			if (nodeService.exists(settingsNodeRef)) {
				docNumSettingsRef = fileFolderService.searchSimple(
						settingsNodeRef, DOCUMENT_NUMBER_SETTINGS);
				if (docNumSettingsRef == null) {
					docNumSettingsRef = fileFolderService.create(
							settingsNodeRef, DOCUMENT_NUMBER_SETTINGS,
							ContentModel.TYPE_CONTENT).getNodeRef();
				}
				cachedFileRef = docNumSettingsRef;

			} else {
				throw new Exception(
						"Settings folder does not exist and could not be created");
			}
		}

		if (nodeService.exists(cachedFileRef)) {
			if (!nodeService.hasAspect(cachedFileRef,
					AkDmModel.ASPECT_AKDM_DOCUMENT_NUMBER_SETTINGS)) {
				nodeService.addAspect(cachedFileRef,
						AkDmModel.ASPECT_AKDM_DOCUMENT_NUMBER_SETTINGS, null);
			}

			String result = "";

			// Now we should have a lock

			Map<QName, Serializable> properties = nodeService
					.getProperties(cachedFileRef);

			SimpleDateFormat df = new SimpleDateFormat(
					(String) properties
							.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_DATE_PATTERN));
			String currentDate = df.format(new Date());
			Integer counter;
			// TODO add handling for a clustered setup to handle synchronization
			// between servers.
			if (!currentDate.equals(properties
					.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_1))) {
				/*
				 * Do not reset counter if (LOG.isDebugEnabled()) LOG.debug(
				 * "Current date does not match stored date, resetting counter"
				 * ); counter = 1;
				 */
				counter = (Integer) properties
						.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_2) + 1;
			} else {
				counter = (Integer) properties
						.get(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_2) + 1;
				if (LOG.isDebugEnabled())
					LOG.debug("Current date matches stored date, increasing counter to  "
							+ counter);

			}
			properties.put(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_1,
					currentDate);
			properties.put(AkDmModel.PROP_AKDM_DOCUMENT_NUMBER_SETTINGS_2,
					counter);
			nodeService.setProperties(cachedFileRef, properties);

			String leftPad = StringUtils.leftPad(counter.toString(), 6, '0');
			result = currentDate + "-" + leftPad.substring(0, 3) + "-"
					+ leftPad.substring(3);
			if (LOG.isDebugEnabled())
				LOG.debug("Generated document number: " + result);

			return result;
		} else {
			throw new Exception(
					"Document number settings does not exist and could not be created");
		}
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DocumentNumberUtil.serviceRegistry = serviceRegistry;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		DocumentNumberUtil.repositoryHelper = repositoryHelper;
	}
}
