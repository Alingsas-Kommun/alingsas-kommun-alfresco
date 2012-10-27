package com.redpill_linpro.alfresco.sales_demo.webscripts;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.redpill_linpro.alfresco.sales_demo.model.RpsdModel;

public class DirectReportGetWebScript extends AbstractWebScript {
	private static final Logger logger = Logger
			.getLogger(DirectReportGetWebScript.class);
	private DictionaryService dictionaryService;
	private NodeService nodeService;
	private SiteService siteService;
	private NamespaceService namespaceService;
	private ContentService contentService;
	private SearchService searchService;

	private static final String reportDirectoryName = "DirectReports";
	private static final String documentLibraryName = "documentLibrary";

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		String aspectName = "";
		String site = "";
		try {
			aspectName = getAspectName(req);
		} catch(WebScriptException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("No report (aspectName) given");
			}
		}
		try {
			site = getSiteName(req);
		} catch (WebScriptException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("No site given");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Get direct reports on site " + site
					+ " for report type " + aspectName);
		}
		
		ArrayList<Map<String, Serializable>> arr = new ArrayList<Map<String, Serializable>>();
		if (site!="") {
			//If site is specified, just list the filtered contents of the Reports directory
			SiteInfo siteInfo = siteService.getSite(site);
			NodeRef siteNodeRef = siteInfo.getNodeRef();
			NodeRef reportDirectory = getReportDirectory(siteNodeRef);		
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(reportDirectory);
			for (ChildAssociationRef childAssoc : childAssocs) {
				Map<QName, Serializable> properties = nodeService.getProperties(childAssoc.getChildRef());
				Map<String, Serializable> filteredProperties = new HashMap<String, Serializable>();
				Set<Entry<QName, Serializable>> entrySet = properties.entrySet();
				Iterator<Entry<QName, Serializable>> iterator = entrySet.iterator();
				
				while (iterator.hasNext()) {
					Entry<QName, Serializable> next = iterator.next();
					QName prefixedQName = next.getKey().getPrefixedQName(namespaceService);
					prefixedQName.getPrefixString();
					filteredProperties.put(prefixedQName.getPrefixString().replace(":", "_").replace("-", "_"), next.getValue());
				}
				if (!filteredProperties.isEmpty()) {
					arr.add(filteredProperties);
				}
			}			
		} else {
			//TODO
			//Perform a lucene search
		}
		
		
		
		JSONObject obj = new JSONObject();
		try {
			obj.put("data", arr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WebScriptException("Error while creating json object", e);
		}
		// build a JSON string and send it back
		String jsonString = obj.toString();
		res.setContentType("application/json;charset=UTF-8");
		// Fix to force the response to be interpreted as json
		res.getWriter().write(jsonString);
	}

	protected NodeRef getReportDirectory(NodeRef siteNodeRef) {
		NodeRef documentLibraryNodeRef = nodeService.getChildByName(
				siteNodeRef, ContentModel.ASSOC_CONTAINS, documentLibraryName);

		NodeRef reportDirectoryNodeRef = nodeService.getChildByName(
				documentLibraryNodeRef, ContentModel.ASSOC_CONTAINS,
				reportDirectoryName);
		// If no report directory exists create one!
		if (reportDirectoryNodeRef == null) {
			Map<QName, Serializable> contentPropertiesMap = new HashMap<QName, Serializable>();
			contentPropertiesMap.put(ContentModel.PROP_NAME,
					reportDirectoryName);
			reportDirectoryNodeRef = nodeService.createNode(
					documentLibraryNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(reportDirectoryName),
					RpsdModel.TYPE_RPSD_FOLDER, contentPropertiesMap)
					.getChildRef();
		}
		return reportDirectoryNodeRef;
	}

	public String getAspectName(WebScriptRequest req) {
		return getParameterOrArgument(req, "aspectName");
	}

	public String getSiteName(WebScriptRequest req) {
		return getParameterOrArgument(req, "site");
	}

	private String getParameterOrArgument(WebScriptRequest req, String name) {
		String param = req.getParameter(name);
		if (param == null) {
			Map<String, String> templateArgs = req.getServiceMatch()
					.getTemplateVars();
			param = templateArgs.get(name);
		}
		if (param == null) {
			throw new WebScriptException("Parameter " + name + " not found");
		}
		return param;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	public void setNamespaceService(NamespaceService nameSpaceService) {
		this.namespaceService = nameSpaceService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

}
