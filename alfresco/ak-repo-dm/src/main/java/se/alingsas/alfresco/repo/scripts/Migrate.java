package se.alingsas.alfresco.repo.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.scripts.ddmigration.AlingsasDocument;
import se.alingsas.alfresco.repo.scripts.ddmigration.MigrationCollection;
import se.alingsas.alfresco.repo.scripts.ddmigration.MigrationUtil;

public class Migrate extends DeclarativeWebScript implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(Migrate.class);

	private static final String ARCHIVE_HMF = "hmf";
	private static final String ARCHIVE_POLITISKA_NAMNDER = "pn";
	private static final String ARCHIVE_PV_FBD = "pvfbd";
	private static final String ARCHIVE_REGIONSERVICE = "rs";
	private static final String ARCHIVE_RK = "rk";
	private static final String ARCHIVE_SOCIALDEMOKRATERNA = "sd";

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

		if (ARCHIVE_HMF.equals(archive)) {
			textfile += "HMF Arbetsdokument/HMF Arbetsdokument Metadata.txt";
			folder += "HMF Arbetsdokument/Files";
		} else if (ARCHIVE_POLITISKA_NAMNDER.equals(archive)) {
			textfile += "Politiska namnder och styrelser Arbetsdokument/Politiska namnder och styrelser Arbetsdokument Metadata.txt";
			folder += "Politiska namnder och styrelser Arbetsdokument/Files";
		} else if (ARCHIVE_PV_FBD.equals(archive)) {
			textfile += "PV FBD Arbetsdokument/PV FBD Arbetsdokument Metadata.txt";
			folder += "PV FBD Arbetsdokument/Files";
		} else if (ARCHIVE_REGIONSERVICE.equals(archive)) {
			textfile += "Regionservice Arbetsdokument/Regionservice Arbetsdokument Metadata.txt";
			folder += "Regionservice Arbetsdokument/Files";
		} else if (ARCHIVE_RK.equals(archive)) {
			textfile += "RK Arbetsdokument/RK Arbetsdokument Metadata.txt";
			folder += "RK Arbetsdokument/Files";
		} else if (ARCHIVE_SOCIALDEMOKRATERNA.equals(archive)) {
			textfile += "Socialdemokraterna Arbetsdokument/Socialdemokraterna Arbetsdokument Metadata.txt";
			folder += "Socialdemokraterna Arbetsdokument/Files";
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
		MigrationCollection migratedDocuments = mu.run(new File(textfile), folder, siteId);

		model.put("migrated", migratedDocuments.noOfDocuments());

		return model;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(_baseFolder);
	}
	//
	// private void commentMigratedDocuments(final String textfile,
	// final Set<AlingsasDocument> migrated) {
	// InputStream inputStream = null;
	// LineIterator iterator = null;
	//
	// OutputStream outputStream = null;
	// Writer osw = null;
	// BufferedWriter writer = null;
	//
	// int lineNumber = 0;
	//
	// try {
	// final String commentedTextfile = FilenameUtils
	// .removeExtension(textfile)
	// + "-backup"
	// + "."
	// + FilenameUtils.getExtension(textfile);
	//
	// inputStream = new FileInputStream(textfile);
	// iterator = IOUtils.lineIterator(inputStream, "UTF-8");
	//
	// outputStream = new FileOutputStream(commentedTextfile);
	// osw = new OutputStreamWriter(outputStream, "UTF-8");
	// writer = new BufferedWriter(osw);
	//
	// while (iterator.hasNext()) {
	// final String line = iterator.nextLine();
	//
	// lineNumber++;
	//
	// if (!StringUtils.hasText(line)) {
	// continue;
	// }
	//
	// if (hasLineNumber(migrated, lineNumber)) {
	// // line = "#" + line;
	// continue;
	// }
	//
	// writer.write(line);
	// writer.newLine();
	// writer.flush();
	// }
	//
	// FileUtils.deleteQuietly(new File(textfile));
	//
	// FileUtils.moveFile(new File(commentedTextfile), new File(textfile));
	// } catch (final Exception ex) {
	// throw new RuntimeException(ex);
	// } finally {
	// IOUtils.closeQuietly(inputStream);
	// LineIterator.closeQuietly(iterator);
	//
	// IOUtils.closeQuietly(outputStream);
	// IOUtils.closeQuietly(osw);
	// IOUtils.closeQuietly(writer);
	// }
	// }
	//
	// private boolean hasLineNumber(final Set<AlingsasDocument> migrated,
	// final int lineNumber) {
	// for (final AlingsasDocument document : migrated) {
	// if (document.lineNumber == lineNumber) {
	// return true;
	// }
	// }
	//
	// return false;
	// }
	//
	// private SiteInfo findSite(final String siteId) {
	// if (!StringUtils.hasText(siteId)) {
	// return null;
	// }
	//
	// return _siteService.getSite(siteId);
	// }
	//
	// /**
	// * Migrates a specific archive.
	// *
	// * @param textfile
	// * The file to take the metadata from.
	// * @param folder
	// * The folder to take the files from.
	// * @param limit
	// * The limit to migrate, 0 for all.
	// * @param site
	// * The site to migrate to.
	// * @return The number of migrated documents.
	// */
	// private Set<AlingsasDocument> migrateArchive(final String archive,
	// final String textfile, final String folder, final int limit,
	// final SiteInfo site) {
	// final Set<AlingsasDocument> migrated = new HashSet<AlingsasDocument>();
	//
	// if (!StringUtils.hasText(textfile) || !StringUtils.hasText(folder)) {
	// return migrated;
	// }
	//
	// final Map<String, Set<AlingsasDocument>> documents = createDocumentList(
	// textfile, folder);
	//
	// // for HMF, there's a lot that don't need to be migrated... do that step
	// // here. Add all those being removed to the migrated list, so that they
	// // will
	// // be removed from the text file later on.
	// if (ARCHIVE_HMF.equals(archive)) {
	// migrated.addAll(cleanHMFList(documents));
	// }
	//
	// // this is essential, there can be duplicates of a name in a folder...
	// // document id and version is unique in VGR dok, not the name
	// validateNames(documents);
	//
	// for (final String documentId : documents.keySet()) {
	// final Set<AlingsasDocument> versions = documents.get(documentId);
	//
	// try {
	// migrated.addAll(migrateVersions(versions, site, folder));
	// } catch (final Exception ex) {
	// LOG.error(ex.getMessage());
	//
	// continue;
	// }
	//
	// logInfo("Migrated '" + documentId + "'");
	//
	// // if the limit is reached, exit
	// if (limit > 0 && migrated.size() >= limit) {
	// break;
	// }
	// }
	//
	// return migrated;
	// }
	//
	// /**
	// * Method for removing all but the 10 last versions of a couple of
	// * documents:
	// *
	// * - DDMM7NGDKS - DDMM7HHFRD - DDMM7BSCTD - DDMM7BRJBH - DDMM7A4E59
	// *
	// * @param documents
	// */
	// private Set<AlingsasDocument> cleanHMFList(
	// final Map<String, Set<AlingsasDocument>> documents) {
	// // create two lists, one for the ones removed and one for the ones which
	// // is
	// // going to be left for migration.
	// final Set<AlingsasDocument> remove = new HashSet<AlingsasDocument>();
	//
	// // iterate through all the documents
	// for (final String documentId : documents.keySet()) {
	// // if the document ID is not one of these 5, continue
	// if (documentId.equals("DDMM7NGDKS")
	// || documentId.equals("DDMM7HHFRD")
	// || documentId.equals("DDMM7BSCTD")
	// || documentId.equals("DDMM7BRJBH")
	// || documentId.equals("DDMM7A4E59")) {
	// // get all the versions for the document ID
	// final Set<AlingsasDocument> versions = documents
	// .get(documentId);
	//
	// final Set<AlingsasDocument> keep = new TreeSet<AlingsasDocument>(
	// new VgrDokDocumentComparator());
	//
	// int count = 0;
	//
	// // iterate through all the versions and keep just the 10
	// for (final AlingsasDocument version : versions) {
	// if (count < versions.size() - 10) {
	// remove.add(version);
	// } else {
	// keep.add(version);
	// }
	//
	// count++;
	// }
	//
	// documents.put(documentId, keep);
	//
	// logInfo("Keep: " + keep.size());
	// }
	// }
	//
	// logInfo("Remove: " + remove.size());
	//
	// return remove;
	// }
	//
	// /**
	// * Validates the names for all the documents. In Alfresco a cm:name must
	// be
	// * unique within a folder. This makes sure that that is the case before
	// the
	// * actual migration process starts.
	// *
	// * @param documents
	// * The document list to check the names for.
	// */
	// private void validateNames(
	// final Map<String, Set<AlingsasDocument>> documents) {
	// final Map<String, Set<String>> list = new HashMap<String, Set<String>>();
	//
	// for (final String documentId : documents.keySet()) {
	// final Set<AlingsasDocument> versions = documents.get(documentId);
	//
	// final AlingsasDocument lastVersion = getLastVersion(versions);
	//
	// Set<String> names;
	//
	// if (list.containsKey(lastVersion.filePath)) {
	// names = list.get(lastVersion.filePath);
	// } else {
	// names = new HashSet<String>();
	// list.put(lastVersion.filePath, names);
	// }
	//
	// if (hasName(names, lastVersion.fileName)) {
	// final String oldName = lastVersion.fileName;
	//
	// final String newName = getValidNewName(names, oldName);
	//
	// logInfo("Duplicate name in folder found, renaming...");
	// logInfo(lastVersion.documentNumber + " - " + lastVersion.version);
	// logInfo(oldName + " -> " + newName);
	// logInfo("");
	//
	// for (final AlingsasDocument version : versions) {
	// version.fileName = newName;
	// }
	// }
	//
	// names.add(lastVersion.fileName);
	// }
	// }
	//
	// private boolean hasName(final Set<String> names, final String name2) {
	// for (final String name1 : names) {
	// if (name1.equalsIgnoreCase(name2)) {
	// return true;
	// }
	// }
	//
	// return false;
	// }
	//
	// private String getValidNewName(final Set<String> names, final String
	// oldName) {
	// String newName = oldName;
	//
	// int count = 2;
	//
	// while (hasName(names, newName)) {
	// final String stem = FilenameUtils.removeExtension(oldName);
	// final String extension = FilenameUtils.getExtension(oldName);
	//
	// newName = stem + " (" + count + ")";
	//
	// if (StringUtils.hasText(extension)) {
	// newName += "." + extension;
	// }
	//
	// count++;
	// }
	//
	// return newName;
	// }
	//
	// /**
	// * Takes a list of documents and returns the one with the highest version.
	// *
	// * @param versions
	// * The Set representing the versions.
	// * @return The highest version.
	// */
	// private AlingsasDocument getLastVersion(final Set<AlingsasDocument>
	// versions) {
	// AlingsasDocument lastVersion = null;
	//
	// //TODO Do we need to care about minor versions as well?
	// for (final AlingsasDocument version : versions) {
	// if (lastVersion==null) {
	// lastVersion = version;
	// }
	// else if
	// (extractMajorVersion(version.version)>extractMajorVersion(lastVersion.version))
	// {//Todo
	// lastVersion = version;
	// }
	// }
	//
	// return lastVersion;
	// }
	//
	// /**
	// * Migrates all the versions.
	// *
	// * @param versions
	// * The versions to migrate.
	// * @param site
	// * The site to migrate to.
	// * @param folder
	// * The folder to look for files in.
	// * @return The number of migrated documents.
	// */
	// private Set<AlingsasDocument> migrateVersions(
	// final Set<AlingsasDocument> versions, final SiteInfo site,
	// final String folder) {
	// final Set<AlingsasDocument> migrated = new HashSet<AlingsasDocument>();
	//
	// for (final AlingsasDocument document : versions) {
	// try {
	// migrated.add(migrateVersion(document, site));
	// } catch (final Exception ex) {
	// LOG.error("Could not migrate document on line "
	// + document.lineNumber);
	// }
	// }
	//
	// return migrated;
	// }
	//
	// private AlingsasDocument migrateVersion(final AlingsasDocument document,
	// final SiteInfo site) {
	// final NodeRef folderNodeRef = createFolder(document.filePath, site);
	//
	// NodeRef baseVersion = findBaseVersion(document);
	//
	// // if no file found, it's the first version
	// if (baseVersion == null) {
	// // create the new file
	// final FileInfo fileInfo = _fileFolderService.create(folderNodeRef,
	// document.fileName, document.documentTypeQName);
	//
	// baseVersion = fileInfo.getNodeRef();
	// documentBaseVersions.put(document.documentNumber, baseVersion);
	// // add the properties to the new file
	// addProperties(baseVersion, document, false);
	//
	// // add the file
	// addFile(baseVersion, document);
	//
	// // create the version history
	// createVersionHistory(baseVersion);
	//
	// logInfo("Migrated version '" + document.version + "'");
	//
	// return document;
	// }
	//
	// if (hasVersion(baseVersion, document)) {
	// logInfo("Migrated version '" + document.version + "'");
	//
	// return document;
	// }
	//
	// // adjust the versioning if needed
	// baseVersion = adjustMajorVersioning(baseVersion, document);
	//
	// final NodeRef workingCopy = _checkOutCheckInService
	// .checkout(baseVersion);
	//
	// addProperties(workingCopy, document, true);
	//
	// addFile(workingCopy, document);
	//
	// final String madeBy = document.createdBy;
	//
	// NodeRef checkinVersion = checkinVersion(workingCopy,
	// isMajorVersion(document) ? VersionType.MAJOR
	// : VersionType.MINOR, madeBy);
	//
	// //Update list of checked in versions
	// documentBaseVersions.put(document.documentNumber, checkinVersion);
	//
	// logInfo("Migrated version '" + document.version + "'");
	//
	// return document;
	// }
	//
	// /**
	// * Finds the base version for a document.
	// *
	// * @param document
	// * The document to base the search on.
	// * @return The NodeRef found or null of none is found.
	// * @throws Throws
	// * an exception if more than one document is found.
	// */
	// private NodeRef findBaseVersion(final AlingsasDocument document) {
	// /*
	// * Search will not work since we use SOLR, search results may be lagged
	// *
	// *
	// * final String query =
	// "ASPECT:\"vgr:vgrdok\" AND @vgr:vgr_dok_document_id:\""
	// + document.documentId + "\" AND -ASPECT:\"vgr:published\"";
	//
	// final SearchParameters searchParameters = new SearchParameters();
	// searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
	// searchParameters.setQuery(query);
	// searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
	//
	// final ResultSet result = _searchService.query(searchParameters);*/
	//
	// //Fetch correct file node, perhaps by caching the node refs for previous
	// versions of the document.
	//
	// NodeRef nodeRef = documentBaseVersions.get(document.documentNumber);
	//
	// return nodeRef;
	// }
	//
	// /**
	// * Adds a file to the nodeRef based on the document.
	// *
	// * @param nodeRef
	// * The nodeRef to add the document for.
	// * @param document
	// * The document to read the file info from.
	// */
	// private void addFile(final NodeRef nodeRef, final AlingsasDocument
	// document) {
	// if (_onlyMetadata) {
	// return;
	// }
	//
	// InputStream inputStream = null;
	//
	// try {
	// inputStream = new FileInputStream(document.file);
	//
	// final ContentWriter writer = _contentService.getWriter(nodeRef,
	// ContentModel.PROP_CONTENT, true);
	//
	// writer.setMimetype(document.mimetype);
	//
	// writer.putContent(inputStream);
	// } catch (final Exception ex) {
	// IOUtils.closeQuietly(inputStream);
	//
	// throw new RuntimeException(ex);
	// }
	// }
	//
	// /**
	// * Checks in the working copy, sets the modifier and creator to madeBy.
	// *
	// * @param workingCopy
	// * The working copy to check in.
	// * @param versionType
	// * Major or minor version.
	// * @param madeBy
	// * Made by is used for modifier and creator.
	// * @return The nodeRef of the revisioned node.
	// */
	// private NodeRef checkinVersion(final NodeRef workingCopy,
	// final VersionType versionType, final String madeBy) {
	// final Map<String, Serializable> properties = new HashMap<String,
	// Serializable>();
	// properties.put(VersionModel.PROP_VERSION_TYPE, versionType);
	//
	// if (_nodeService.getProperty(workingCopy, ContentModel.PROP_MODIFIER) ==
	// null) {
	// _nodeService.setProperty(workingCopy, ContentModel.PROP_MODIFIER,
	// madeBy);
	// }
	//
	// if (_nodeService.getProperty(workingCopy, ContentModel.PROP_CREATOR) ==
	// null) {
	// _nodeService.setProperty(workingCopy, ContentModel.PROP_CREATOR,
	// madeBy);
	// }
	//
	// return _checkOutCheckInService.checkin(workingCopy, properties);
	// }
	//
	// /**
	// * Fills any gap in the version history, only fills the major version
	// gaps.
	// *
	// * @param baseVersion
	// * The base version to check against.
	// * @param document
	// * The document representing the next version to check in.
	// * @return The nodeRef of the adjusted version history.
	// */
	// private NodeRef adjustMajorVersioning(final NodeRef baseVersion,
	// final AlingsasDocument document) {
	// final int currentMajorVersion = extractMajorVersion(baseVersion);
	// final int nextMajorVersion = extractMajorVersion(document.version);
	//
	// int difference = nextMajorVersion - currentMajorVersion;
	//
	// NodeRef newVersionNodeRef = null;
	//
	// while (difference > 1) {
	// final NodeRef workingCopy = _checkOutCheckInService
	// .checkout(baseVersion);
	//
	// final String madeBy = getMadeBy(document.uploadedBy,
	// document.createdBy);
	//
	// newVersionNodeRef = checkinVersion(workingCopy, VersionType.MAJOR,
	// madeBy);
	//
	// logInfo("Adjust versioning...");
	//
	// difference = nextMajorVersion
	// - extractMajorVersion(newVersionNodeRef,
	// ContentModel.PROP_VERSION_LABEL);
	// }
	//
	// return newVersionNodeRef != null ? newVersionNodeRef : baseVersion;
	// }
	//
	// /**
	// * Extracts the x part of a x.y version label.
	// *
	// * @param version
	// * The string representation of the version.
	// * @return The extraced major version as an int.
	// */
	// private int extractMajorVersion(final String version) {
	// final String[] parts = StringUtils.split(version, ".");
	//
	// return Integer.parseInt(parts[0]);
	// }
	//
	// /**
	// * Extracts the "x" part of a "x.y" version label.
	// *
	// * @param nodeRef
	// * The nodeRef to extract the version from
	// * @param versionProperty
	// * The property that contains the version.
	// * @return The extraced major version as an int.
	// */
	// private int extractMajorVersion(final NodeRef nodeRef,
	// final QName versionProperty) {
	// final Serializable version = _nodeService.getProperty(nodeRef,
	// versionProperty);
	//
	// return extractMajorVersion(version.toString());
	// }
	//
	// private int extractMajorVersion(final NodeRef nodeRef) {
	// return extractMajorVersion(nodeRef, VgrModel.PROP_VGR_DOK_VERSION);
	// }
	//
	// private void createVersionHistory(final NodeRef baseVersion) {
	// final VersionHistory versionHistory = _versionService
	// .getVersionHistory(baseVersion);
	//
	// if (versionHistory == null) {
	// // check it in again, with supplied version history note
	// final Map<String, Serializable> properties = new HashMap<String,
	// Serializable>();
	// properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
	// VersionModel.PROP_VERSION_NUMBER;
	// _versionService.createVersion(baseVersion, properties);
	// }
	// }
	//
	// private boolean hasVersion(final NodeRef baseVersion,
	// final AlingsasDocument document) {
	// final VersionHistory versionHistory = _versionService
	// .getVersionHistory(baseVersion);
	//
	// if (versionHistory == null) {
	// return false;
	// }
	//
	// final Collection<Version> versions = versionHistory.getAllVersions();
	//
	// for (final Version version : versions) {
	//
	// final String v = version.getVersionProperty("vgr_dok_version")
	// .toString();
	//
	//
	// if (v.equals(document.version)) {
	// return true;
	// }
	// }
	//
	// return false;
	// }
	//
	// private void addProperties(final NodeRef nodeRef,
	// final AlingsasDocument document, final boolean workingCopy) {
	// final Map<QName, Serializable> properties = new HashMap<QName,
	// Serializable>();
	//
	// if (workingCopy) {
	// final String name = _nodeService.getProperty(nodeRef,
	// ContentModel.PROP_NAME).toString();
	//
	// final String workingCopyName = createWorkingCopyName(document.fileName);
	//
	// if (!name.equalsIgnoreCase(workingCopyName)) {
	// addProperty(properties, ContentModel.PROP_NAME, document.fileName);
	// }
	// } else {
	// addProperty(properties, ContentModel.PROP_NAME, document.fileName);
	// }
	//
	// // final String checksum = _serviceUtils.getChecksum(document.file);
	//
	// addProperty(properties, ContentModel.PROP_AUTO_VERSION_PROPS, true);
	// addProperty(properties, ContentModel.PROP_AUTO_VERSION, true);
	// addProperty(properties, ContentModel.PROP_TITLE, document.title);
	// addProperty(properties, ContentModel.PROP_CREATOR,
	// getMadeBy(document.createdBy, document.uploadedBy));
	// addProperty(properties, ContentModel.PROP_MODIFIER,
	// getMadeBy(document.uploadedBy, document.createdBy));
	// addProperty(properties, ContentModel.PROP_MODIFIED,
	// document.createdDate);
	// addProperty(properties, ContentModel.PROP_CREATED, document.createdDate);
	//
	// addProperty(properties, VgrModel.PROP_TITLE, document.title);
	// addProperty(properties, VgrModel.PROP_TITLE_FILENAME, document.filename);
	// addProperty(properties, VgrModel.PROP_SOURCE, document.documentId);
	// addProperty(properties, VgrModel.PROP_CONTRIBUTOR_SAVEDBY,
	// getMadeBy(document.uploadedBy, document.createdBy));
	// addProperty(properties, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID,
	// getMadeBy(document.uploadedBy, document.createdBy));
	// addProperty(properties, VgrModel.PROP_DATE_SAVED, document.createdDate);
	// addProperty(properties, VgrModel.PROP_DATE_AVAILABLE_FROM,
	// document.availableDateFrom);
	// addProperty(properties, VgrModel.PROP_DATE_AVAILABLE_TO,
	// document.availableDateTo);
	// addProperty(properties, VgrModel.PROP_DESCRIPTION, document.description);
	// addProperty(properties, VgrModel.PROP_CREATOR, document.author);
	// addProperty(properties, VgrModel.PROP_CREATOR_ID, document.author);
	// addProperty(properties, VgrModel.PROP_TYPE_DOCUMENT,
	// getDocumentType(document.documentType));
	// addProperty(properties, VgrModel.PROP_TYPE_RECORD, DEFAULT_TYPE_RECORD);
	// addProperty(properties, VgrModel.PROP_STATUS_DOCUMENT,
	// DEFAULT_STATUS_DOCUMENT);
	// addProperty(properties, VgrModel.PROP_CREATOR_PROJECT_ASSIGNMENT,
	// document.responsibleProject);
	// addProperty(properties, VgrModel.PROP_TYPE_DOCUMENT_SERIE,
	// document.documentSeries);
	// addProperty(properties, VgrModel.PROP_TYPE_DOCUMENT_ID,
	// document.externalReference);
	// addProperty(properties, VgrModel.PROP_ACCESS_RIGHT,
	// getAccessRights(document.accessZone));
	// addProperty(properties, VgrModel.PROP_PUBLISHER,
	// _serviceUtils.getRepresentation(document.publishedBy));
	// addProperty(properties, VgrModel.PROP_PUBLISHER_ID,
	// document.publishedBy);
	// addProperty(properties, VgrModel.PROP_DATE_ISSUED,
	// document.publishedDate);
	// addProperty(properties, VgrModel.PROP_CREATOR_FUNCTION,
	// document.responsiblePerson);
	// addProperty(properties, VgrModel.PROP_CHECKSUM, checksum);
	// addProperty(properties, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT,
	// DEFAULT_PUBLISHER_PROJECT_ASSIGNMENT);
	//
	// if (document.file != null) {
	// addProperty(properties, VgrModel.PROP_FORMAT_EXTENT_EXTENSION,
	// FilenameUtils.getExtension(document.file.getName()));
	// }
	//
	// addProperty(properties, VgrModel.PROP_COVERAGE_HSACODE,
	// getHsaCode(document.businessAreaMain, document.businessAreaSub));
	// addProperty(properties, VgrModel.PROP_VGR_DOK_UNIVERSAL_ID,
	// document.universalID);
	// addProperty(properties, VgrModel.PROP_VGR_DOK_FILEPATH,
	// document.filepath);
	// // below is to make the versioning work correctly
	// addProperty(properties, VgrModel.PROP_VGR_DOK_VERSION, document.version);
	// addProperty(properties, VgrModel.PROP_VGR_DOK_DOCUMENT_ID,
	// document.documentId);
	// addProperty(properties, VgrModel.PROP_VGR_DOK_DOCUMENT_STATUS,
	// document.documentStatus);
	// addProperty(properties, VgrModel.PROP_VGR_DOK_PUBLISH_TYPE,
	// document.publishType);
	//
	// _nodeService.addProperties(nodeRef, properties);
	// }
	//
	// private String createWorkingCopyName(String name) {
	// if (this.getWorkingCopyLabel() != null
	// && this.getWorkingCopyLabel().length() != 0) {
	// if (name != null && name.length() != 0) {
	// final int index = name.lastIndexOf(EXTENSION_CHARACTER);
	// if (index > 0) {
	// // Insert the working copy label before the file extension
	// name = name.substring(0, index) + " "
	// + getWorkingCopyLabel() + name.substring(index);
	// } else {
	// // Simply append the working copy label onto the end of the
	// // existing
	// // name
	// name = name + " " + getWorkingCopyLabel();
	// }
	// } else {
	// name = getWorkingCopyLabel();
	// }
	// }
	//
	// return name;
	// }
	//
	// public String getWorkingCopyLabel() {
	// return I18NUtil.getMessage(MSG_WORKING_COPY_LABEL);
	// }
	//
	// private void addProperty(final Map<QName, Serializable> properties,
	// final QName key, Serializable value) {
	// // if no text, just exit
	// if (value == null) {
	// return;
	// }
	//
	// // if no text, just exit
	// if (!StringUtils.hasText(value.toString())) {
	// return;
	// }
	//
	// if (value.toString().startsWith("LNID:= ")) {
	// value = StringUtils.replace(value.toString(), "LNID:= ", "");
	// }
	//
	// // trim all trailing spaces for strings
	// if (value instanceof String) {
	// value = StringUtils.trimTrailingWhitespace(value.toString());
	// }
	//
	// properties.put(key, value);
	// }
	//
	// private String getMadeBy(final String first, final String second) {
	// String username;
	//
	// if (StringUtils.hasText(first)) {
	// username = first;
	// } else if (StringUtils.hasText(second)) {
	// username = second;
	// } else {
	// username = "admin";
	// }
	//
	// return _serviceUtils.getRepresentation(username);
	// }
	//
	// private NodeRef createFolder(final String filepath, final SiteInfo site)
	// {
	// NodeRef rootNodeRef = _fileFolderService.searchSimple(
	// site.getNodeRef(), "documentLibrary");
	//
	// final String[] parts = StringUtils.delimitedListToStringArray(filepath,
	// "\\");
	//
	// for (String part : parts) {
	// part = StringUtils.trimWhitespace(part);
	//
	// NodeRef folder = _fileFolderService.searchSimple(rootNodeRef, part);
	//
	// if (folder == null) {
	// folder = _fileFolderService.create(rootNodeRef, part,
	// ContentModel.TYPE_FOLDER).getNodeRef();
	// }
	//
	// rootNodeRef = folder;
	// }
	//
	// return rootNodeRef;
	// }
	//
	// private Map<String, Set<AlingsasDocument>> createDocumentList(
	// final String textfile, final String folder) {
	// InputStream inputStream = null;
	// LineIterator lineIterator = null;
	// int lineNumber = 0;
	//
	// try {
	// inputStream = new FileInputStream(textfile);
	//
	// lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
	//
	// final Map<String, Set<AlingsasDocument>> documents = new HashMap<String,
	// Set<AlingsasDocument>>();
	//
	// while (lineIterator.hasNext()) {
	// lineNumber++;
	//
	// final String line = lineIterator.nextLine();
	//
	// // if it's an empty line or a comment, skip
	// if (!StringUtils.hasText(line) || line.startsWith("#")) {
	// continue;
	// }
	//
	// final String[] parts = StringUtils.delimitedListToStringArray(
	// line, "#!#");
	//
	// if (parts.length < 1) {
	// continue;
	// }
	//
	// final AlingsasDocument document = createDocument(parts);
	//
	// if (document == null) {
	// LOG.error("Document found on line number '" + lineNumber
	// + "' has no document ID, skipping...");
	//
	// continue;
	// }
	//
	// document.lineNumber = lineNumber;
	//
	// populateFile(document, folder);
	//
	// Set<AlingsasDocument> versions;
	//
	// if (documents.containsKey(document.documentId)) {
	// versions = documents.get(document.documentId);
	// } else {
	// versions = new TreeSet<AlingsasDocument>(
	// new VgrDokDocumentComparator());
	// documents.put(document.documentId, versions);
	// }
	//
	// if (versions.contains(document)) {
	// LOG.error("Duplicate document found on line number '"
	// + lineNumber + "': " + document);
	//
	// continue;
	// }
	//
	// versions.add(document);
	// }
	//
	// return documents;
	// } catch (final Exception ex) {
	// throw new RuntimeException("Error on line '" + lineNumber + "'", ex);
	// } finally {
	// IOUtils.closeQuietly(inputStream);
	// LineIterator.closeQuietly(lineIterator);
	// }
	// }
	//
	// private void populateFile(final AlingsasDocument document,
	// final String folder) {
	// String ext = FilenameUtils.getExtension(document.filename);
	//
	// if (StringUtils.hasText(ext)) {
	// ext = "." + ext;
	// }
	//
	// final File file = new File(folder + File.separator
	// + document.universalID + ext);
	//
	// if (!file.exists()) {
	// LOG.warn("Could not find " + file.getAbsolutePath() + " in folder "
	// + folder + " (" + document.filename + ")");
	//
	// return;
	// }
	//
	// document.file = file;
	// document.mimetype = getMimetype(FilenameUtils.getExtension(file
	// .getName()));
	// // document.checksum = getChecksum(file);
	// }
	//
	// private AlingsasDocument createDocument(final String[] parts) {
	// final AlingsasDocument document = new AlingsasDocument();
	//
	// int position = 0;
	//
	// document.title = parts[position];
	// document.documentId = parts[++position];
	// document.uploadedBy = parts[++position];
	// document.createdBy = parts[++position];
	// document.createdDate = parseDate(parts[++position]);
	// document.filename = parts[++position];
	// document.version = parseVersion(parts[++position]);
	// document.documentType = parts[++position];
	// document.documentStatus = parts[++position];
	// document.filepath = parseFilePath(parts[++position]);
	// document.publishedBy = parts[++position];
	// document.publishedDate = parseDate(parts[++position]);
	// document.description = parts[++position];
	// document.publishType = parts[++position];
	// document.author = parts[++position];
	// document.businessAreaMain = parts[++position];
	// document.businessAreaSub = parts[++position];
	// document.responsibleProject = parts[++position];
	// document.responsiblePerson = parts[++position];
	// document.documentSeries = parts[++position];
	// document.externalReference = parts[++position];
	// document.availableDateFrom = parseDate(parts[++position]);
	// document.availableDateTo = parseDate(parts[++position]);
	// document.accessZone = parts[++position];
	// document.universalID = parts[++position];
	//
	// // special title...
	// document.title = extractTitle(document);
	// // and special name
	// document.name = extractName(document, 1);
	//
	// if (!StringUtils.hasText(document.documentId)) {
	// return null;
	// }
	//
	// return document;
	// }
	//
	// private Date parseDate(final String date) {
	// if (!StringUtils.hasText(date)) {
	// return null;
	// }
	//
	// try {
	// final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd HH:mm:ss");
	//
	// return simpleDateFormat.parse(date);
	// } catch (final ParseException ex) {
	// final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd");
	//
	// try {
	// return simpleDateFormat.parse(date);
	// } catch (final ParseException ex1) {
	// throw new RuntimeException(ex1);
	// }
	// }
	// }
	//
	// private String parseVersion(final String value) {
	// if (value.equals("1.")) {
	// logInfo("Wrong version '" + value
	// + "', replaced with version '1.0'");
	//
	// return "1.0";
	// }
	//
	// try {
	// Float.parseFloat(value);
	//
	// return value;
	// } catch (final Exception ex) {
	// logInfo("Wrong version '" + value
	// + "', replaced with version '0.1'");
	//
	// return "0.1";
	// }
	// }
	//
	// private String parseFilePath(String filepath) {
	// if (!StringUtils.hasText(filepath)) {
	// return null;
	// }
	//
	// filepath = filepath.startsWith("\\") ? filepath.substring(1) : filepath;
	// filepath = filepath.endsWith("\\") ? filepath.substring(0,
	// filepath.length() - 1) : filepath;
	//
	// filepath = filepath.replace(":", "-");
	// filepath = filepath.replace("/", "-");
	//
	// return filepath;
	// }
	//
	// private String extractTitle(final AlingsasDocument document) {
	// String title = document.title;
	//
	// // get the extension from the filename
	// final String extension = FilenameUtils.getExtension(document.fileName);
	//
	// // if the file has an extension, remove that form the title if the title
	// // ends with it
	// if (StringUtils.hasText(extension) && title.endsWith(extension)) {
	// title = StringUtils.replace(title, extension, "");
	// }
	//
	// // if the title ends with a . remove it
	// if (title.endsWith(".")) {
	// title = title.substring(0, title.length() - 1);
	// }
	//
	// return title;
	// }
	//
	// private String extractName(final AlingsasDocument document, final int
	// count) {
	// String name = "";
	//
	// name += document.title.replace("\\", "-");
	// name = name.replace(":", "-");
	// name = name.replace("?", "");
	// name = name.replace("/", "-");
	// name = name.replace("\"", "\'");
	//
	// if (count > 1) {
	// name += " (" + count + ")";
	// }
	//
	// final String extension = FilenameUtils.getExtension(document.fileName);
	//
	// if (StringUtils.hasText(extension)) {
	// name += "." + extension;
	// }
	//
	// // if there by some strange chance is no name, then create one
	// if (!StringUtils.hasText(name)) {
	// name = document.documentNumber + " - " + document.version;
	// LOG.warn("No filename found for document "+ document.documentNumber
	// +" generating new name: "+name);
	// }
	//
	// return name;
	// }
	//
	// private String getMimetype(final String extension) {
	// String mimetype;
	//
	// if (!StringUtils.hasText(extension)) {
	// mimetype = "application/octet-stream";
	// } else if (extension.equalsIgnoreCase("pdf")) {
	// mimetype = "application/pdf";
	// } else if (extension.equalsIgnoreCase("ppt")) {
	// mimetype = "application/vnd.ms-powerpoint";
	// } else if (extension.equalsIgnoreCase("doc")) {
	// mimetype = "application/msword";
	// } else if (extension.equalsIgnoreCase("vsd")) {
	// mimetype = "application/visio";
	// } else if (extension.equalsIgnoreCase("bmp")) {
	// mimetype = "image/bmp";
	// } else if (extension.equalsIgnoreCase("htm")) {
	// mimetype = "text/html";
	// } else if (extension.equalsIgnoreCase("html")) {
	// mimetype = "text/html";
	// } else if (extension.equalsIgnoreCase("dot")) {
	// mimetype = "application/msword";
	// } else if (extension.equalsIgnoreCase("xls")) {
	// mimetype = "application/vnd.ms-excel";
	// } else if (extension.equalsIgnoreCase("jpg")) {
	// mimetype = "image/jpeg";
	// } else if (extension.equalsIgnoreCase("pot")) {
	// mimetype = "application/vnd.ms-powerpoint";
	// } else if (extension.equalsIgnoreCase("rtf")) {
	// mimetype = "application/rtf";
	// } else if (extension.equalsIgnoreCase("tiff")) {
	// mimetype = "image/tiff";
	// } else if (extension.equalsIgnoreCase("eps")) {
	// mimetype = "application/eps";
	// } else if (extension.equalsIgnoreCase("zip")) {
	// mimetype = "application/zip";
	// } else {
	// throw new RuntimeException("The extension '" + extension
	// + "' has no configured mimetype.");
	// }
	//
	// return mimetype;
	// }
	//
	// private String getDocumentType(final String documentType) {
	// if (!StringUtils.hasText(documentType)) {
	// return null;
	// }
	//
	// String result;
	//
	// if (documentType.equals("- Välj dokumenttyp -")) {
	// result = null;
	// } else if (documentType.equals("Informationsmaterial")) {
	// result = "Information";
	// } else if (documentType.equals("Mötesanteckning")) {
	// result = "Mötesdokument";
	// } else if (documentType.equals("Nyhetsbrev")) {
	// result = "Information";
	// } else if (documentType.equals("Protokoll")) {
	// result = "Mötesdokument";
	// } else if (documentType.equals("Anbud")) {
	// result = "Anbud/Upphandling";
	// } else if (documentType.equals("Dagordning")) {
	// result = "Mötesdokument";
	// } else if (documentType.equals("Bild")) {
	// result = "Bild";
	// } else if (documentType.equals("Projektdokument")) {
	// result = "Projektdokument";
	// } else if (documentType.equals("Tjänsteutlåtande")) {
	// result = "Tjänsteutlåtande";
	// } else if (documentType.equals("Handlingsprogram")) {
	// result = "Politisk beslutsberedning";
	// } else if (documentType.equals("Rapport")) {
	// result = "Rapport/Redovisning";
	// } else if (documentType.equals("Handbok")) {
	// result = "Beskrivning";
	// } else if (documentType.equals("Policy")) {
	// result = "Regler/Riktlinjer/Rekommendationer";
	// } else if (documentType.equals("Uppdragsdokument")) {
	// result = "Beslutsdokument";
	// } else if (documentType.equals("Förteckning")) {
	// result = "Förteckning";
	// } else if (documentType.equals("Anvisning")) {
	// result = "Regler/Riktlinjer/Rekommendationer";
	// } else if (documentType.equals("Ansökan")) {
	// result = "Ansökan/Anmälan";
	// } else if (documentType.equals("Anmälan")) {
	// result = "Ansökan/Anmälan";
	// } else if (documentType.equals("Lokal rutin")) {
	// result = "\"Lokal rutin\"";
	// } else if (documentType.equals("Avtal")) {
	// result = "Avtal/Överenskommelse";
	// } else if (documentType.equals("Plan")) {
	// result = "Handlingsplan";
	// } else if (documentType.equals("Formulär")) {
	// result = "Förteckning";
	// } else if (documentType.equals("Interpellation")) {
	// result = "Politisk beslutsberedning";
	// } else if (documentType.equals("Meddelande")) {
	// result = "Korrespondens";
	// } else if (documentType.equals("Upphandlingsdokument")) {
	// result = "Anbud/Upphandling";
	// } else if (documentType.equals("Intyg/betyg")) {
	// result = "Intyg";
	// } else if (documentType.equals("Motionssvar")) {
	// result = "Politisk beslutsberedning";
	// } else if (documentType.equals("Kalendarium")) {
	// result = "Mötesdokument";
	// } else if (documentType.equals("Enkät")) {
	// result = "Enkät";
	// } else if (documentType.equals("Kallelse")) {
	// result = "Kallelse/Inbjudan";
	// } else if (documentType.equals("Inbjudan")) {
	// result = "Kallelse/Inbjudan";
	// } else if (documentType.equals("Beställning")) {
	// result = "Beställning";
	// } else if (documentType.equals("Reseräkning")) {
	// result = "Mall";
	// } else {
	// throw new RuntimeException("Document type '" + documentType
	// + "' not recognized!");
	// }
	//
	// return result;
	// }
	//
	// private String getAccessRights(final String accessZone) {
	// if (!StringUtils.hasText(accessZone)) {
	// return null;
	// }
	//
	// String result;
	//
	// if (accessZone.equals("Intranät")) {
	// result = "Intranät";
	// } else if (accessZone.equals("Internet")) {
	// result = "Internet";
	// } else if (accessZone.equals("Extranät")) {
	// result = "Intranät";
	// } else {
	// throw new RuntimeException("The access zone '" + accessZone
	// + "' was not recognized.");
	// }
	//
	// return result;
	// }
	//
	// private String getHsaCode(final String businessAreaMain,
	// final String businessAreaSub) {
	// if (!StringUtils.hasText(businessAreaMain)
	// || StringUtils.hasText(businessAreaSub)) {
	// return "";
	// }
	//
	// final String businessArea = businessAreaMain + "/" + businessAreaSub;
	//
	// String hsa;
	//
	// if ("Administration/Information/kommunikation".equals(businessArea)) {
	// hsa = "";
	// } else if ("Administration/IT /  Informationsteknik"
	// .equals(businessArea)) {
	// hsa = "";
	// } else if ("Administration/Ledningsfrågor".equals(businessArea)) {
	// hsa = "";
	// } else {
	// throw new RuntimeException("Unknown business area '" + businessArea
	// + "'");
	// }
	//
	// return hsa;
	// }
	//
	// private boolean isMajorVersion(final AlingsasDocument document) {
	// final String[] parts = StringUtils.split(document.version, ".");
	//
	// return parts[1].equals("0");
	// }
	//
	// private void logInfo(final String logString) {
	// if (LOG.isInfoEnabled()) {
	// LOG.info(logString);
	// }
	// }
	//

	//
	// class VgrDokDocumentComparator implements Comparator<AlingsasDocument> {
	//
	// @Override
	// public int compare(final AlingsasDocument document1,
	// final AlingsasDocument document2) {
	// final String v1 = document1.version;
	// final String v2 = document2.version;
	//
	// // sort the list descending (ie. most recent first)
	// return new VersionNumber(v1).compareTo(new VersionNumber(v2));
	// }
	//
	// }
	//
	// private class AlingsasDocument {
	//
	// //General properties
	// public String documentNumber;
	// public String createdBy;
	// public Date createdDate;
	// public String fileName;
	// public String title;
	// public String documentType;
	// public String documentStatus;
	// public String description;
	// public String secrecy;
	// public String version;
	//
	// //Anvisning properties
	// public String protocolId;
	// public String decisionDate;
	// public String group;
	//
	// //Författning properties
	// //public String protocolId;
	// //public String decisionDate;
	// //public String group;
	//
	// //Handbok properties
	// public String function;
	// public String handbook;
	//
	// //Protokoll properties
	// //public String protocolId;
	//
	// //Internal migration properties
	// public int lineNumber;
	// public QName documentTypeQName;
	// public File file;
	// public String mimetype;
	// public String filePath;
	// public NodeRef documentNodeRef;
	//
	// /*
	// public String title; // mapped
	// public String documentId; // mapped
	// public String uploadedBy; // mapped
	// public String createdBy; // not mapped - no relevance
	// public Date createdDate; // mapped
	// public String filename; // mapped
	// public String filepath; // mapped
	// public String version; // mapped
	// public String documentType; // mapped
	// public String documentStatus; // not mapped
	// public String publishedBy; // mapped
	// public Date publishedDate; // mapped
	// public String description; // mapped
	// public String publishType; // not mapped
	// public String businessAreaMain; // not mapped
	// public String businessAreaSub; // not mapped
	// public String responsibleProject; // mapped
	// public String responsiblePerson; // mapped
	// public String documentSeries; // mapped
	// public String externalReference; // mapped
	// public Date availableDateFrom; // not mapped
	// public Date availableDateTo; // not mapped
	// public String accessZone; // mapped
	// public String universalID; // mapped
	// public File file; // mapped
	// public String author; // mapped
	// public String mimetype; // mapped
	// public String name;
	// public int lineNumber;
	// */
	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this);
	// }
	//
	// @Override
	// public boolean equals(final Object object) {
	// if (this == object) {
	// return true;
	// }
	//
	// if (!(object instanceof AlingsasDocument)) {
	// return false;
	// }
	//
	// final AlingsasDocument that = (AlingsasDocument) object;
	//
	// final EqualsBuilder builder = new EqualsBuilder();
	//
	// builder.append(this.documentNumber.toLowerCase(),
	// that.documentNumber.toLowerCase());
	// builder.append(this.version, that.version);
	//
	// return builder.isEquals();
	// }
	//
	// @Override
	// public int hashCode() {
	// final HashCodeBuilder builder = new HashCodeBuilder();
	//
	// builder.append(documentNumber.toLowerCase());
	// builder.append(version);
	//
	// return builder.hashCode();
	// }
	//
	// }

}
