package se.alingsas.alfresco.repo.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.GUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Used as bridge between Javascript and Java.
 * Can be called in JS by: archiveService.METHOD
 *
 * @author Anton Häägg - Redpill Linpro AB
 */
public class ScriptEmailService extends BaseProcessorExtension implements InitializingBean {

  protected NodeService nodeService;
  protected SearchService searchService;

  public boolean validateEmailExistance(String firstName, String lastName, String userName) {

    List<NodeRef> users = getUser(firstName, lastName, userName);

    if(users.size() != 1){
      return false;
    }

    NodeRef requestedUser = users.get(0);
    if(!nodeService.exists(requestedUser)){
      return false;
    }
    String email = (String) nodeService.getProperty(requestedUser, ContentModel.PROP_EMAIL);

    if(StringUtils.isEmpty(email)){
      String newEmail = "default" + "@" + GUID.generate()+ ".com";
      nodeService.setProperty(requestedUser, ContentModel.PROP_EMAIL, newEmail);
    }

    return true;
  }

  /**
   * Finds and returns the specified user
   *
   * @param firstName - of user
   * @param lastName - of user
   * @param userName - of user
   * @return list of users
   */
  protected List<NodeRef> getUser(String firstName, String lastName, String userName ) {
    SearchParameters sp = new SearchParameters();
    StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    sp.addStore(storeRef);
    sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    String archiveSearchQuery = "=cm:firstName:" + firstName + " AND =cm:lastName:" + lastName + " AND =cm:userName:" + userName;
    sp.setQuery(archiveSearchQuery);
    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodes = new ArrayList<>();
    try {
      if (rs.length() == 0) {
        return nodes;
      }
      nodes = rs.getNodeRefs();
    } finally {
      rs.close();
    }
    return nodes;
  }


  @Override
  public void afterPropertiesSet() {
    Assert.notNull(nodeService, "nodeService is null");
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }
}
