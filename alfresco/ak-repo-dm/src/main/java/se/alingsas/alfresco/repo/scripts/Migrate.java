package se.alingsas.alfresco.repo.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;

import se.alingsas.alfresco.repo.scripts.ddmigration.MigrationCollection;
import se.alingsas.alfresco.repo.scripts.ddmigration.MigrationUtil;

public class Migrate extends DeclarativeWebScript implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(Migrate.class);

	private static final String ARCHIVE_TEST = "test";

	private String _baseFolder;

	protected boolean _onlyMetadata = false;

	public void setBaseFolder(final String baseFolder) {
		_baseFolder = baseFolder;
	}

	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		final Map<String, Object> model = new HashMap<String, Object>();
		// get the archive to migrate
		final String archive = req.getParameter("archive");

		// get the site parameter
		final String siteId = req.getParameter("site");

		String textfile = _baseFolder + "/";
		String folder = _baseFolder + "/";

		if (ARCHIVE_TEST.equals(archive)) {
			textfile += "Test/Test Metadata.txt";
			folder += "Test/Files";
		} else {
			status.setCode(400);
			status.setMessage("Wrong archive argument.");
			status.setRedirect(true);

			return null;
		}

		if (siteId == null) {
			status.setCode(400);
			status.setMessage("Site is a mandatory argument");
			status.setRedirect(true);
			return null;
		}

		MigrationUtil mu = new MigrationUtil();
		MigrationCollection migratedDocuments = mu.run(new File(textfile),
				folder, siteId);
		if (migratedDocuments == null) {
			status.setCode(400);
			status.setMessage("Migration failed, site does not exist or other error occured, please check the server logs for more information.");
			status.setRedirect(true);
			return null;
		}
		model.put("migrated", migratedDocuments.noOfDocuments());

		return model;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(_baseFolder);
	}

}
