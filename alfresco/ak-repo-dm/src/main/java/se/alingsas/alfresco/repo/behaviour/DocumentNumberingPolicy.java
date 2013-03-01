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

package se.alingsas.alfresco.repo.behaviour;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.alingsas.alfresco.repo.model.AkDmModel;
import se.alingsas.alfresco.repo.utils.documentnumber.DocumentNumberUtil;

/**
 * Policy which generates document numbers for new documents
 * 
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class DocumentNumberingPolicy implements
		NodeServicePolicies.OnUpdateNodePolicy,
		CopyServicePolicies.OnCopyCompletePolicy {

	private static final Logger LOG = Logger
			.getLogger(DocumentNumberingPolicy.class);
	private Behaviour onUpdateNode;
	private Behaviour onCopyComplete;
	private BehaviourFilter behaviourFilter;
	private PolicyComponent policyComponent;
	private DocumentNumberUtil documentNumberUtil;
	private NodeService nodeService;

	public void init() {

		this.onUpdateNode = new JavaBehaviour(this, "onUpdateNode",
				NotificationFrequency.TRANSACTION_COMMIT);
		this.onCopyComplete = new JavaBehaviour(this, "onCopyComplete",
				NotificationFrequency.TRANSACTION_COMMIT);

		this.policyComponent.bindClassBehaviour(QName.createQName(
				NamespaceService.ALFRESCO_URI, "onUpdateNode"),
				AkDmModel.TYPE_AKDM_DOCUMENT, this.onUpdateNode);
		this.policyComponent.bindClassBehaviour(QName.createQName(
				NamespaceService.ALFRESCO_URI, "onCopyComplete"),
				AkDmModel.TYPE_AKDM_DOCUMENT, this.onCopyComplete);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy#onUpdateNode
	 * (org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public void onUpdateNode(NodeRef nodeRef) {
		try {
			if (nodeService.exists(nodeRef)) {
				if (!nodeService.hasAspect(nodeRef,
						ContentModel.ASPECT_WORKING_COPY)) {
					behaviourFilter.disableBehaviour(nodeRef);
					documentNumberUtil.setDocumentNumber(nodeRef, false);
					behaviourFilter.enableBehaviour(nodeRef);
				} else {
					if (LOG.isTraceEnabled()) {
						LOG.trace("Node is working copy, will not generate a new document number");
					}
				}
			} else {
				LOG.debug("Node does not exist " + nodeRef.toString());
			}

		} catch (Exception e) {
			LOG.error("Failed to generate document number for node", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy#
	 * onCopyComplete(org.alfresco.service.namespace.QName,
	 * org.alfresco.service.cmr.repository.NodeRef,
	 * org.alfresco.service.cmr.repository.NodeRef, boolean, java.util.Map)
	 */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef,
			NodeRef targetNodeRef, boolean copyToNewNode,
			Map<NodeRef, NodeRef> copyMap) {
		try {
			if (nodeService.exists(targetNodeRef)) {
				if (!nodeService.hasAspect(targetNodeRef,
						ContentModel.ASPECT_WORKING_COPY)) {
					behaviourFilter.disableBehaviour(targetNodeRef);
					documentNumberUtil.setDocumentNumber(targetNodeRef, false);
					behaviourFilter.enableBehaviour(targetNodeRef);
				} else {
					if (LOG.isTraceEnabled()) {
						LOG.trace("Node is working copy, will not generate a new document number");
					}
				}
			} else {
				LOG.debug("Node does not exist " + targetNodeRef.toString());
			}
		} catch (Exception e) {
			LOG.error("Failed to generate document number for node", e);
		}
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setDocumentNumberUtil(DocumentNumberUtil documentNumberUtil) {
		this.documentNumberUtil = documentNumberUtil;
	}

	public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
		this.behaviourFilter = behaviourFilter;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

}
