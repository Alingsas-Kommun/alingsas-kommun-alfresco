package se.alingsas.alfresco.repo.behaviour;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.redpill.alfresco.numbering.component.NumberingComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.model.AkDmModel;

/**
 * Sets new content inside document library to akdm:document
 *
 * @author Marcus Svensson - Redpill Linpro AB
 *
 */
public class DocumentPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnMoveNodePolicy, OnUpdateNodePolicy, OnCopyCompletePolicy, InitializingBean {

  private static final Logger LOG = Logger.getLogger(DocumentPolicy.class);
  private NumberingComponent numberingComponent;
  private static boolean isInitialized = false;

  protected void setType(NodeRef nodeRef) {
    QName nodeType = nodeService.getType(nodeRef);
    if (nodeType.equals(ContentModel.PROP_CONTENT)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Changing type of content to " + AkDmModel.TYPE_AKDM_DOCUMENT + ": " + nodeRef);
      }
      boolean enabled = behaviourFilter.isEnabled(nodeRef);
      if (enabled) {
        behaviourFilter.disableBehaviour(nodeRef);
      }
      nodeService.setType(nodeRef, AkDmModel.TYPE_AKDM_DOCUMENT);
      if (enabled) {
        behaviourFilter.enableBehaviour(nodeRef);
      }
    }
  }

  protected void setAutoVersionProps(NodeRef nodeRef) {
    QName nodeType = nodeService.getType(nodeRef);
    if (nodeType.equals(ContentModel.PROP_CONTENT)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting auto version properties to true for node: " + nodeRef);
      }
      boolean enabled = behaviourFilter.isEnabled(nodeRef);
      if (enabled) {
        behaviourFilter.disableBehaviour(nodeRef);
      }
      nodeService.setProperty(nodeRef, ContentModel.PROP_AUTO_VERSION_PROPS, true);
      if (enabled) {
        behaviourFilter.enableBehaviour(nodeRef);
      }
    }
  }

  protected void setDocumentNumber(NodeRef nodeRef) {
    setDocumentNumber(nodeRef, false);
  }

  protected void setDocumentNumber(NodeRef nodeRef, boolean forceNewNumber) {
    QName nodeType = nodeService.getType(nodeRef);
    if (dictionaryService.isSubClass(nodeType, AkDmModel.TYPE_AKDM_DOCUMENT)) {
      boolean enabled = behaviourFilter.isEnabled(nodeRef);
      if (enabled) {
        behaviourFilter.disableBehaviour(nodeRef);
      }
      String docNumber = (String) nodeService.getProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER);
      if (docNumber == null || !StringUtils.hasText(docNumber) || forceNewNumber) {
        String decoratedNextNumber = numberingComponent.getDecoratedNextNumber(nodeRef);
        nodeService.setProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER, decoratedNextNumber);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Setting document number for " + decoratedNextNumber + " for node " + nodeRef.toString());
        }
      }
      if (enabled) {
        behaviourFilter.enableBehaviour(nodeRef);
      }
    }
  }

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCreateNode begin - " + childAssocRef.getParentRef() + "->" + childAssocRef.getChildRef());
    }

    final NodeRef nodeRef = childAssocRef.getChildRef();

    if (allowUpdate(nodeRef)) {
      setType(nodeRef);
      setDocumentNumber(nodeRef);
      setAutoVersionProps(nodeRef);
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onCreateNode end");
    }

  }

  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onMoveNode begin");
    }

    final NodeRef newNodeRef = newChildAssocRef.getChildRef();

    // Check lock and store for new node
    if (allowUpdate(newNodeRef)) {
      setType(newNodeRef);
      setDocumentNumber(newNodeRef);
      setAutoVersionProps(newNodeRef);
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " onMoveNode end");
    }
  }

  @Override
  public void onUpdateNode(NodeRef nodeRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("onUpdateNode");
    }
    // Check lock and store for new node
    if (allowUpdate(nodeRef)) {
      setDocumentNumber(nodeRef);
    }
  }

  @Override
  public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("onCopyComplete");
    }

    // Check lock and store for new node
    if (allowUpdate(targetNodeRef)) {
      setDocumentNumber(targetNodeRef, copyToNewNode);
    }

  }

  private boolean allowUpdate(final NodeRef nodeRef) {
    if (!nodeService.exists(nodeRef)) {
      LOG.debug("Node does not exist. Skipping...");
      return false;
    }

    // Do not update nodes outside the workspace spacesstore
    if (!nodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Tried to set document metadata on node (" + nodeRef + ") in store " + nodeRef.getStoreRef() + " which is ignored.");
      }
      return false;
    }

    QName nodeType = nodeService.getType(nodeRef);

    if (ContentModel.TYPE_THUMBNAIL.equals(nodeType)) {
      LOG.debug("Node type is cm:thumbnail. Skipping...");
      return false;
    }
    try {
      // Check that the node is not locked by another user
      lockService.checkForLock(nodeRef);
    } catch (final NodeLockedException nle) {
      LOG.debug("Tried to set document metadata on locked node: " + nodeRef + " which is ignored.");
      return false;
    }

    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      LOG.debug("Node is working copy, skipping: " + nodeRef);
      return false;
    }

    // Check if file is within a sitedoclib , user home or shared files
    boolean result = true;
    try {
      // Find out if a node is within the document library. An exception will be
      // thrown if it is not within the document library.
      SiteInfo site = siteService.getSite(nodeRef);

      if (site == null) {
        LOG.debug("Not within a site. Skipping...");
        result = false;
      } else {
        NodeRef container = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
        if (container == null) {
          LOG.trace("Document library does not exist. Denying.");
          return false;
        }
        fileFolderService.getNameOnlyPath(container, nodeRef);
        LOG.trace("Document is within a site. Allowing.");
        return true;
      }

    } catch (FileNotFoundException e) {
      LOG.debug("Tried to set document metadata on node outside of document library: " + nodeRef);
      result = false;
    }

    NodeRef fullyAuthenticatedPersonNodeRef = repository.getFullyAuthenticatedPerson();
    if (fullyAuthenticatedPersonNodeRef == null) {
      LOG.debug("Not a valid authenticated user: " + nodeRef);
      return false;
    }
    NodeRef userHomeNodeRef = repository.getUserHome(fullyAuthenticatedPersonNodeRef);
    NodeRef sharedHomeNodeRef = repository.getSharedHome();
    NodeRef companyHomeNodeRef = repository.getCompanyHome();
    try {
      // Test if node is within user home
      if (userHomeNodeRef == null || companyHomeNodeRef.equals(userHomeNodeRef)) {
        LOG.trace("User home is company home. Not allowing.");
        result = false;
      } else {
        fileFolderService.getNameOnlyPath(userHomeNodeRef, nodeRef);
        result = true;
        LOG.trace("Document is within user home. Allowing.");
        return true;
      }
    } catch (FileNotFoundException e) {
      LOG.debug("Tried to set document metadata on node outside of user home: " + nodeRef);
      result = false;
    }

    try {
      // Test if node is within shared home
      if (sharedHomeNodeRef == null) {
        result = false;
      } else {
        fileFolderService.getNameOnlyPath(sharedHomeNodeRef, nodeRef);
        result = true;
        LOG.trace("Document is within shared home. Allowing.");
        return true;
      }
    } catch (FileNotFoundException e) {
      LOG.debug("Tried to set document metadata on node outside of shared home: " + nodeRef);
      result = false;
    }

    return result;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    Assert.notNull(numberingComponent);
    if (!isInitialized()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Initialized " + this.getClass().getName());
      }

      // policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME,
      // ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onUpdateNode",
      // NotificationFrequency.EVERY_EVENT));
      policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCopyComplete", NotificationFrequency.TRANSACTION_COMMIT));

      policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.EVERY_EVENT));
      policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onMoveNode", NotificationFrequency.EVERY_EVENT));
    }
  }

  public void setNumberingComponent(NumberingComponent numberingComponent) {
    this.numberingComponent = numberingComponent;
  }

  private Boolean isInitialized() {
    if (isInitialized == false) {
      isInitialized = true;
    } else {
      return true;
    }
    return false;
  }

}
