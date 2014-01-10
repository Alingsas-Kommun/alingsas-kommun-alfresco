package se.alingsas.alfresco.repo.utils.documentnumber;

import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.DataListModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class DocumentNumberingPolicyIntegrationTest {
  private static final Logger LOG = Logger.getLogger(DocumentNumberingPolicyIntegrationTest.class);
  protected static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
  protected static AuthenticationComponent authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
  protected static ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");
  protected static RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();
  protected static BehaviourFilter behaviourFilter = (BehaviourFilter) applicationContext.getBean("policyBehaviourFilter");

  protected static Repository repository = (Repository) applicationContext.getBean("repositoryHelper");
  protected static SiteService siteService = serviceRegistry.getSiteService();
  protected static MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
  protected static PersonService personService = serviceRegistry.getPersonService();
  private static SiteInfo site;
  private static String siteManagerUser;
  private static NodeRef documentLibrary;

  private static final int CREATE_FILE_ITERATIONS = 30;
  private static final int THREADS = 10;

  @BeforeClass
  public static void setUp() {
    // Setup authentication
    authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

    // Create a site
    site = createSite("site-dashboard");
    assertNotNull(site);

    // Create a user
    siteManagerUser = "sitemanager" + System.currentTimeMillis();
    createUser(siteManagerUser);

    // Make user the site manager of the site
    siteService.setMembership(site.getShortName(), siteManagerUser, SiteModel.SITE_MANAGER);

    documentLibrary = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
    if (documentLibrary == null) {
      documentLibrary = siteService.createContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
    }
    assertNotNull(documentLibrary);

  }

  @AfterClass
  public static void tearDown() {
    authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    siteService.deleteSite(site.getShortName());

    personService.deletePerson(siteManagerUser);
    if (authenticationService.authenticationExists(siteManagerUser)) {
      authenticationService.deleteAuthentication(siteManagerUser);
    }
    authenticationComponent.clearCurrentSecurityContext();
  }

  /**
   * Creates a site and makes sure that a document library and a data list
   * container exist.
   * 
   * @param preset
   *          the site preset.
   * @return
   */
  protected static SiteInfo createSite(final String preset) {
    return transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<SiteInfo>() {
      @Override
      public SiteInfo execute() throws Throwable {
        // Create site
        behaviourFilter.disableBehaviour(SiteModel.TYPE_SITE);
        final SiteService siteService = serviceRegistry.getSiteService();
        String name = "it-" + System.currentTimeMillis();
        SiteInfo site = siteService.createSite(preset, name, name, name, SiteVisibility.PRIVATE);
        assertNotNull(site);
        serviceRegistry.getNodeService().addAspect(site.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);

        // Create document library container
        documentLibrary = siteService.getContainer(name, SiteService.DOCUMENT_LIBRARY);
        if (documentLibrary == null) {
          documentLibrary = siteService.createContainer(name, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        }
        assertNotNull(documentLibrary);
        behaviourFilter.enableBehaviour(SiteModel.TYPE_SITE);
        return site;
      }
    }, false, false);
  }

  /**
   * Creates a user and a related person in the repository.
   * 
   * @param userId
   * @return
   */
  protected static NodeRef createUser(String userId) {
    final MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
    if (!authenticationService.authenticationExists(userId)) {
      authenticationService.createAuthentication(userId, "password".toCharArray());
      PropertyMap properties = new PropertyMap(3);
      properties.put(ContentModel.PROP_USERNAME, userId);
      properties.put(ContentModel.PROP_FIRSTNAME, userId);
      properties.put(ContentModel.PROP_LASTNAME, "Test");

      return serviceRegistry.getPersonService().createPerson(properties);
    } else {
      fail("User exists: " + userId);
      return null;
    }
  }

  protected static NodeRef createRandomFile() {
    String fileName = GUID.generate();
    LOG.trace("Creating random file with name " + fileName);

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(ContentModel.PROP_NAME, fileName);
    return serviceRegistry.getNodeService().createNode(documentLibrary, ContentModel.ASSOC_CONTAINS, QName.createQName(fileName), AkDmModel.TYPE_AKDM_GENERAL_DOC, properties).getChildRef();
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
    transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {

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
          if (exception!=null) {
            //throw new Exception("Exception occured",exception);
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
