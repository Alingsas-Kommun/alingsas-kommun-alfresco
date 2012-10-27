package com.redpill_linpro.alfresco.sales_demo.webscripts;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.redpill_linpro.alfresco.sales_demo.model.RpsdModel;

public class DirectReportPostWebScript extends AbstractWebScript {
	private static final Logger logger = Logger
			.getLogger(DirectReportPostWebScript.class);
	private DictionaryService dictionaryService;
	private NodeService nodeService;
	private SiteService siteService;
	private NamespaceService namespaceService;
	private ContentService contentService;

	private static final String reportDirectoryName = "DirectReports";
	private static final String documentLibraryName = "documentLibrary";

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		String aspectName = getAspectName(req);
		String site = getSiteName(req);
		if (logger.isDebugEnabled()) {
			logger.debug("Create direct report on site " + site
					+ "for report type " + aspectName);
		}
		String[] split = aspectName.split(":");
		// build a json array
		ArrayList<String> arr = new ArrayList<String>();
		if (split != null && split.length == 2) {

			QName aspectQName = QName.createQName(split[0], split[1],
					getNamespaceService());
			Map<QName, PropertyDefinition> aspectProperties = dictionaryService
					.getAspect(aspectQName).getProperties();
			SiteInfo siteInfo = siteService.getSite(site);
			NodeRef siteNodeRef = siteInfo.getNodeRef();
			String directReportContent = "";
			String contentName = "";
			JSONParser a = new JSONParser();
			try {
				// Parse the JSON map and look up actual QNames for the
				// properties
				Map<String, String> parsedJsonObject = (Map<String, String>) a
						.parse(req.getContent().getContent());
				Set<String> keySet = parsedJsonObject.keySet();
				Iterator<String> it = keySet.iterator();
				Map<QName, Serializable> aspectPropertiesMap = new HashMap<QName, Serializable>();
				while (it.hasNext()) {
					String key = it.next();
					String value = parsedJsonObject.get(key);
					try {
						split = key.split(":");
						if (split != null && split.length == 2) {
							// Do a lookup to see if the property actually
							// exists in the data model.
							QName qName = QName.createQName(split[0], split[1],
									getNamespaceService());

							// Special handling of content properties
							if (RpsdModel.PROP_RPSD_CONTENT.compareTo(qName) == 0) {
								directReportContent = value;
								if (logger.isDebugEnabled()) {
									logger.debug("Found content property, mapping to actual content");
								}
							} else if (RpsdModel.PROP_RPSD_NAME
									.compareTo(qName) == 0) {
								contentName = value + "_" + getDateTime();
								if (logger.isDebugEnabled()) {
									logger.debug("Found name property, mapping to actual name");
								}
							} else if (aspectProperties.containsKey(qName)) {
								// Validate property against aspect
								if (logger.isDebugEnabled()) {
									logger.debug("Found "
											+ qName.getLocalName()
											+ " property, setting aspect property value to "
											+ value);
								}
								aspectPropertiesMap.put(qName, value);
							} else {
								logger.warn("Attempt to set property " + key
										+ " which is not part of aspect "
										+ aspectName);
							}
						} else {
							logger.warn("Attempt to set invalid property "
									+ key
									+ " which does not contain any namespace");
						}
					} catch (InvalidQNameException e) {
						logger.warn("Failed to look up property " + key
								+ " in the data model, does it exist?", e);
					}
				}
				NodeRef reportDirectory = getReportDirectory(siteNodeRef);
				Map<QName, Serializable> contentPropertiesMap = new HashMap<QName, Serializable>();
				// Validate that a content name has been set
				if (contentName.length() == 0) {
					contentName = "Direct Report_" + getDateTime() + ".html";
				}
				contentPropertiesMap.put(ContentModel.PROP_NAME, contentName);
				ChildAssociationRef createNode = nodeService.createNode(
						reportDirectory, ContentModel.ASSOC_CONTAINS,
						QName.createQName(contentName),
						RpsdModel.TYPE_RPSD_DOCUMENT, contentPropertiesMap);
				nodeService.addAspect(createNode.getChildRef(), aspectQName,
						aspectPropertiesMap);
				ContentWriter writer = contentService.getWriter(
						createNode.getChildRef(), ContentModel.PROP_CONTENT,
						true);
				writer.setMimetype("html/text");
				writer.putContent(directReportContent);
				arr.add(createNode.getChildRef().toString());
				if (logger.isDebugEnabled()) {
					logger.debug("Wrote content to node "
							+ createNode.getChildRef().toString());
				}
			} catch (ParseException e) {
				throw new WebScriptException(
						"Error while parsing request json object", e);
			}
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("items", arr);
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

	private final static String getDateTime() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hhmmss");
		// df.setTimeZone(TimeZone.getTimeZone("PST"));
		return df.format(new Date());
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

}
