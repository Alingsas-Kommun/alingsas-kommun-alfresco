package se.alingsas.alfresco.repo.workflow;

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.log4j.Logger;

import se.alingsas.alfresco.repo.workflow.model.CommonWorkflowModel;

public class UnlockFilesTaskListener implements TaskListener {
	private static final Logger LOG = Logger
			.getLogger(UnlockFilesTaskListener.class);

	@Override
	public void notify(final DelegateTask task) {
		final String akwfInitiator = AuthenticationUtil
				.getFullyAuthenticatedUser();
		LOG.debug("Entering UnlockFilesTaskListener");

		LOG.debug("Authenticated user is " + akwfInitiator);

		final ServiceRegistry serviceRegistry = WorkflowUtil
				.getServiceRegistry();
		final LockService lockService = serviceRegistry.getLockService();
		final NodeService nodeService = serviceRegistry.getNodeService();
		final PermissionService permissionService = serviceRegistry.getPermissionService();
		final List<ChildAssociationRef> childAssocs = WorkflowUtil
				.getBpmPackageFiles(task.getExecution(), nodeService);
		LOG.debug("Unlocking files");
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
			public Object doWork() throws Exception {
				if (childAssocs != null && childAssocs.size() > 0) {
					for (ChildAssociationRef childAssoc : childAssocs) {
						final NodeRef fileNodeRef = childAssoc.getChildRef();
						lockService.unlock(fileNodeRef);
						String readPermSiteRoleGroup = (String) task.getExecution().getVariable(CommonWorkflowModel.SITE_GROUP);
						if (readPermSiteRoleGroup!=null) {
							String removePermissions = (String) task.getExecution().getVariable(CommonWorkflowModel.REMOVE_PERMISSIONS_DONE);
							if (removePermissions==null || !"true".equals(removePermissions)) {
								//If temporary permissions are not already cleared, clear them now
								
								LOG.debug("Removing temporary read permission on file for "+readPermSiteRoleGroup);
								permissionService.deletePermission(fileNodeRef, readPermSiteRoleGroup, PermissionService.READ);
							}
						}
					}
				}
				return "";
			}
		}, AuthenticationUtil.getSystemUserName());
	}

}
