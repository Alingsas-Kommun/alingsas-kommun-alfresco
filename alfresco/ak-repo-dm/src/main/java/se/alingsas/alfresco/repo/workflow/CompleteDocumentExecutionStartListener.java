package se.alingsas.alfresco.repo.workflow;

import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

import se.alingsas.alfresco.repo.model.AkDmModel;
import se.alingsas.alfresco.repo.workflow.model.CommonWorkflowModel;
import se.alingsas.alfresco.repo.workflow.model.CompleteDocumentWorkflowModel;

public class CompleteDocumentExecutionStartListener implements
		ExecutionListener {
	private static final Logger LOG = Logger
			.getLogger(CompleteDocumentExecutionStartListener.class);

	

	/**
	 * CompleteDocumentStartTaskListener which will prepare the workflow with data
	 */
	@Override
	public void notify(DelegateExecution execution) {
		final String akwfInitiator = AuthenticationUtil
				.getFullyAuthenticatedUser();
		LOG.debug("Entering CompleteDocumentStartTaskListener");

		LOG.debug("Authenticated user is " + akwfInitiator);

		final ServiceRegistry serviceRegistry = WorkflowUtil.getServiceRegistry();
		final LockService lockService = serviceRegistry.getLockService();
		final PermissionService permissionService = serviceRegistry
				.getPermissionService();
		final NodeService nodeService = serviceRegistry.getNodeService();
		final SiteService siteService = serviceRegistry.getSiteService();
		final AuthorityService authorityService = serviceRegistry
				.getAuthorityService();
		execution.setVariable(CommonWorkflowModel.INITIATOR, akwfInitiator);
		LOG.info("Starting complete document workflow for user "
				+ AuthenticationUtil.getFullyAuthenticatedUser());
		ActivitiScriptNode akwfTargetFolder = (ActivitiScriptNode) execution
				.getVariable(CommonWorkflowModel.TARGET_FOLDER);
		ActivitiScriptNode akwfTargetSite = (ActivitiScriptNode) execution
				.getVariable(CommonWorkflowModel.TARGET_SITE);
		NodeRef targetFolderNodeRef = akwfTargetFolder.getNodeRef();
		NodeRef akwfTargetSiteNodeRef = akwfTargetSite.getNodeRef();
		if (AccessStatus.ALLOWED.equals(permissionService.hasPermission(
				akwfTargetFolder.getNodeRef(), PermissionService.WRITE))) {
			execution.setVariable(CommonWorkflowModel.HANDLING, CompleteDocumentWorkflowModel.AUTOMATIC_HANDLING);
			LOG.debug("User "
					+ akwfInitiator
					+ " have write permission on target folder, setting WF handling to Automatic");
			execution.setVariable(CommonWorkflowModel.APPROVER, akwfInitiator);
		} else {
			execution.setVariable(CommonWorkflowModel.HANDLING, CompleteDocumentWorkflowModel.MANUAL_HANDLING);
			LOG.debug("User "
					+ akwfInitiator
					+ " have read permission on target folder, setting WF handling to Manual");
		}
		
		/*
		 * Verify package contents
		 */
		List<ChildAssociationRef> childAssocs = WorkflowUtil.getBpmPackageFiles(execution,
				nodeService);
		if (childAssocs != null && childAssocs.size() > 0) {
			for (ChildAssociationRef childAssoc : childAssocs) {
				NodeRef fileNodeRef = childAssoc.getChildRef();
				if (AccessStatus.ALLOWED.equals(permissionService
						.hasPermission(fileNodeRef, PermissionService.WRITE))) {
					LOG.debug("User " + akwfInitiator
							+ " has permission to complete file ");
				} else {
					throw new RuntimeException(
							"Fel: Du har inte skrivrättigheter på filen som du försöker färdigställa.");
				}
				
				if (!CommonWorkflowModel.DOC_STATUS_DONE.equals(nodeService.getProperty(fileNodeRef, AkDmModel.PROP_AKDM_DOC_STATUS))) {
					LOG.debug("Document is in status "+CommonWorkflowModel.DOC_STATUS_WORKING+".");
				} else {
					throw new RuntimeException(
							"Fel: Dokumentet har redan status "+CommonWorkflowModel.DOC_STATUS_DONE);
				}
			}
		} else {
			throw new RuntimeException(
					"Fel: Inga filer inkluderades i arbetsflödet");
		}

		/*
		 * Validate sites assignment
		 */
		SiteInfo folderSite = siteService.getSite(targetFolderNodeRef);

		SiteInfo site = siteService.getSite(akwfTargetSiteNodeRef);
		if (!site.equals(folderSite)) {
			throw new RuntimeException(
					"Fel: Vald mapp ligger inte under den valda siten.");
		}

		String siteRoleGroup = siteService.getSiteRoleGroup(
				site.getShortName(), CommonWorkflowModel.SITE_COLLABORATOR);
		Set<String> containedAuthorities = authorityService
				.getContainedAuthorities(AuthorityType.USER, siteRoleGroup,
						false);
		if (containedAuthorities.size() == 0) {
			siteRoleGroup = siteService.getSiteRoleGroup(site.getShortName(),
					CommonWorkflowModel.SITE_MANAGER);
			containedAuthorities = authorityService.getContainedAuthorities(
					AuthorityType.USER, siteRoleGroup, false);
			if (containedAuthorities.size() == 0) {
				throw new RuntimeException(
						"Fel: Det finns ingen som kan godkänna på denna sammarbetsyta.");
			}
		}
		LOG.info(siteRoleGroup + " assigned for review task.");
		execution.setVariable(CommonWorkflowModel.SITE, site.getShortName());
		execution.setVariable(CommonWorkflowModel.SITE_GROUP, siteRoleGroup);

		/*
		 * Lock files
		 */
		String akwfHandling = (String) execution.getVariable(CommonWorkflowModel.HANDLING);
		if (akwfHandling != "Error") {
			if (childAssocs != null && childAssocs.size() > 0) {
				for (ChildAssociationRef childAssoc : childAssocs) {
					final NodeRef fileNodeRef = childAssoc.getChildRef();
					LOG.debug("Locked file " + fileNodeRef.toString());
					AuthenticationUtil.runAs(
							new AuthenticationUtil.RunAsWork<Object>() {
								public Object doWork() throws Exception {
									AuthenticationUtil
											.setFullyAuthenticatedUser(AuthenticationUtil
													.getSystemUserName());
									lockService.lock(fileNodeRef,
											LockType.READ_ONLY_LOCK);
									AuthenticationUtil
											.setFullyAuthenticatedUser(akwfInitiator);
									return "";
								}
							}, AuthenticationUtil.getSystemUserName());
				}
			}
		}
	}

	

	

}
