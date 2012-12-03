package se.alingsas.alfresco.repo.utils.byggreda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class ByggRedaUtilTest {
	final Mockery context = new Mockery();
	final SiteService siteService = context.mock(SiteService.class);
	final SiteInfo siteInfo = context.mock(SiteInfo.class);
	final FileFolderService fileFolderService = context
			.mock(FileFolderService.class);
	final FileInfo fileInfo = context.mock(FileInfo.class);
	final ContentService contentService = context.mock(ContentService.class);
	final ContentReader contentReader = context.mock(ContentReader.class);
	final NodeService nodeService = context.mock(NodeService.class);
	final ContentWriter contentWriter = context.mock(ContentWriter.class);
	final VersionService versionService = context.mock(VersionService.class);
	
	final StoreRef storeRef = new StoreRef("workspace://SpacesStore");
	final String dummyNodeId = "cafebabe-cafe-babe-cafe-babecafebabe";
	final NodeRef dummyNodeRef = new NodeRef(storeRef, dummyNodeId);

	final String validSourcePath = "src/test/resources/byggreda/source";
	final String invalidSourcePath = "src/test/resources/byggreda/nosource";
	final String validDestinationPath = "src/test/resources/byggreda/destination";
	final String invalidDestinationPath = "src/test/resources/byggreda/nodestination";
	final String validLogsPath = "src/test/resources/byggreda/logs";
	final String invalidLogsPath = "src/test/resources/byggreda/nologs";
	final String validMetaFileName = "metadata.txt";
	final String invalidMetaFileName = "nometadata.txt";
	final String validFileName = "test.pdf";
	List<String> validSourcePathList = new ArrayList<String>();
	List<String> invalidSourcePathList = new ArrayList<String>();
	List<String> validDestinationPathList = new ArrayList<String>();
	List<String> invalidDestinationPathList = new ArrayList<String>();
	List<String> validLogsPathList = new ArrayList<String>();
	List<String> invalidLogsPathList = new ArrayList<String>();
	List<String> validFilePathList = new ArrayList<String>();
	List<String> invalidFilePathList = new ArrayList<String>();
	List<String> validFilePathList2 = new ArrayList<String>();
	List<String> invalidFilePathList2 = new ArrayList<String>();
	List<String> validFilePathList3 = new ArrayList<String>();
	List<String> invalidFilePathList3 = new ArrayList<String>();
	InputStream validInputStream;
	InputStream validInputStream2;
	ContentData contentData;

	@Before
	public void setUp() throws FileNotFoundException,
			java.io.FileNotFoundException {
		// Source
		String[] parts = StringUtils.delimitedListToStringArray(
				validSourcePath, "/");
		for (String part : parts) {
			validSourcePathList.add(part);
		}

		parts = StringUtils.delimitedListToStringArray(invalidSourcePath, "/");
		for (String part : parts) {
			invalidSourcePathList.add(part);
		}
		// Destination
		parts = StringUtils.delimitedListToStringArray(validDestinationPath,
				"/");
		for (String part : parts) {
			validDestinationPathList.add(part);
		}

		parts = StringUtils.delimitedListToStringArray(invalidDestinationPath,
				"/");
		for (String part : parts) {
			invalidDestinationPathList.add(part);
		}
		// Logs
		parts = StringUtils.delimitedListToStringArray(validLogsPath, "/");
		for (String part : parts) {
			validLogsPathList.add(part);
		}

		parts = StringUtils.delimitedListToStringArray(invalidLogsPath, "/");
		for (String part : parts) {
			invalidLogsPathList.add(part);
		}

		// Files
		parts = StringUtils.delimitedListToStringArray(validSourcePath + "/"
				+ validMetaFileName, "/");
		for (String part : parts) {
			validFilePathList.add(part);
		}

		parts = StringUtils.delimitedListToStringArray(validSourcePath + "/"
				+ invalidMetaFileName, "/");
		for (String part : parts) {
			invalidFilePathList.add(part);
		}
		
		parts = StringUtils.delimitedListToStringArray(validSourcePath + "/"
				+ validFileName, "/");
		for (String part : parts) {
			validFilePathList2.add(part);
		}
		
		parts = StringUtils.delimitedListToStringArray(validDestinationPath+"/A/A/"
				+ validFileName, "/");
		for (String part : parts) {
			validFilePathList3.add(part);
		}


		contentData = new ContentData("file", "text/plain", 0, "UTF-8");

		validInputStream = new FileInputStream(validSourcePath + "/"
				+ validMetaFileName);
		context.checking(new Expectations() {
			{
				allowing(siteService).getSite("test");
				will(returnValue(siteInfo));

				allowing(siteService).getSite("fail");
				will(returnValue(null));

				allowing(siteInfo).getNodeRef();
				will(returnValue(dummyNodeRef));

				allowing(fileFolderService).searchSimple(dummyNodeRef,
						SiteService.DOCUMENT_LIBRARY);
				will(returnValue(dummyNodeRef));
				// Source
				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						validSourcePathList, false);
				will(returnValue(fileInfo));

				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						invalidSourcePathList, false);
				will(returnValue(null));
				// Destination
				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						validDestinationPathList, false);
				will(returnValue(fileInfo));

				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						invalidDestinationPathList, false);
				will(returnValue(null));
				// Logs
				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						validLogsPathList, false);
				will(returnValue(fileInfo));

				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						invalidLogsPathList, false);
				will(returnValue(null));

				// Files
				allowing(contentService).getRawReader(
						contentData.getContentUrl());
				will(returnValue(contentReader));

				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						validFilePathList, false);
				will(returnValue(fileInfo));
				allowing(fileFolderService).resolveNamePath(dummyNodeRef,
						invalidFilePathList, false);
				will(returnValue(null));

				allowing(fileInfo).getContentData();
				will(returnValue(contentData));

				allowing(fileInfo).getNodeRef();
				will(returnValue(dummyNodeRef));
				
				allowing(contentReader).getContentInputStream();
				will(returnValue(validInputStream));

				//Import documents
				allowing(fileFolderService).searchSimple(with(equal(dummyNodeRef)),
						with(any(String.class)));
				will(returnValue(dummyNodeRef));

				allowing(fileFolderService).create(with(equal(dummyNodeRef)),
						with(any(String.class)), with(equal(AkDmModel.TYPE_AKDM_BYGGREDA_DOC)));
				will(returnValue(fileInfo));
				
				allowing(fileFolderService).create(with(equal(dummyNodeRef)),
						with(any(String.class)), with(equal(AkDmModel.TYPE_AKDM_DOCUMENT)));
				will(returnValue(fileInfo));
				
				allowing(nodeService).addProperties(with(equal(dummyNodeRef)), with(any(HashMap.class)));
				
				allowing(fileFolderService).resolveNamePath(dummyNodeRef, validFilePathList2, false);
				will(returnValue(fileInfo));
				
				allowing(fileFolderService).resolveNamePath(dummyNodeRef, validFilePathList3, false);
				will(returnValue(fileInfo));
				
				allowing(contentService).getWriter(dummyNodeRef, ContentModel.PROP_CONTENT, true);
				will(returnValue(contentWriter));
				
				allowing(contentWriter).setMimetype(with(any(String.class)));
				
				allowing(contentWriter).putContent(with(any(InputStream.class)));
				
				allowing(versionService).getVersionHistory(dummyNodeRef);
				will(returnValue(null));
				
				allowing(versionService).createVersion(with(equal(dummyNodeRef)), with(any(Map.class)));
				
				allowing(contentReader).exists();
				will(returnValue(true));
				
			}
		});
	}

	@Test
	public void testMocks() throws FileNotFoundException {
		assertEquals(siteInfo, siteService.getSite("test"));
		assertNull(siteService.getSite("fail"));
		assertEquals(dummyNodeRef, siteInfo.getNodeRef());
		assertEquals(dummyNodeRef, fileFolderService.searchSimple(dummyNodeRef,
				SiteService.DOCUMENT_LIBRARY));
		assertEquals(fileInfo, fileFolderService.resolveNamePath(dummyNodeRef,
				validSourcePathList, false));
		assertNull(fileFolderService.resolveNamePath(dummyNodeRef,
				invalidSourcePathList, false));
		assertEquals(fileInfo, fileFolderService.resolveNamePath(dummyNodeRef,
				validDestinationPathList, false));
		assertNull(fileFolderService.resolveNamePath(dummyNodeRef,
				invalidDestinationPathList, false));
		assertEquals(fileInfo, fileFolderService.resolveNamePath(dummyNodeRef,
				validLogsPathList, false));
		assertNull(fileFolderService.resolveNamePath(dummyNodeRef,
				invalidLogsPathList, false));
		assertEquals(fileInfo, fileFolderService.resolveNamePath(dummyNodeRef,
				validFilePathList, false));
		assertNull(fileFolderService.resolveNamePath(dummyNodeRef,
				invalidFilePathList, false));
		assertEquals(contentReader,
				contentService.getRawReader(contentData.getContentUrl()));
		assertEquals(contentData, fileInfo.getContentData());
		assertEquals(validInputStream, contentReader.getContentInputStream());
		assertEquals(contentWriter, contentService.getWriter(dummyNodeRef, ContentModel.PROP_CONTENT, true));
		context.assertIsSatisfied();
	}

	@Test
	public void testFsSourceReadValid() throws IOException {
		ByggRedaUtil bru = new ByggRedaUtil();
		bru.setSiteService(siteService);
		bru.setFileFolderService(fileFolderService);
		bru.setContentService(contentService);
		bru.setSourceType(ByggRedaUtil.SOURCE_TYPE_FS);
		bru.setNodeService(nodeService);
		bru.setVersionService(versionService);
		
		// All valid
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertTrue(bru.run(validSourcePath, validMetaFileName));
		Set<ByggRedaDocument> documents = bru.getDocuments();
		assertTrue(documents!=null);
		assertEquals(1, documents.size());
		context.assertIsSatisfied();
	}

	@Test
	public void testFsSourceReadInvalid() throws IOException {

		ByggRedaUtil bru = new ByggRedaUtil();
		bru.setSiteService(siteService);
		bru.setFileFolderService(fileFolderService);
		bru.setSourceType(ByggRedaUtil.SOURCE_TYPE_FS);
		bru.setNodeService(nodeService);
		bru.setVersionService(versionService);
		
		// Invalid Destinationpath
		bru.setDestinationPath(invalidDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath(null);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));

		// Invalid Logs path
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(invalidLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(null);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));

		// Invalid site name
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("fail");
		assertFalse(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("");
		assertFalse(bru.run(validSourcePath, validMetaFileName));

		// Invalid Source folder
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(invalidSourcePath, validMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(null, validMetaFileName));

		// Invalid Metadata filename
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, invalidMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, null));

		context.assertIsSatisfied();
	}

	@Test
	public void testRepoSourceReadValid() throws IOException,
			FileNotFoundException {
		ByggRedaUtil bru = new ByggRedaUtil();
		bru.setSiteService(siteService);
		bru.setFileFolderService(fileFolderService);
		bru.setSourceType(ByggRedaUtil.SOURCE_TYPE_REPO);
		bru.setNodeService(nodeService);
		bru.setVersionService(versionService);
		
		// All valid
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertTrue(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath("/" + validDestinationPath + "/");
		bru.setLogPath("/" + validLogsPath + "/");
		try {
			bru.run("/" + validSourcePath + "/", validMetaFileName);
			assertTrue(false);
		} catch (RuntimeException ex) {
			// Expected since the file was already used and closed
		}

		context.assertIsSatisfied();
	}

	@Test
	public void testRepoSourceReadInvalid() throws IOException {

		ByggRedaUtil bru = new ByggRedaUtil();
		bru.setSiteService(siteService);
		bru.setFileFolderService(fileFolderService);
		bru.setSourceType(ByggRedaUtil.SOURCE_TYPE_REPO);
		bru.setNodeService(nodeService);
		bru.setVersionService(versionService);
		
		// Invalid Destinationpath
		bru.setDestinationPath(invalidDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath(null);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));

		// Invalid Logs path
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(invalidLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(null);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, validMetaFileName));

		// Invalid site name
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("fail");
		assertFalse(bru.run(validSourcePath, validMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName(null);
		assertFalse(bru.run(validSourcePath, validMetaFileName));

		// Invalid Source folder
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(invalidSourcePath, validMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(null, validMetaFileName));

		// Invalid Metadata filename
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, invalidMetaFileName));
		bru.setDestinationPath(validDestinationPath);
		bru.setLogPath(validLogsPath);
		bru.setSiteName("test");
		assertFalse(bru.run(validSourcePath, null));

		context.assertIsSatisfied();
	}

}
