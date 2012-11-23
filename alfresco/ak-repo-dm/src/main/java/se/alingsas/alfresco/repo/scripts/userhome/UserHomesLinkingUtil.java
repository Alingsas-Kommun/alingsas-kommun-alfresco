package se.alingsas.alfresco.repo.scripts.userhome;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;

public class UserHomesLinkingUtil {
	
	private static ServiceRegistry serviceRegistry;
	
	public boolean linkUserHomesWithSite(String siteId) {
		SiteInfo site = serviceRegistry.getSiteService().getSite(siteId);
		if (site==null) {
			return false;
		}
		NodeRef userHomes = null;
		SearchParameters sp = new SearchParameters();
        sp.addStore(Repository.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"/app:company_home/app:user_homes\"");
        ResultSet results = null;
        try
        {
            results = serviceRegistry.getSearchService().query(sp);
            userHomes = results.getNodeRef(0);
        }
        finally
        {
            if(results != null)
            {
                results.close();
            }
        } 	
		
		NodeRef documentLibrary = serviceRegistry.getNodeService().getChildByName(site.getNodeRef(), ContentModel.ASSOC_CONTAINS, "documentLibrary");
		if (serviceRegistry.getNodeService().getChildByName(documentLibrary, ContentModel.ASSOC_CONTAINS, "User Homes")!=null) {
			return false;
		} else {
			serviceRegistry.getNodeService().addChild(documentLibrary, userHomes, ContentModel.ASSOC_CONTAINS, QName.createQName("User Homes"));
		}
		return true;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		UserHomesLinkingUtil.serviceRegistry = serviceRegistry;
	}
}
