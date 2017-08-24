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

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import se.alingsas.alfresco.repo.model.AkDmModel;

public class GoverningAspectPolicyIT extends AbstractAkRepoIT {

    private static final Logger LOG = Logger.getLogger(GoverningAspectPolicyIT.class);

    private static SiteInfo site;
    private static String siteManagerUser;
    private static NodeRef siteManagerNodeRef;
    private static NodeRef documentLibrary;
    private FileInfo testFolder;
    private Map<QName, Serializable> props;
    private Serializable creator;

    private static final int CREATE_FILE_ITERATIONS = 30;
    private static final int THREADS = 10;

    @Before
    public void setup() {

        _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        // Create a site
        site = createSite("site-dashboard" + System.currentTimeMillis());
        assertNotNull(site);

        // Create a user
        siteManagerUser = "sitemanager" + System.currentTimeMillis();
        siteManagerNodeRef = createUser(siteManagerUser);

        _transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable {
                // Make user the site manager of the site
                _siteService.setMembership(site.getShortName(), siteManagerUser, SiteModel.SITE_MANAGER);

                documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

                if (documentLibrary == null) {
                    documentLibrary = _siteService.createContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
                }
                return null;
            }
        }, false, isRequiresNew());
        assertNotNull(documentLibrary);

        // Run the tests as this user
        _authenticationComponent.setCurrentUser(siteManagerUser);
    }

    @After
    public void afterClassSetup() {
        _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        deleteSite(site);
        deleteUser(siteManagerUser);
        _authenticationComponent.clearCurrentSecurityContext();
    }

    @Test
    public void testGoverningAspectPolicy() {
        _authenticationComponent.setCurrentUser(siteManagerUser);

        _transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable {
                Date date = new Date();
                testFolder = _fileFolderService.create(documentLibrary, "testFolder", ContentModel.TYPE_CONTENT);
                creator = _nodeService.getProperty(testFolder.getNodeRef(), ContentModel.PROP_CREATOR);

                // Add mandatory properties
                props = new HashMap<QName, Serializable>();
                //props.put(AkDmModel.PROP_AKDM_GOVERNING_DOC_RESPONSIBLE, creator);
                props.put(AkDmModel.PROP_AKDM_GOVERNING_DECISION_DATE, date);
                props.put(AkDmModel.PROP_AKDM_GOVERNING_GROUP, "Regel");
                props.put(AkDmModel.PROP_AKDM_GOVERNING_TARGET_GROUP, "KLK");
                props.put(AkDmModel.PROP_AKDM_GOVERNING_INSTANCE, "KS");
                // Folder should not have akdm:governing aspect
                assertFalse(_nodeService.hasAspect(testFolder.getNodeRef(), AkDmModel.ASPECT_AKDM_GOVERNING));
                _nodeService.addAspect(testFolder.getNodeRef(), AkDmModel.ASPECT_AKDM_GOVERNING, props);
                assertTrue(_nodeService.hasAspect(testFolder.getNodeRef(), AkDmModel.ASPECT_AKDM_GOVERNING));

                //jämför testFolders skapare med aspekt propertyn dokumentansvarig (AkDmModel.PROP_AKDM_GOVERNING_DOC_RESPONSIBLE)
                assertEquals(creator, _nodeService.getProperty(testFolder.getNodeRef(), AkDmModel.PROP_AKDM_GOVERNING_DOC_RESPONSIBLE));
                return null;
            }
        }, false, isRequiresNew());

    }

}
