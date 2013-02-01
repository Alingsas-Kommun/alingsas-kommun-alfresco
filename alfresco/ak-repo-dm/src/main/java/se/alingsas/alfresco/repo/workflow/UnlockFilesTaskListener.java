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
import org.apache.log4j.Logger;

public class UnlockFilesTaskListener implements TaskListener {
	private static final Logger LOG = Logger
			.getLogger(UnlockFilesTaskListener.class);

	@Override
	public void notify(DelegateTask task) {
		final String akwfInitiator = AuthenticationUtil
				.getFullyAuthenticatedUser();
		LOG.debug("Entering UnlockFilesTaskListener");

		LOG.debug("Authenticated user is " + akwfInitiator);

		final ServiceRegistry serviceRegistry = WorkflowUtil
				.getServiceRegistry();
		final LockService lockService = serviceRegistry.getLockService();
		final NodeService nodeService = serviceRegistry.getNodeService();

		final List<ChildAssociationRef> childAssocs = WorkflowUtil
				.getBpmPackageFiles(task.getExecution(), nodeService);
		LOG.debug("Unlocking files");
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
			public Object doWork() throws Exception {
				if (childAssocs != null && childAssocs.size() > 0) {
					for (ChildAssociationRef childAssoc : childAssocs) {
						final NodeRef fileNodeRef = childAssoc.getChildRef();
						lockService.unlock(fileNodeRef);
					}
				}
				return "";
			}
		}, AuthenticationUtil.getSystemUserName());
	}

}
