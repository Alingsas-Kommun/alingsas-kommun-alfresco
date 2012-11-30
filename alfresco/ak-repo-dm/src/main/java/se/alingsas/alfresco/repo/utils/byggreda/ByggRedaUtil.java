package se.alingsas.alfresco.repo.utils.byggreda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.model.AkDmModel;
import se.alingsas.alfresco.repo.scripts.ddmigration.AlingsasDocument;

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
	private static VersionService versionService;
	private static FileFolderService fileFolderService;
	private static ContentService contentService;
	private final String DOC_STATUS_COMPLETE = "Färdigt dokument";
	private final String DOC_SECRECY_PUBLIC = "Offentligt";
	
	private Set<ByggRedaDocument> documents = null;

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
	public boolean run(String sourcePath, String metaFileName)
			throws IOException {
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

		InputStream metadataFile = readFile(site, sourcePath, metaFileName);

		if (metadataFile == null) {
			LOG.error("Could not find metadata file at " + sourcePath + "/"
					+ metaFileName);
			return false;
		} else {
			
			try {
				documents = ReadMetadataDocument.read(metadataFile);

			} finally {
				IOUtils.closeQuietly(metadataFile);
			}
			if (documents == null) {
				return false;
			}
			documents = importDocuments(site, sourcePath, documents);
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

	private InputStream readFile(SiteInfo site, String path, String fileName) {
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

	/**
	 * Import documents which were successfully read into Alfresco, return a
	 * list with the result This class expects all input data to already have
	 * been validated. Therefore it will not be checked again
	 * 
	 * @param site
	 * @param destinationPath
	 * @param documents
	 * @return
	 */
	public Set<ByggRedaDocument> importDocuments(SiteInfo site,
			String sourcePath, final Set<ByggRedaDocument> documentSet) {
		LOG.info("Starting import of documents from ByggReda");
		Set<ByggRedaDocument> result = new HashSet<ByggRedaDocument>();
		Iterator<ByggRedaDocument> it = documentSet.iterator();
		while (it.hasNext()) {
			ByggRedaDocument next = it.next();
			if (next.readSuccessfully) {
				ByggRedaDocument importedDocument = importDocument(site,
						sourcePath, next);
				result.add(importedDocument);
			} else {
				result.add(next);
			}
		}
		LOG.info("Finished import of documents from ByggReda");
		return result;
	}

	/**
	 * Import a document
	 * 
	 * @param site
	 * @param sourcePath
	 * @param document
	 * @return
	 */
	private ByggRedaDocument importDocument(SiteInfo site, String sourcePath,
			ByggRedaDocument document) {
		destinationPath = destinationPath + "/"
				+ document.buildingDescription.substring(0, 1).toUpperCase()
				+ "/" + document.buildingDescription.toUpperCase();
		
		try {
			NodeRef folderNodeRef = createFolder(destinationPath, site);
			final FileInfo fileInfo = fileFolderService.create(folderNodeRef,
					document.fileName, AkDmModel.TYPE_AKDM_BYGGREDA_DOC);
			document.nodeRef = fileInfo.getNodeRef();
			addProperties(document.nodeRef, document);
			createFile(document.nodeRef, site, sourcePath, document);
			createVersionHistory(document.nodeRef);
			document.readSuccessfully = true;
			LOG.debug("Imported document "+document.recordNumber);
		} catch (FileExistsException ex) {
			document.readSuccessfully = false;
			document.errorMsg = "File already exists at path "
					+ destinationPath;
			LOG.error(document.errorMsg);
		} catch (Exception e) {
			document.readSuccessfully = false;
			document.errorMsg = e.getMessage();
			LOG.error("Error importing document "+document.recordNumber,e);
		}
		return document;

	}
	
	private void createVersionHistory(final NodeRef nodeRef) {
		final VersionHistory versionHistory = getVersionService()
				.getVersionHistory(nodeRef);

		if (versionHistory == null) {
			// check it in again, with supplied version history note
			final Map<String, Serializable> properties = new HashMap<String, Serializable>();
			properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			versionService.createVersion(nodeRef, properties);
		}
	}

	private void createFile(NodeRef nodeRef, SiteInfo site, String sourcePath,
			ByggRedaDocument document) {
		InputStream inputStream = readFile(site, sourcePath, document.fileName);
		if (inputStream != null) {
			try {
				final ContentWriter writer = contentService.getWriter(nodeRef,
						ContentModel.PROP_CONTENT, true);

				writer.setMimetype(document.mimetype);

				writer.putContent(inputStream);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

	/**
	 * Create new or return existing folder
	 * 
	 * @param filepath
	 * @param site
	 * @return
	 */
	private NodeRef createFolder(final String filepath, final SiteInfo site) {
		NodeRef rootNodeRef = fileFolderService.searchSimple(site.getNodeRef(),
				SiteService.DOCUMENT_LIBRARY);

		final String[] parts = StringUtils.delimitedListToStringArray(filepath,
				"/");

		for (String part : parts) {
			part = StringUtils.trimWhitespace(part);

			NodeRef folder = fileFolderService.searchSimple(rootNodeRef, part);

			while (folder == null) {
				folder = fileFolderService.create(rootNodeRef, part,
						AkDmModel.TYPE_AKDM_FOLDER).getNodeRef();
			}

			rootNodeRef = folder;
		}

		return rootNodeRef;
	}
	
	private void addProperties(final NodeRef nodeRef,
			final ByggRedaDocument document) {
		final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

		addProperty(properties, ContentModel.PROP_NAME, document.fileName);
		

		// final String checksum = _serviceUtils.getChecksum(document.file);
		// Alfresco general properties
		addProperty(properties, ContentModel.PROP_AUTO_VERSION_PROPS, true);
		addProperty(properties, ContentModel.PROP_AUTO_VERSION, true);
		addProperty(properties, ContentModel.PROP_TITLE, document.recordNumber + " "+document.issuePurpose);
		addProperty(properties, ContentModel.PROP_CREATOR, AuthenticationUtil.SYSTEM_USER_NAME);
		addProperty(properties, ContentModel.PROP_MODIFIER, AuthenticationUtil.SYSTEM_USER_NAME);
		//addProperty(properties, ContentModel.PROP_MODIFIED, document.createdDate);
		//addProperty(properties, ContentModel.PROP_CREATED, document.createdDate);
		//addProperty(properties, ContentModel.PROP_DESCRIPTION, document.description);

		// Common
		

		addProperty(properties, AkDmModel.PROP_AKDM_DOC_STATUS,
				DOC_STATUS_COMPLETE);
		addProperty(properties, AkDmModel.PROP_AKDM_DOC_SECRECY,
				DOC_SECRECY_PUBLIC);
		//ByggReda
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_FILM,
				document.film);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_SERIAL_NUMBER,
				document.serialNumber);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_RECORD_NUMBER,
				document.recordNumber);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_BUILDING_DESCR,
				document.buildingDescription);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_LAST_BUILDING_DESCR,
				document.lastBuildingDescription);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_ADDRESS,
				document.address);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_LAST_ADDRESS,
				document.lastAddress);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_DECISION,
				document.decision);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_FOR,
				document.forA);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_ISSUE_PURPOSE,
				document.issuePurpose);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_NOTE,
				document.note);
		addProperty(properties, AkDmModel.PROP_AKDM_BYGGREDA_RECORDS,
				document.records);
		

		nodeService.addProperties(nodeRef, properties);
	}
	
	private void addProperty(final Map<QName, Serializable> properties,
			final QName key, Serializable value) {
		// if no text, just exit
		if (value == null) {
			return;
		}

		// if no text, just exit
		if (!StringUtils.hasText(value.toString())) {
			return;
		}

		// trim all trailing spaces for strings
		if (value instanceof String) {
			value = StringUtils.trimTrailingWhitespace(value.toString());
		}

		properties.put(key, value);
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

	public VersionService getVersionService() {
		return versionService;
	}

	public void setVersionService(VersionService versionService) {
		ByggRedaUtil.versionService = versionService;
	}

	public Set<ByggRedaDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(Set<ByggRedaDocument> documents) {
		this.documents = documents;
	}
}
