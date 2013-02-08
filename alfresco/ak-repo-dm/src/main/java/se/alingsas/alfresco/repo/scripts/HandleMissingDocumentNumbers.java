package se.alingsas.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.alingsas.alfresco.repo.model.AkDmModel;
import se.alingsas.alfresco.repo.utils.documentnumber.DocumentNumberUtil;

public class HandleMissingDocumentNumbers extends DeclarativeWebScript
		implements InitializingBean {

	private static final Logger LOG = Logger
			.getLogger(HandleMissingDocumentNumbers.class);
	private ServiceRegistry serviceRegistry;
	private BehaviourFilter behaviourFilter;
	private DocumentNumberUtil documentNumberUtil;
	private Repository repositoryHelper;
	
	
	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		final Map<String, Object> model = new HashMap<String, Object>();

		SearchService searchService = serviceRegistry.getSearchService();
		behaviourFilter.disableBehaviour();
		
		
		SearchParameters sp = new SearchParameters();
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.addStore(repositoryHelper.getCompanyHome().getStoreRef());
		sp.setQuery("+TYPE:\""+AkDmModel.TYPE_AKDM_DOCUMENT.toString()+"\" +ISNULL:\"akdm:documentNumber\"");
		sp.setLimit(5000);
		LOG.debug("Query: "+sp.getQuery());
		
		ResultSet results = null;
		int result = 0;
        try
        {
        	 results = searchService.query(sp);
        	 
        	 LOG.debug("Query returned "+results.length()+" results.");
        	 result = results.length();
             for(ResultSetRow row : results)
             {
                 NodeRef currentNodeRef = row.getNodeRef();
                 try {
					documentNumberUtil.setDocumentNumber(currentNodeRef, false);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
             }
         }
         finally
         {
             if(results != null)
             {
                 results.close();
             }
         } 

		model.put("documents", result);
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setDocumentNumberUtil(DocumentNumberUtil documentNumberUtil) {
		this.documentNumberUtil = documentNumberUtil;
	}
	
	public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
		this.behaviourFilter = behaviourFilter;
	}

	public Repository getRepositoryHelper() {
		return repositoryHelper;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

}
