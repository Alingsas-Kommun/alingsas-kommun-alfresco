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

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class DocumentNumberingPolicyIntegrationTest extends AbstractRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(DocumentNumberingPolicyIntegrationTest.class);

  private static SiteInfo site;
  private static String siteManagerUser;
  private static NodeRef documentLibrary;

  private static final int CREATE_FILE_ITERATIONS = 30;
  private static final int THREADS = 10;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

    // Create a site
    site = createSite("site-dashboard");
    assertNotNull(site);

    // Create a user
    siteManagerUser = "sitemanager" + System.currentTimeMillis();
    createUser(siteManagerUser);

    // Make user the site manager of the site
    _siteService.setMembership(site.getShortName(), siteManagerUser, SiteModel.SITE_MANAGER);

    documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
    if (documentLibrary == null) {
      documentLibrary = _siteService.createContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
    }
    assertNotNull(documentLibrary);

    // Run the tests as this user
    _authenticationComponent.setCurrentUser(siteManagerUser);
  }

  @Override
  public void afterClassSetup() {
    super.afterClassSetup();
    _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    _siteService.deleteSite(site.getShortName());
    _personService.deletePerson(siteManagerUser);
    if (_authenticationService.authenticationExists(siteManagerUser)) {
      _authenticationService.deleteAuthentication(siteManagerUser);
    }
    _authenticationComponent.clearCurrentSecurityContext();
  }

  protected NodeRef createRandomFile() {
    String fileName = GUID.generate();
    LOG.trace("Creating random file with name " + fileName);

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(ContentModel.PROP_NAME, fileName);
    return _nodeService.createNode(documentLibrary, ContentModel.ASSOC_CONTAINS, QName.createQName(fileName), AkDmModel.TYPE_AKDM_GENERAL_DOC, properties).getChildRef();
  }

  @Test
  public void testMultipleFileCreationsInSeparateTransactions() {
    LOG.debug("Creating " + CREATE_FILE_ITERATIONS + " random files in separate transactions");
    for (int i = 0; i < CREATE_FILE_ITERATIONS; i++) {
      createRandomFile();
    }
  }

  @Test
  public void testMultipleFileCreationsInOneTransaction() {
    _transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        LOG.debug("Creating " + CREATE_FILE_ITERATIONS + " random files in one transaction");
        for (int i = 0; i < CREATE_FILE_ITERATIONS; i++) {
          createRandomFile();
        }
        return null;
      }
    }, false, true);

  }

  @Test
  public void testMultipleFileCreationsInMultipleThreads() throws Exception {
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

        _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
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
