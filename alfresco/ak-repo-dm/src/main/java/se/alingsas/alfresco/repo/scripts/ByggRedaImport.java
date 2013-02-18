package se.alingsas.alfresco.repo.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.alingsas.alfresco.repo.utils.byggreda.ByggRedaUtil;

public class ByggRedaImport extends DeclarativeWebScript implements
		InitializingBean {

	private static final Logger LOG = Logger.getLogger(ByggRedaImport.class);
	private static ServiceRegistry serviceRegistry;
	private static String siteName;
	private static String basePath;

	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		final Map<String, Object> model = new HashMap<String, Object>();
		// Check permissions since only site collaborators and managers should
		// be able to run this action

		SiteService siteService = serviceRegistry.getSiteService();
		MutableAuthenticationService authenticationService = serviceRegistry
				.getAuthenticationService();
		String currentUserName = authenticationService.getCurrentUserName();
		String membersRole = siteService.getMembersRole(siteName,
				currentUserName);
		if ("SiteManager".equals(membersRole)
				|| "SiteCollaborator".equals(membersRole)) {
			// get the site parameter
			final String sourcePath = basePath + "/"
					+ req.getParameter("sourcePath");
			final String metaFileName = req.getParameter("metaFileName");
			final boolean updateExisting = "yes".equals(req
					.getParameter("updateIfExists"));

			File f = new File(sourcePath);
			if (!f.exists()) {
				status.setCode(400);
				//status.setMessage("Kan inte läsa källmappen " + sourcePath
				//		+ ". Kontrollera att du skrivit in korrekt sökväg.");
				//status.setRedirect(true);
				model.put("result", "Kan inte läsa källmappen " + sourcePath
						+ ". Kontrollera att du skrivit in korrekt sökväg.");
				return null;
			}
			f = new File(sourcePath + "/" + metaFileName);
			if (!f.exists()) {
				status.setCode(400);
				//status.setMessage("Kan inte läsa styrfilen " + sourcePath + "/"
				//		+ metaFileName
				//		+ ". Kontrollera att du skrivit in korrekt sökväg.");
				//status.setRedirect(true);
				model.put("result", "Kan inte läsa styrfilen " + sourcePath + "/"
						+ metaFileName
						+ ". Kontrollera att du skrivit in korrekt sökväg.");
				return null;
			}
			ByggRedaUtil bru = new ByggRedaUtil();
			bru.setUpdateExisting(updateExisting);
			bru.setSourcePath(sourcePath);
			bru.setMetaFileName(metaFileName);
			bru.setRunAs(AuthenticationUtil.getFullyAuthenticatedUser());
			if (!bru.validateParams()) {
				status.setCode(400);
				//status.setMessage("Kunde inte validera inparametrar. Kontrollera systemloggen för mer detaljer.");
				model.put("result", "Kunde inte validera inparametrar. Kontrollera systemloggen för mer detaljer.");
				//status.setRedirect(true);
			} else {
				Thread t = new Thread(bru);
				t.start();
				model.put("result", "Byggredaimport startad. Du kan nu stänga denna sidan.");
			}
		} else {
			status.setCode(403);
			model.put("result", "Åtkomst nekad, du måste vara ansvarig eller dokumentansvarig för samarbetsytan för att använda detta verktyg.");
			//status.setMessage("Access denied, you must be site manager or site collaborator to run this tool");
			//status.setRedirect(true);
			return null;
		}

		return model;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		ByggRedaImport.serviceRegistry = serviceRegistry;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		ByggRedaImport.siteName = siteName;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		ByggRedaImport.basePath = basePath;
	}

}
