package se.alingsas.alfresco.repo.workflow;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import se.alingsas.alfresco.repo.workflow.model.CommonWorkflowModel;

public class WorkflowUtil {

	/**
	 * Get the service registry bean from the activiti context
	 * @return
	 */
	public static ServiceRegistry getServiceRegistry() {
		ProcessEngineConfigurationImpl config = Context
				.getProcessEngineConfiguration();
		if (config != null) {
			// Fetch the registry that is injected in the activiti
			// spring-configuration
			ServiceRegistry registry = (ServiceRegistry) config.getBeans().get(
					ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
			if (registry == null) {
				throw new RuntimeException(
						"Service-registry not present in ProcessEngineConfiguration beans, expected ServiceRegistry with key"
								+ ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
			}
			return registry;
		}
		throw new IllegalStateException(
				"No ProcessEngineCOnfiguration found in active context");
	}

	/**
	 * Get the workflow files from the bpm:package model variable as a list
	 * @param execution
	 * @param nodeService
	 * @return
	 */
	public static List<ChildAssociationRef> getBpmPackageFiles(
			DelegateExecution execution, NodeService nodeService) {
		ActivitiScriptNode bpm_package = (ActivitiScriptNode) execution
				.getVariable(CommonWorkflowModel.BPM_PACKAGE);
		if (bpm_package == null) {
			return null;
		} else {
			NodeRef nodeRef = bpm_package.getNodeRef();
			return nodeService.getChildAssocs(nodeRef);
		}
	}
}
