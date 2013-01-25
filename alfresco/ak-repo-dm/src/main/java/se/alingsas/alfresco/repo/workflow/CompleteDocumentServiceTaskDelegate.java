package se.alingsas.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class CompleteDocumentServiceTaskDelegate implements JavaDelegate {
	private static final Logger LOG = Logger
			.getLogger(CompleteDocumentServiceTaskDelegate.class);
	private static final String DOC_STATUS_DONE = "Färdigt dokument";
	
	/**
	 * CompleteDocumentServiceTaskDelegate will handle completion of document in workflow
	 */
	@Override
	public void execute(final DelegateExecution execution) throws Exception {
		LOG.debug("Entering CompleteDocumentServiceTaskDelegate");
		
		final ServiceRegistry serviceRegistry = WorkflowUtil.getServiceRegistry();
		final LockService lockService = serviceRegistry.getLockService();
		
		final NodeService nodeService = serviceRegistry.getNodeService();
		final FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
		
		final CheckOutCheckInService checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();

		ActivitiScriptNode akwfTargetFolder = (ActivitiScriptNode) execution
				.getVariable(WorkflowUtil.TARGET_FOLDER);
		final NodeRef targetFolderNodeRef = akwfTargetFolder.getNodeRef();
		final OwnableService ownableService = serviceRegistry.getOwnableService();
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
			public Object doWork() throws Exception {
				
				List<ChildAssociationRef> childAssocs = WorkflowUtil.getBpmPackageFiles(execution,
						nodeService);
				if (childAssocs != null && childAssocs.size() > 0) {
					for (ChildAssociationRef childAssoc : childAssocs) {
						NodeRef fileNodeRef = childAssoc.getChildRef();
						String akwfApprover = (String)execution.getVariable("akwf_approver");
						
						LOG.debug("Unlocking document");
						lockService.unlock(fileNodeRef);
						
						LOG.debug("Moving document");
						try {
							fileFolderService.move(fileNodeRef, targetFolderNodeRef, null);
						} catch (FileExistsException e) {
							LOG.warn("File exists, renaming...");
							String name = (String) nodeService.getProperty(fileNodeRef, ContentModel.PROP_NAME);
							String strippedFileName = StringUtils.stripFilenameExtension(name);
							String filenameExtension = StringUtils.getFilenameExtension(name);
							name = strippedFileName + "_" +System.currentTimeMillis() + "." +filenameExtension;
							fileFolderService.move(fileNodeRef, targetFolderNodeRef, name);
						}
						ownableService.setOwner(fileNodeRef, akwfApprover);
						LOG.debug("Checking in document");
						NodeRef workingCopy = checkOutCheckInService.checkout(fileNodeRef);
						nodeService.setProperty(workingCopy, AkDmModel.PROP_AKDM_DOC_STATUS, DOC_STATUS_DONE);
						Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
						versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
						
						versionProperties.put(VersionModel.PROP_DESCRIPTION, "Dokumentet färdigställdes av "+akwfApprover);
						checkOutCheckInService.checkin(workingCopy, versionProperties);
						

					}
				}
				return "";
			}
		}, AuthenticationUtil.getSystemUserName());
	}

}
