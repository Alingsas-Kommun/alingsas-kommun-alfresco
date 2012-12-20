package se.alingsas.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.alingsas.alfresco.repo.utils.byggreda.ByggRedaUtil;

public class ByggRedaImport extends DeclarativeWebScript implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(ByggRedaImport.class);

	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		final Map<String, Object> model = new HashMap<String, Object>();

		// get the site parameter
		final String sourcePath = req.getParameter("sourcePath");
		final String metaFileName = req.getParameter("metaFileName");
		
		ByggRedaUtil bru = new ByggRedaUtil();
		
		if (bru.run(sourcePath, metaFileName)) {
			model.put("result", bru.getLogMessage());			
		} else {
			status.setCode(400);
			status.setMessage("Failed to run ByggReda import");
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
