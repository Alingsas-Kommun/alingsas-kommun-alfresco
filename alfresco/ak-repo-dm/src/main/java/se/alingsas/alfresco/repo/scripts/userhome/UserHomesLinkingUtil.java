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
	
	/**
	 * Add a reference to the user homes folder inside a specific site.
	 * @param siteId
	 * @return
	 */
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
