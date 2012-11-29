package se.alingsas.alfresco.repo.utils.byggreda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class ByggRedaUtil {
	private static final Logger LOG = Logger.getLogger(ByggRedaUtil.class);
	public static final String SOURCE_TYPE_REPO = "repository";
	public static final String SOURCE_TYPE_FS = "filesystem";

	private static String sourceType;
	private static String siteName;
	private static String destinationPath;
	private static String logPath;

	private static SiteService siteService;
	private static NodeService nodeService;
	private static FileFolderService fileFolderService;
	private static ContentService contentService;

	/**
	 * Main method which runs an import of material.
	 * 
	 * @param sourcePath
	 *            The path where the files to import exist
	 * @param metaFileName
	 *            The file name of the meta file which should exist under
	 *            sourcePath
	 * @throws IOException 
	 * 
	 */
	public boolean run(String sourcePath, String metaFileName) throws IOException {
		LOG.debug("sourcePath: " + sourcePath + ", metaFileName: "
				+ metaFileName);
		if (!SOURCE_TYPE_REPO.equals(sourceType)
				&& !SOURCE_TYPE_FS.equals(sourceType)) {
			LOG.error("Invalid sourceType, this must be set through bean configuration. Expected '"
					+ SOURCE_TYPE_REPO
					+ "' or '"
					+ SOURCE_TYPE_FS
					+ "', was "
					+ sourceType);
			return false;
		}

		SiteInfo site = findSite(siteName);
		if (site == null) {
			LOG.error("Could not find site " + siteName);
			return false;
		}

		if (!StringUtils.hasText(sourcePath)) {
			LOG.error("Source path does not exist, was " + sourcePath);
			return false;
		}

		if (SOURCE_TYPE_REPO.equals(sourceType)) {
			if (sourcePath.startsWith("/")) {
				sourcePath = sourcePath.substring(1);
				LOG.debug("Stripping prefix / from sourcePath. Path is: "
						+ sourcePath);
			}
			if (sourcePath.endsWith("/")) {
				sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
				LOG.debug("Stripping postfix / from sourcePath. Path is: "
						+ sourcePath);
			}
		}

		if (!StringUtils.hasText(sourcePath)
				|| !sourceFolderExists(site, sourcePath)) {
			LOG.error("Source path does not exist, was " + sourcePath);
			return false;
		}

		if (!StringUtils.hasText(destinationPath)) {
			LOG.error("Destination path does not exist, was " + destinationPath);
			return false;
		}

		if (destinationPath.startsWith("/")) {
			destinationPath = destinationPath.substring(1);
			LOG.debug("Stripping prefix / from destinationPath. Path is: "
					+ destinationPath);
		}
		if (destinationPath.endsWith("/")) {
			destinationPath = destinationPath.substring(0,
					destinationPath.length() - 1);
			LOG.debug("Stripping postfix / from destinationPath. Path is: "
					+ destinationPath);
		}

		if (!StringUtils.hasText(destinationPath)
				|| !repoFolderExists(site, destinationPath)) {
			LOG.error("Destination path does not exist, was " + destinationPath);
			return false;
		}

		if (!StringUtils.hasText(logPath)) {
			LOG.error("Log path does not exist, was " + logPath);
			return false;
		}

		if (logPath.startsWith("/")) {
			logPath = logPath.substring(1);
			LOG.debug("Stripping prefix / from logPath. Path is: " + logPath);
		}
		if (logPath.endsWith("/")) {
			logPath = logPath.substring(0, logPath.length() - 1);
			LOG.debug("Stripping postfix / from logPath. Path is: " + logPath);
		}

		if (!StringUtils.hasText(logPath) || !repoFolderExists(site, logPath)) {
			LOG.error("Log path does not exist, was " + logPath);
			return false;
		}

		if (!StringUtils.hasText(metaFileName)) {
			LOG.error("No filename supplied");
			return false;
		}

		InputStream metadataFile = getMetadataFile(site, sourcePath,
				metaFileName);

		if (metadataFile == null) {
			LOG.error("Could not find metadata file at " + sourcePath + "/"
					+ metaFileName);
			return false;
		} else {
			Set<ByggRedaDocument> documents = null;
			try {
				documents = ReadMetadataDocument.read(metadataFile);
				
			} finally {
				IOUtils.closeQuietly(metadataFile);				
			}
			if (documents == null) {
				return false;
			}
			return true;
		}
	}

	private SiteInfo findSite(final String siteId) {
		if (!StringUtils.hasText(siteId)) {
			return null;
		}

		return siteService.getSite(siteId);
	}

	private boolean sourceFolderExists(SiteInfo site, String path) {
		if (sourceType.equals(SOURCE_TYPE_FS)) {
			// Check filesystem for source folder
			File f = new File(path);
			return f.exists();
		} else {
			return repoFolderExists(site, path);
		}
	}

	private FileInfo getRepoFileFolder(SiteInfo site, String path) {
		// Check Alfresco for folder
		final String[] parts = StringUtils
				.delimitedListToStringArray(path, "/");
		List<String> list = new ArrayList<String>();
		for (String part : parts) {
			list.add(part);
		}

		NodeRef rootNodeRef = fileFolderService.searchSimple(site.getNodeRef(),
				SiteService.DOCUMENT_LIBRARY);
		try {
			return fileFolderService.resolveNamePath(rootNodeRef, list, false);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private boolean repoFolderExists(SiteInfo site, String path) {
		return getRepoFileFolder(site, path) != null;
	}

	private InputStream getMetadataFile(SiteInfo site, String path,
			String fileName) {
		if (sourceType.equals(SOURCE_TYPE_FS)) {
			try {
				return new FileInputStream(path + "/" + fileName);
			} catch (java.io.FileNotFoundException e) {
				return null;
			}
		} else {
			FileInfo repoFileFolder = getRepoFileFolder(site, path + "/"
					+ fileName);
			if (repoFileFolder == null) {
				return null;
			} else {
				ContentReader rawReader = contentService
						.getRawReader(repoFileFolder.getContentData()
								.getContentUrl());
				return rawReader.getContentInputStream();
			}
		}
	}
	
	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		ByggRedaUtil.sourceType = sourceType;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		ByggRedaUtil.siteName = siteName;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		ByggRedaUtil.destinationPath = destinationPath;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		ByggRedaUtil.logPath = logPath;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		ByggRedaUtil.siteService = siteService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		ByggRedaUtil.nodeService = nodeService;
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		ByggRedaUtil.fileFolderService = fileFolderService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		ByggRedaUtil.contentService = contentService;
	}
}
