/*
 * Copyright (C) 2012-2014 Alingsås Kommun
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnSetNodeTypePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.redpill.alfresco.repo.findwise.model.FindwiseIntegrationModel;
import org.springframework.beans.factory.InitializingBean;

import se.alingsas.alfresco.repo.model.AkDmModel;

/**
 * Enabled the findwise indexer for a node by attaching an aspect
 * 
 * @author Marcus Svensson - Redpill Linpro AB
 *
 */
public class EnableFindwiseIndexerPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnSetNodeTypePolicy, InitializingBean {
  private static final Logger LOG = Logger.getLogger(EnableFindwiseIndexerPolicy.class);

  private static boolean isInitialized = false;

  private boolean enabled = false;

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    if (!isInitialized() && enabled) {
      isInitialized = true;
      if (LOG.isTraceEnabled())
        LOG.trace("Initialized " + this.getClass().getName());
      policyComponent.bindClassBehaviour(OnSetNodeTypePolicy.QNAME, AkDmModel.TYPE_AKDM_DOCUMENT, new JavaBehaviour(this, "onSetNodeType", NotificationFrequency.EVERY_EVENT));
      policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, AkDmModel.TYPE_AKDM_DOCUMENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.EVERY_EVENT));

    } else if (!enabled) {
      LOG.info("Findwise indexing is disabled. Change by setting the property findwise.indexing.enabled=true in alfresco-global.properties");
    }
  }

  @Override
  public void onSetNodeType(NodeRef nodeRef, QName oldType, QName newType) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onSetNodeType begin");
    }

    if (allowAddAspect(nodeRef)) {
      if (dictionaryService.isSubClass(newType, AkDmModel.TYPE_AKDM_DOCUMENT))
        addFindwiseIndexableAspect(nodeRef);
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onSetNodeType end");
    }
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCreateNode begin");
    }

    final NodeRef nodeRef = childAssocRef.getChildRef();

    if (allowAddAspect(nodeRef)) {
      addFindwiseIndexableAspect(nodeRef);
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCreateNode end");
    }
  }

  private boolean allowAddAspect(final NodeRef nodeRef) {
    if (!nodeService.exists(nodeRef)) {
      return false;
    }

    // Do not update nodes outside the workspace spacesstore
    if (!nodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Tried to set document metadata on node (" + nodeRef + ") in store " + nodeRef.getStoreRef() + " which is ignored.");
      }
      return false;
    }

    if (nodeService.hasAspect(nodeRef, FindwiseIntegrationModel.ASPECT_FINDWISE_INDEXABLE)) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Node already have findwise aspect. Skipping");
      }
      return false;
    }

    return true;
  }

  private Boolean isInitialized() {
    if (isInitialized == false) {
      isInitialized = true;
    } else {
      return true;
    }
    return false;
  }

  private final void addFindwiseIndexableAspect(final NodeRef nodeRef) {
    final Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(1);

    nodeService.addAspect(nodeRef, FindwiseIntegrationModel.ASPECT_FINDWISE_INDEXABLE, aspectProperties);
  }
 
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
