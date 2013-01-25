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

public class WorkflowUtil {
	public static final String BPM_PACKAGE = "bpm_package";
	public static final String BPM_COMMENT = "bpm_comment";
	public static final String AUTOMATIC_HANDLING = "Automatic";
	public static final String MANUAL_HANDLING = "Manual";
	public static final String HANDLING = "akwf_handling";
	public static final String INITIATOR = "akwf_initiator";
	public static final String APPROVER = "akwf_approver";
	public static final String TARGET_SITE = "akwf_targetSite";
	public static final String TARGET_FOLDER = "akwf_targetFolder";
	
	
	public static final String SITE_GROUP = "akwf_siteGroup";
	public static final String SITE = "akwf_site";
	public static final String SITE_MANAGER = "SiteManager";
	public static final String SITE_COLLABORATOR = "SiteCollaborator";
	
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
	
	public static List<ChildAssociationRef> getBpmPackageFiles(
			DelegateExecution execution, NodeService nodeService) {
		ActivitiScriptNode bpm_package = (ActivitiScriptNode) execution
				.getVariable(BPM_PACKAGE);
		if (bpm_package == null) {
			return null;
		} else {
			NodeRef nodeRef = bpm_package.getNodeRef();
			return nodeService.getChildAssocs(nodeRef);
		}
	}
}
