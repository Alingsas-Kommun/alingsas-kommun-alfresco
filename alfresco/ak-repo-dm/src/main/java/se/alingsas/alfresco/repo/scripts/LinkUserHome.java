package se.alingsas.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.alingsas.alfresco.repo.scripts.userhome.UserHomesLinkingUtil;

public class LinkUserHome extends DeclarativeWebScript implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(LinkUserHome.class);

	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		final Map<String, Object> model = new HashMap<String, Object>();

		// get the site parameter
		final String siteId = req.getParameter("site");
		
		if (siteId == null) {
			status.setCode(400);
			status.setMessage("Site is a mandatory argument");
			status.setRedirect(true);
			return null;
		}
		UserHomesLinkingUtil uhl = new UserHomesLinkingUtil();
		if (uhl.linkUserHomesWithSite(siteId)) {
			model.put("result", "success");			
		} else {
			status.setCode(400);
			status.setMessage("Failed to link user home to site. Does the site exist? Does the link already exist?");
			status.setRedirect(true);
			return null;
		}
		

		return model;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
