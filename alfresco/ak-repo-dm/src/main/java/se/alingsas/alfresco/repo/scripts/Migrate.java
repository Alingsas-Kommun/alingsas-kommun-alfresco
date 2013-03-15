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
	private static final String ARCHIVE_DOKUMENT = "dokument";
	private static final String ARCHIVE_EKONOMI = "ekonomi";
	private static final String ARCHIVE_SOCIAL = "social";
	private static final String ARCHIVE_VAN = "van";
	private static final String ARCHIVE_VERKSAMHETSSYSTEM = "verksamhetssystem";

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
		} else if (ARCHIVE_DOKUMENT.equals(archive)) {
			textfile += "Dokument_clean.txt";
			folder += "Files";
		} else if (ARCHIVE_EKONOMI.equals(archive)) {
			textfile += "Ekonomi_clean.txt";
			folder += "Files";
		} else if (ARCHIVE_VAN.equals(archive)) {
			textfile += "VAN_clean.txt";
			folder += "Files";
		} else if (ARCHIVE_SOCIAL.equals(archive)) {
			textfile += "Social_clean.txt";
			folder += "Files";
		} else if (ARCHIVE_VERKSAMHETSSYSTEM.equals(archive)) {
			textfile += "Verksamhetssystem_clean.txt";
			folder += "Files";
		} else {
			status.setCode(400);
			status.setMessage("Wrong archive argument: "+archive	);
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
