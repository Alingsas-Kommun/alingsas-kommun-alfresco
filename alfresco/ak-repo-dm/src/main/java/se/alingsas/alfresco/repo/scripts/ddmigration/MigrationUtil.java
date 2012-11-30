package se.alingsas.alfresco.repo.scripts.ddmigration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class MigrationUtil {
	private static final Logger LOG = Logger.getLogger(MigrationUtil.class);
	private static final String EXTENSION_CHARACTER = ".";
	private static final String MSG_WORKING_COPY_LABEL = "(Working Copy)";
	protected static FileFolderService fileFolderService;
	protected static SiteService siteService;
	protected static NodeService nodeService;
	protected static CheckOutCheckInService checkOutCheckInService;
	protected static VersionService versionService;
	protected static ContentService contentService;
	protected static SearchService searchService;
	private static BehaviourFilter behaviourFilter;
	private static ServiceRegistry serviceRegistry;

	public MigrationCollection run(File f, String folder, String siteId) {

		SiteInfo site = findSite(siteId);
		if (site == null) {
			LOG.error("Site " + siteId + " does not exist, aborting");
			return null;
		}

		MigrationCollection readDocuments = readDocuments(f, folder);
		LOG.info(readDocuments.noOfDocuments()
				+ " documents prepared for migration");
		LOG.info(readDocuments.noOfDocumentVersions()
				+ " documents (including versions) prepared for migration");
		MigrationCollection migratedDocuments = migrateDocuments(readDocuments,
				site);
		LOG.info(migratedDocuments.noOfDocuments()
				+ " documents migrated successfully");
		LOG.info(migratedDocuments.noOfDocumentVersions()
				+ " documents (including versions) migrated successfully");
		if (readDocuments.noOfDocuments() != migratedDocuments.noOfDocuments()
				|| readDocuments.noOfDocumentVersions() != migratedDocuments
						.noOfDocumentVersions()) {
			LOG.warn("Not all documents were migrated, please look in the logs for more details");
		}
		return migratedDocuments;
	}

	/**
	 * Will loop through all documents and migrate them
	 * 
	 * @param documents
	 * @return
	 */
	public MigrationCollection migrateDocuments(
			final MigrationCollection documents, final SiteInfo site) {
		MigrationCollection migratedDocuments = new MigrationCollection();
		if (site == null) {
			LOG.error("Site is null, aborting");
			return migratedDocuments;
		}

		RetryingTransactionHelper txnHelper = getServiceRegistry()
				.getRetryingTransactionHelper();
		migratedDocuments = txnHelper.doInTransaction(
				new RetryingTransactionCallback<MigrationCollection>() {
					@Override
					public MigrationCollection execute() {
						// Disable the auditable aspect's behaviours for this
						// transaction, to allow creation & modification dates
						// to be set
						behaviourFilter
								.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
						MigrationCollection migratedDocuments = new MigrationCollection();
						Set<String> keys = documents.getKeys();
						Iterator<String> it = keys.iterator();
						while (it.hasNext()) {
							String key = it.next();
							Set<AlingsasDocument> document = documents.get(key);
							migratedDocuments = migrateDocument(
									migratedDocuments, document, site);
						}
						return migratedDocuments;
					}
				}, false, false);

		return migratedDocuments;
	}

	/**
	 * Will migrate a single document
	 * 
	 * @param migratedDocuments
	 * @param document
	 * @return
	 */
	private MigrationCollection migrateDocument(
			final MigrationCollection migratedDocuments,
			final Set<AlingsasDocument> document, final SiteInfo site) {

		Iterator<AlingsasDocument> it = document.iterator();
		String documentNumber = "N/A";
		AlingsasDocument lastVersion = null;
		int noOfPreviousVersion = checkPreviousVersion(document, site);
		if (noOfPreviousVersion > 0) {
			LOG.info("Document "
					+ ((AlingsasDocument) document.toArray()[0]).documentNumber
					+ " have " + noOfPreviousVersion
					+ " versions already migrated, skipping those");
		}
		for (int i = 0; i < noOfPreviousVersion; i++) {
			if (it.hasNext()) {
				AlingsasDocument version = it.next();
				migratedDocuments.put(version.documentNumber, version);
				documentNumber = version.documentNumber;
			}
		}
		while (it.hasNext()) {
			AlingsasDocument version = it.next();
			if (migrateDocumentVersion(version, lastVersion, site)) {
				migratedDocuments.put(version.documentNumber, version);
				documentNumber = version.documentNumber;
				LOG.debug("Migrated document " + version.documentNumber
						+ " v. " + version.version);
			} else {
				LOG.warn("Version " + version.version + " of document "
						+ version.documentNumber + " was not migrated");
			}
			lastVersion = version;
		}

		LOG.info("Completed migrating document " + documentNumber);

		return migratedDocuments;

	}

	private int checkPreviousVersion(Set<AlingsasDocument> documents,
			SiteInfo site) {

		for (AlingsasDocument version : documents) {
			NodeRef folder = createFolder(fileFolderService, version.filePath, site);
			if (folder != null) {
				NodeRef nodeRef = nodeService.getChildByName(folder,
						ContentModel.ASSOC_CONTAINS, version.fileName);
				if (nodeRef != null) {
					VersionHistory versionHistory = versionService
							.getVersionHistory(nodeRef);
					return versionHistory.getAllVersions().size();
				}
			}
		}
		return 0;
	}

	/**
	 * Will migrate a single version of a document
	 * 
	 * @param lastVersion
	 * 
	 * @param document
	 * @return
	 */
	private boolean migrateDocumentVersion(final AlingsasDocument version,
			AlingsasDocument lastVersion, final SiteInfo site) {
		final NodeRef versionNodeRef = createVersion(version, lastVersion, site);
		return (versionNodeRef != null);
	}

	/**
	 * Create a version of a document
	 * 
	 * @param folderNodeRef
	 * @param version
	 * @param lastVersion
	 * @param site
	 * @return
	 */
	private NodeRef createVersion(AlingsasDocument version,
			AlingsasDocument lastVersion, SiteInfo site) {
		NodeRef childByName;
		NodeRef destinationFolderRef;
		NodeRef sourceFolderRef;

		destinationFolderRef = createFolder(fileFolderService, version.filePath, site);
		/**
		 * Check if file was moved to another folder
		 */
		if (lastVersion != null
				&& !version.filePath.equals(lastVersion.filePath)) {
			sourceFolderRef = createFolder(fileFolderService, lastVersion.filePath, site);
		} else {
			sourceFolderRef = destinationFolderRef;
		}

		/**
		 * If the file was renamed, then get the last version
		 */
		if (lastVersion != null
				&& !version.fileName.equals(lastVersion.fileName)) {
			childByName = nodeService.getChildByName(sourceFolderRef,
					ContentModel.ASSOC_CONTAINS, lastVersion.fileName);
		} else {
			childByName = nodeService.getChildByName(sourceFolderRef,
					ContentModel.ASSOC_CONTAINS, version.fileName);
		}

		if (childByName != null) {
			// File already exists
			Map<QName, Serializable> properties = nodeService
					.getProperties(childByName);
			String docNumber = (String) properties
					.get(AkDmModel.PROP_AKDM_DOC_NUMBER);
			if (!version.documentNumber.equals(docNumber)) {
				LOG.warn("Document name collission! File " + version.filePath
						+ version.fileName
						+ " already exists. Existing document: " + docNumber
						+ ", New document: " + version.documentNumber
						+ ", Renaming document to " + version.documentNumber
						+ "." + version.fileExtension);
				version.fileName = version.documentNumber + "."
						+ version.fileExtension;
				childByName = nodeService.getChildByName(sourceFolderRef,
						ContentModel.TYPE_CONTENT, version.fileName);
			} else {
				VersionNumber existingVersion = new VersionNumber(
						(String) properties
								.get(ContentModel.PROP_VERSION_LABEL));
				VersionNumber thisVersion = new VersionNumber(version.version);
				if (thisVersion.compareTo(existingVersion) <= 0) {
					LOG.info("Document " + version.documentNumber + " v."
							+ version.version + " already migrated, skipping");
					return childByName;
				}
			}
		}

		if (childByName != null) {
			try {
				return createNewVersion(childByName, version, sourceFolderRef,
						destinationFolderRef);
			} catch (FileExistsException e) {
				LOG.error("File " + version.filePath + "/" + version.fileName
						+ " already exists, aborting...");
				return null;
			} catch (FileNotFoundException e) {
				LOG.error("File " + version.filePath + "/" + version.fileName
						+ " could not be found, aborting...");
				return null;
			}
		} else {
			return createFirstVersion(destinationFolderRef, version);
		}
	}

	/**
	 * Create the first version of a document
	 * 
	 * @param folderNodeRef
	 * @param version
	 * @return
	 */
	private NodeRef createFirstVersion(NodeRef folderNodeRef,
			AlingsasDocument version) {
		// create the new file
		String fileName;
		if ("".equals(version.fileName) || version.fileName==null) {
			fileName = version.title;
		} else {
			fileName = version.fileName;
		}
		final FileInfo fileInfo = fileFolderService.create(folderNodeRef,
				fileName, version.documentTypeQName);
		NodeRef baseVersion = fileInfo.getNodeRef();
		addProperties(baseVersion, version, false);
		// add the file
		addFile(baseVersion, version);
		// create the version history
		createVersionHistory(baseVersion, version);
		return baseVersion;
	}

	/**
	 * Create a new version of an existing document, checking if the version
	 * already exists should be done outside of this method
	 * 
	 * @param childByName
	 * @param version
	 * @param destinationFolderRef
	 * @param sourceFolderRef
	 * @return
	 * @throws FileNotFoundException
	 * @throws FileExistsException
	 */
	private NodeRef createNewVersion(NodeRef baseVersion,
			AlingsasDocument version, NodeRef sourceFolderRef,
			NodeRef destinationFolderRef) throws FileExistsException,
			FileNotFoundException {

		// adjust the versioning if needed needed?
		// baseVersion = adjustMajorVersioning(baseVersion, document);
		final NodeRef workingCopy = checkOutCheckInService
				.checkout(baseVersion);

		addProperties(workingCopy, version, true);
		addFile(workingCopy, version);
		final String madeBy = version.createdBy;
		NodeRef checkinVersion = checkinVersion(
				workingCopy,
				isMajorVersion(version) ? VersionType.MAJOR : VersionType.MINOR,
				madeBy);
		if (!sourceFolderRef.equals(destinationFolderRef)) {
			// Move the file
			fileFolderService.move(checkinVersion, destinationFolderRef,
					version.fileName);
			LOG.debug("Moving document " + version.documentNumber);
		}

		return checkinVersion;
	}

	private void addProperties(final NodeRef nodeRef,
			final AlingsasDocument document, final boolean workingCopy) {
		final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

		if (workingCopy) {
			final String name = nodeService.getProperty(nodeRef,
					ContentModel.PROP_NAME).toString();

			final String workingCopyName = createWorkingCopyName(document.fileName);

			if (!name.equalsIgnoreCase(workingCopyName)) {
				addProperty(properties, ContentModel.PROP_NAME,
						document.fileName);
			}
		} else {
			addProperty(properties, ContentModel.PROP_NAME, document.fileName);
		}

		// final String checksum = _serviceUtils.getChecksum(document.file);
		// Alfresco general properties
		addProperty(properties, ContentModel.PROP_AUTO_VERSION_PROPS, true);
		addProperty(properties, ContentModel.PROP_AUTO_VERSION, true);
		addProperty(properties, ContentModel.PROP_TITLE, document.title);
		addProperty(properties, ContentModel.PROP_CREATOR, document.createdBy);
		addProperty(properties, ContentModel.PROP_MODIFIER, document.createdBy);
		addProperty(properties, ContentModel.PROP_MODIFIED,
				document.createdDate);
		addProperty(properties, ContentModel.PROP_CREATED, document.createdDate);
		addProperty(properties, ContentModel.PROP_DESCRIPTION,
				document.description);

		// Common
		addProperty(properties, AkDmModel.PROP_AKDM_DOC_NUMBER,
				document.documentNumber);
		addProperty(properties, AkDmModel.PROP_AKDM_DOC_STATUS,
				document.documentStatus);
		addProperty(properties, AkDmModel.PROP_AKDM_DOC_SECRECY,
				document.secrecy);
		// Anvisning & Författning properties
		addProperty(properties, AkDmModel.PROP_AKDM_PROTOCOL_ID,
				document.protocolId);
		addProperty(properties, AkDmModel.PROP_AKDM_INSTRUCTION_DECISION_DATE,
				document.decisionDate);
		addProperty(properties, AkDmModel.PROP_AKDM_INSTRUCTION_GROUP,
				document.group);
		// Handbok properties
		addProperty(properties, AkDmModel.PROP_AKDM_MANUAL_FUNCTION,
				document.function);
		addProperty(properties, AkDmModel.PROP_AKDM_MANUAL_MANUAL,
				document.handbook);
		// General document
		addProperty(properties, AkDmModel.PROP_AKDM_GENERAL_DOCUMENT_DATE,
				document.generalDocumentDate);
		addProperty(properties,
				AkDmModel.PROP_AKDM_GENERAL_DOCUMENT_DESCRIPTION,
				document.generalDocumentDescription);

		nodeService.addProperties(nodeRef, properties);
	}

	private void createVersionHistory(final NodeRef baseVersion, AlingsasDocument version) {
		final VersionHistory versionHistory = versionService
				.getVersionHistory(baseVersion);

		if (versionHistory == null) {
			// check it in again, with supplied version history note
			final Map<String, Serializable> properties = new HashMap<String, Serializable>();
			properties.put(VersionModel.PROP_VERSION_TYPE, isMajorVersion(version) ? VersionType.MAJOR : VersionType.MINOR);
			versionService.createVersion(baseVersion, properties);
		}
	}

	/**
	 * Adds a file to the nodeRef based on the document.
	 * 
	 * @param nodeRef
	 *            The nodeRef to add the document for.
	 * @param document
	 *            The document to read the file info from.
	 */
	private void addFile(final NodeRef nodeRef, final AlingsasDocument document) {

		InputStream inputStream = null;
		if (document.file.exists()) {
			try {

				inputStream = new FileInputStream(document.file);

				final ContentWriter writer = contentService.getWriter(nodeRef,
						ContentModel.PROP_CONTENT, true);

				writer.setMimetype(document.mimetype);

				writer.putContent(inputStream);

			} catch (final Exception ex) {
				IOUtils.closeQuietly(inputStream);

				throw new RuntimeException(ex);
			}
		} else {
			LOG.warn(document.documentNumber + " file does not exist, creating empty file in repository with metadata");
		}
	}

	/**
	 * Checks in the working copy, sets the modifier and creator to madeBy.
	 * 
	 * @param workingCopy
	 *            The working copy to check in.
	 * @param versionType
	 *            Major or minor version.
	 * @param madeBy
	 *            Made by is used for modifier and creator.
	 * @return The nodeRef of the revisioned node.
	 */
	private NodeRef checkinVersion(final NodeRef workingCopy,
			final VersionType versionType, final String madeBy) {
		final Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(VersionModel.PROP_VERSION_TYPE, versionType);

		if (nodeService.getProperty(workingCopy, ContentModel.PROP_MODIFIER) == null) {
			nodeService.setProperty(workingCopy, ContentModel.PROP_MODIFIER,
					madeBy);
		}

		if (nodeService.getProperty(workingCopy, ContentModel.PROP_CREATOR) == null) {
			nodeService.setProperty(workingCopy, ContentModel.PROP_CREATOR,
					madeBy);
		}

		return checkOutCheckInService.checkin(workingCopy, properties);
	}

	/**
	 * Create new or return existing folder
	 * 
	 * @param filepath
	 * @param site
	 * @return
	 */
	private NodeRef createFolder(FileFolderService fileFolderService, final String filepath, final SiteInfo site) {
		NodeRef rootNodeRef = fileFolderService.searchSimple(site.getNodeRef(),
				SiteService.DOCUMENT_LIBRARY);

		final String[] parts = StringUtils.delimitedListToStringArray(filepath,
				"/");

		for (String part : parts) {
			part = StringUtils.trimWhitespace(part);

			NodeRef folder = fileFolderService.searchSimple(rootNodeRef, part);

			while (folder == null) {
				try {
					folder = fileFolderService.create(rootNodeRef, part,
							AkDmModel.TYPE_AKDM_FOLDER).getNodeRef();
				} catch (FileExistsException e) {
					try {
						LOG.debug("Folder not found with search but exists. Search index might not have synchronized in time, sleeping 1s");
						Thread.sleep(1000);
						folder = fileFolderService.searchSimple(rootNodeRef,
								part);

					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						LOG.error(e1);
						break;
					}
				}

			}

			rootNodeRef = folder;
		}

		return rootNodeRef;
	}

	/**
	 * Read the documents into a collection using a file as input
	 * 
	 * @param f
	 *            The meta file to read
	 * @param folder
	 *            The folder where the actual files exist
	 * @return
	 */
	public MigrationCollection readDocuments(File f, String folder) {
		MigrationCollection collection = new MigrationCollection();
		if (f == null) {
			LOG.error("File object is null");
		} else if (!f.exists()) {
			LOG.error("File containing metadata about documents did not exist! "
					+ f.getAbsolutePath());
		} else {
			LOG.info("Reading documents from file " + f.getAbsolutePath());
			collection = createDocumentList(collection, f.getAbsolutePath(),
					folder);

		}
		return collection;
	}

	private MigrationCollection createDocumentList(
			MigrationCollection collection, final String textfile,
			final String folder) {
		InputStream inputStream = null;
		LineIterator lineIterator = null;
		int lineNumber = 0;

		try {
			inputStream = new FileInputStream(textfile);
			lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			while (lineIterator.hasNext()) {
				lineNumber++;
				final String line = lineIterator.nextLine();
				// if it's an empty line or a comment, skip
				if (!StringUtils.hasText(line) || line.startsWith("#")) {
					continue;
				}
				final String[] parts = StringUtils.delimitedListToStringArray(
						line, "#!#");
				if (parts.length < 1) {
					continue;
				}
				AlingsasDocument document = createDocument(parts, folder);
				if (document == null) {
					LOG.error("Document found on line number '" + lineNumber
							+ "' has no document number, skipping...");

					continue;
				}
				document.lineNumber = lineNumber;

				collection.put(document.documentNumber, document);
			}
			return collection;
		} catch (final Exception ex) {
			throw new RuntimeException("Error on line '" + lineNumber + "'", ex);
		} finally {
			IOUtils.closeQuietly(inputStream);
			LineIterator.closeQuietly(lineIterator);
		}
	}

	private AlingsasDocument createDocument(final String[] parts, String folder) {
		final AlingsasDocument document = new AlingsasDocument();

		int position = 0;
		document.tmpStorageFolder = folder;

		document.documentNumber = parts[position];
		document.createdBy = parts[++position];
		document.createdDate = parseDate(parts[++position]);
		document.title = parts[++position];
		document.fileName = parts[++position];
		document.version = parseVersion(parts[++position]);
		document.documentType = parts[++position];
		document.documentStatus = parts[++position];
		document.secrecy = parts[++position];
		document.protocolId = parts[++position];
		document.decisionDate = parseDate(parts[++position]);
		document.group = parts[++position];
		document.function = parts[++position];
		document.handbook = parts[++position];
		document.generalDocumentDescription = parts[++position];
		document.generalDocumentDate = parseDate(parts[++position]);
		document.filePath = parseFilePath(parts[++position]);
		document.ddUUID = parts[++position];
		document.description = parts[++position];
		document.fileExtension = FilenameUtils.getExtension(document.fileName);
		document.file = new File(document.tmpStorageFolder + "/"
				+ document.ddUUID + '.' + document.fileExtension);
		document.mimetype = getMimetype(FilenameUtils
				.getExtension(document.fileName));
		// document.fileExtension = parts[++position];
		// document.createdDate = parseDate(parts[++position]);
		// document.fileName = parts[++position];

		// Internal properties
		document.documentTypeQName = parseKnownQNames(document.documentType);
		if (!StringUtils.hasText(document.documentNumber)) {
			return null;
		}
		return document;
	}

	public QName parseKnownQNames(String type) throws IllegalArgumentException {
		if ("Anvisning".equals(type)) {
			return AkDmModel.TYPE_AKDM_INSTRUCTION;
		} else if ("Författning".equalsIgnoreCase(type)) {
			return AkDmModel.TYPE_AKDM_ORDINANCE;
		} else if ("Handbok".equalsIgnoreCase(type)) {
			return AkDmModel.TYPE_AKDM_MANUAL;
		} else if ("Protokoll".equalsIgnoreCase(type)) {
			return AkDmModel.TYPE_AKDM_PROTOCOL;
		} else if ("Generellt Dokument".equalsIgnoreCase(type)) {
			return AkDmModel.TYPE_AKDM_GENERAL_DOC;
		} else if ("Ekonomidokument".equalsIgnoreCase(type)) {
			return AkDmModel.TYPE_AKDM_ECONOMY_DOC;
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	public boolean validateFile(AlingsasDocument d) {
		File f = new File(d.tmpStorageFolder + d.ddUUID + '.' + d.fileExtension);
		return f.exists();
	}

	private Date parseDate(final String date) {
		if (!StringUtils.hasText(date)) {
			return null;
		}

		try {
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			return simpleDateFormat.parse(date);
		} catch (final ParseException ex) {
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd");

			try {
				return simpleDateFormat.parse(date);
			} catch (final ParseException ex1) {
				throw new RuntimeException(ex1);
			}
		}
	}

	private String parseVersion(final String value) {
		if (value.equals("1.")) {
			LOG.warn("Wrong version '" + value
					+ "', replaced with version '1.0'");

			return "1.0";
		}

		try {
			Float.parseFloat(value);

			return value;
		} catch (final Exception ex) {
			LOG.warn("Wrong version '" + value
					+ "', replaced with version '0.1'");

			return "0.1";
		}
	}

	private boolean isMajorVersion(final AlingsasDocument document) {
		final String[] parts = StringUtils.split(document.version, ".");

		return parts[1].equals("0");
	}

	private String createWorkingCopyName(String name) {
		if (this.getWorkingCopyLabel() != null
				&& this.getWorkingCopyLabel().length() != 0) {
			if (name != null && name.length() != 0) {
				final int index = name.lastIndexOf(EXTENSION_CHARACTER);
				if (index > 0) {
					// Insert the working copy label before the file extension
					name = name.substring(0, index) + " "
							+ getWorkingCopyLabel() + name.substring(index);
				} else {
					// Simply append the working copy label onto the end of the
					// existing
					// name
					name = name + " " + getWorkingCopyLabel();
				}
			} else {
				name = getWorkingCopyLabel();
			}
		}

		return name;
	}

	public String getWorkingCopyLabel() {
		return MSG_WORKING_COPY_LABEL;
	}

	private String parseFilePath(String filepath) {
		if (!StringUtils.hasText(filepath)) {
			return null;
		}

		filepath = filepath.startsWith("/") ? filepath.substring(1) : filepath;
		filepath = filepath.endsWith("/") ? filepath.substring(0,
				filepath.length() - 1) : filepath;

		filepath = filepath.replace(":", "-");
		// filepath = filepath.replace("/", "-");

		return filepath;
	}

	private String getMimetype(final String extension) {
		String mimetype;

		if (!StringUtils.hasText(extension)) {
			mimetype = "application/octet-stream";
		} else if (extension.equalsIgnoreCase("pdf")) {
			mimetype = "application/pdf";
		} else if (extension.equalsIgnoreCase("ppt")) {
			mimetype = "application/vnd.ms-powerpoint";
		} else if (extension.equalsIgnoreCase("doc")) {
			mimetype = "application/msword";
		} else if (extension.equalsIgnoreCase("vsd")) {
			mimetype = "application/visio";
		} else if (extension.equalsIgnoreCase("bmp")) {
			mimetype = "image/bmp";
		} else if (extension.equalsIgnoreCase("htm")) {
			mimetype = "text/html";
		} else if (extension.equalsIgnoreCase("html")) {
			mimetype = "text/html";
		} else if (extension.equalsIgnoreCase("dot")) {
			mimetype = "application/msword";
		} else if (extension.equalsIgnoreCase("xls")) {
			mimetype = "application/vnd.ms-excel";
		} else if (extension.equalsIgnoreCase("jpg")) {
			mimetype = "image/jpeg";
		} else if (extension.equalsIgnoreCase("pot")) {
			mimetype = "application/vnd.ms-powerpoint";
		} else if (extension.equalsIgnoreCase("rtf")) {
			mimetype = "application/rtf";
		} else if (extension.equalsIgnoreCase("tiff")) {
			mimetype = "image/tiff";
		} else if (extension.equalsIgnoreCase("eps")) {
			mimetype = "application/eps";
		} else if (extension.equalsIgnoreCase("zip")) {
			mimetype = "application/zip";
		} else if (extension.equalsIgnoreCase("txt")) {
			mimetype = "text/plain";
		} else if (extension.equalsIgnoreCase("odt")) {
			mimetype = "application/vnd.oasis.opendocument.text";
		} else if (extension.equalsIgnoreCase("ods")) {
			mimetype = "application/vnd.oasis.opendocument.spreadsheet";
		} else if (extension.equalsIgnoreCase("odp")) {
			mimetype = "application/vnd.oasis.opendocument.presentation";
		} else if (extension.equalsIgnoreCase("tif")) {
			mimetype = "image/tiff";
		} else if (extension.equalsIgnoreCase("tiff")) {
			mimetype = "image/tiff";
		} else if (extension.equalsIgnoreCase("pcx")) {
			mimetype = "image/pcx";
		}

		else {
			throw new RuntimeException("The extension '" + extension
					+ "' has no configured mimetype.");
		}

		return mimetype;
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

		// TODO what is this?
		if (value.toString().startsWith("LNID:= ")) {
			value = StringUtils.replace(value.toString(), "LNID:= ", "");
		}

		// trim all trailing spaces for strings
		if (value instanceof String) {
			value = StringUtils.trimTrailingWhitespace(value.toString());
		}

		properties.put(key, value);
	}

	private SiteInfo findSite(final String siteId) {
		if (!StringUtils.hasText(siteId)) {
			return null;
		}

		return siteService.getSite(siteId);
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		MigrationUtil.fileFolderService = fileFolderService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		MigrationUtil.siteService = siteService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		MigrationUtil.nodeService = nodeService;
	}

	public CheckOutCheckInService getCheckOutCheckInService() {
		return checkOutCheckInService;
	}

	public void setCheckOutCheckInService(
			CheckOutCheckInService checkOutCheckInService) {
		MigrationUtil.checkOutCheckInService = checkOutCheckInService;
	}

	public VersionService getVersionService() {
		return versionService;
	}

	public void setVersionService(VersionService versionService) {
		MigrationUtil.versionService = versionService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		MigrationUtil.contentService = contentService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		MigrationUtil.searchService = searchService;
	}

	public BehaviourFilter getBehaviourFilter() {
		return behaviourFilter;
	}

	public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
		MigrationUtil.behaviourFilter = behaviourFilter;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		MigrationUtil.serviceRegistry = serviceRegistry;
	}
}
