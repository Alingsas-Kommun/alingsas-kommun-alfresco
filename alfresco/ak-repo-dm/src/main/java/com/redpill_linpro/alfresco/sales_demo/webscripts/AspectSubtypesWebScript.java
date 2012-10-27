package com.redpill_linpro.alfresco.sales_demo.webscripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class AspectSubtypesWebScript extends AbstractWebScript {
	private static final Logger logger = Logger
			.getLogger(AspectSubtypesWebScript.class);
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		String aspectName = getAspectName(req);
		if (logger.isDebugEnabled()) {
			logger.debug("Get sub-aspects for type: " + aspectName);
		}
		String[] split = aspectName.split(":");
		// build a json array
		ArrayList<String> arr = new ArrayList<String>();
		if (split != null && split.length == 2) {

			QName aspectQName = QName.createQName(split[0], split[1],
					getNamespaceService());
			Collection<QName> subAspects = dictionaryService.getSubAspects(
					aspectQName, false);
			for (QName subAspect : subAspects) {
				arr.add(subAspect.getPrefixString());
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
		res.setContentType("application/json;charset=UTF-8"); //Fix to force the response to be interpreted as json
		res.getWriter().write(jsonString);		

	}

	public String getAspectName(WebScriptRequest req) {
		String param = req.getParameter("aspectName");
		if (param == null) {
			Map<String, String> templateArgs = req.getServiceMatch()
					.getTemplateVars();
			param = templateArgs.get("aspectName");
		}
		if (param == null) {
			throw new WebScriptException("Parameter aspectName not found");
		}
		return param;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

}
