/*
 * Copyright (C) 2012-2014 Alingsås Kommun
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
package se.alingsas.alfresco.repo.it;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.alingsas.alfresco.repo.model.AkDmModel;

import java.io.Serializable;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DocumentPolicyIT extends AbstractComponentIT {

    private static final Logger LOG = Logger.getLogger(DocumentPolicyIT.class);

    private static SiteInfo site;
    private static String siteManagerUser;
    private static NodeRef siteManagerNodeRef;
    private static NodeRef documentLibrary;

    private static final int CREATE_FILE_ITERATIONS = 30;
    private static final int THREADS = 10;

    private Map<QName, Serializable> properties;

    @Before
    public void setup() {
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        // Create a site
        site = createSite("site-dashboard" + System.currentTimeMillis());
        assertNotNull(site);

        // Create a user
        siteManagerUser = "sitemanager" + System.currentTimeMillis();
        siteManagerNodeRef = createUser(siteManagerUser);
        // Make user the site manager of the site
        siteService.setMembership(site.getShortName(), siteManagerUser, SiteModel.SITE_MANAGER);
        documentLibrary = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
        if (documentLibrary == null) {
            documentLibrary = siteService.createContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        }
        assertNotNull(documentLibrary);
        // Run the tests as this user
        authenticationComponent.setCurrentUser(siteManagerUser);
    }

    @After
    public void afterClassSetup() {
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        deleteSite(site);
        deleteUser(siteManagerUser);
        //authenticationComponent.clearCurrentSecurityContext();
    }

    protected NodeRef createRandomFile() {
        return createRandomFile(documentLibrary);
    }

    protected NodeRef createRandomFile(NodeRef location) {
        String fileName = GUID.generate();
        LOG.trace("Creating random file with name " + fileName);

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, fileName);
        return nodeService.createNode(location, ContentModel.ASSOC_CONTAINS, QName.createQName(fileName), ContentModel.TYPE_CONTENT, properties).getChildRef();
    }

    @Test
    public void testAlingsasDocumentType() throws FileExistsException, FileNotFoundException {
        LOG.info("******* START testAlingsasDocumentType");

        NodeRef userHomeNodeRef = repository.getUserHome(siteManagerNodeRef);
        NodeRef sharedHomeNodeRef = repository.getSharedHome();
        NodeRef companyHomeNodeRef = repository.getCompanyHome();
        NodeRef nodeRef;

        nodeRef = createRandomFile(userHomeNodeRef);
        fileFolderService.move(nodeRef, documentLibrary, null);
        assertEquals("Node in user home should get new type", AkDmModel.TYPE_AKDM_DOCUMENT, nodeService.getType(nodeRef));

        nodeRef = createRandomFile(sharedHomeNodeRef);
        fileFolderService.move(nodeRef, documentLibrary, null);
        assertEquals("Node in shared home should get new type", AkDmModel.TYPE_AKDM_DOCUMENT, nodeService.getType(nodeRef));

        nodeRef = createRandomFile();

        assertEquals("Node in a site should get a new type", AkDmModel.TYPE_AKDM_DOCUMENT, nodeService.getType(nodeRef));
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        nodeRef = createRandomFile(companyHomeNodeRef);
        assertEquals("Files in repository folder should not become akdm documents", ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));

        FileInfo copy = fileFolderService.copy(nodeRef, documentLibrary, null);
        assertEquals("Copied node should get new type", AkDmModel.TYPE_AKDM_DOCUMENT, nodeService.getType(copy.getNodeRef()));

        nodeRef = createRandomFile(companyHomeNodeRef);
        fileFolderService.move(nodeRef, documentLibrary, null);
        assertEquals("Moved node should get new type", AkDmModel.TYPE_AKDM_DOCUMENT, nodeService.getType(nodeRef));

        LOG.info("******* END testAlingsasDocumentType");
    }


    @Test
    public void testMultipleFileCreationsInSeparateTransactions() {
        LOG.info("******* START testMultipleFileCreationsInSeparateTransactions");

        LOG.debug("Creating " + CREATE_FILE_ITERATIONS + " random files in separate transactions");
        for (int i = 0; i < CREATE_FILE_ITERATIONS; i++) {
            transactionHelper.doInTransaction((RetryingTransactionHelper.RetryingTransactionCallback<Void>) () -> {
                createRandomFile();
                return null;
            }, false, isRequiresNew());
        }
        LOG.info("******* END testMultipleFileCreationsInSeparateTransactions");
    }

    @Test
    public void testNumbering() throws FileExistsException, FileNotFoundException {
        LOG.info("******* START testNumbering");
        //transactionHelper.doInTransaction((RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>) () -> {

        NodeRef nodeRef = createRandomFile();
        String number = (String) nodeService.getProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER);
        assertNotNull("Document number missing!", number);
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        NodeRef companyHome = repository.getCompanyHome();
        nodeRef = createRandomFile(companyHome);
        number = (String) nodeService.getProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER);
        assertNull("Documents in repo home should not have numbers", number);

        FileInfo copy = fileFolderService.copy(nodeRef, documentLibrary, null);
        number = (String) nodeService.getProperty(copy.getNodeRef(), AkDmModel.PROP_AKDM_DOC_NUMBER);
        assertNotNull("Copied documents should have numbers", number);
        FileInfo copy2 = fileFolderService.copy(copy.getNodeRef(), documentLibrary, GUID.generate());
        String number2 = (String) nodeService.getProperty(copy2.getNodeRef(), AkDmModel.PROP_AKDM_DOC_NUMBER);
        assertNotNull("Copied documents should have numbers", number2);
        assertNotEquals("Copied documents should get a NEW number", number, number2);

        nodeRef = createRandomFile(companyHome);
        fileFolderService.move(nodeRef, documentLibrary, null);
        assertEquals("Moved node should get new type", AkDmModel.TYPE_AKDM_DOCUMENT, nodeService.getType(nodeRef));
        number = (String) nodeService.getProperty(nodeRef, AkDmModel.PROP_AKDM_DOC_NUMBER);
        assertNotNull("Copied documents should have numbers", number);
        //return null;
        //}, false, isRequiresNew());
        LOG.info("******* END testNumbering");
    }

    @Test
    public void testMultipleFileCreationsInOneTransaction() {
        LOG.info("******* START testMultipleFileCreationsInOneTransaction");

        LOG.debug("Creating " + CREATE_FILE_ITERATIONS + " random files in one transaction");
        for (int i = 0; i < CREATE_FILE_ITERATIONS; i++) {
            createRandomFile();
        }

        LOG.info("******* END testMultipleFileCreationsInOneTransaction");

    }

    @Test
    public void testMultipleFileCreationsInMultipleThreads() throws Exception {
        LOG.info("******* START testMultipleFileCreationsInMultipleThreads");
        ArrayList<RunnableThread> threadList = new ArrayList<RunnableThread>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            threadList.add(new RunnableThread("thread" + i));
        }
        try {
            while (threadList.size() > 0) {
                RunnableThread runnableThread = threadList.get(0);
                if (runnableThread.getStatus() == State.TERMINATED) {
                    threadList.remove(runnableThread);
                    Exception exception = runnableThread.getException();
                    if (exception != null) {
                        // throw new Exception("Exception occured",exception);
                        throw exception;
                    }
                } else {
                    LOG.info("Waiting for all threads to complete");
                    Thread.currentThread().sleep(100);
                }
            }
        } catch (InterruptedException e) {
        }
        LOG.info("******* END testMultipleFileCreationsInMultipleThreads");
    }

    class RunnableThread implements Runnable {

        Thread runner;
        Exception exception = null;

        public RunnableThread() {
        }

        public RunnableThread(String threadName) {
            runner = new Thread(this, threadName); // (1) Create a new thread.
            runner.start(); // (2) Start the thread.
        }

        public void run() {
            try {

                authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
                LOG.debug("Creating " + CREATE_FILE_ITERATIONS + " random files in a separate thread");
                for (int i = 0; i < CREATE_FILE_ITERATIONS; i++) {
                    createRandomFile();
                }
            } catch (Exception e) {
                exception = e;
            }
        }

        public State getStatus() {
            return runner.getState();
        }

        public Exception getException() {
            return exception;
        }
    }
}
