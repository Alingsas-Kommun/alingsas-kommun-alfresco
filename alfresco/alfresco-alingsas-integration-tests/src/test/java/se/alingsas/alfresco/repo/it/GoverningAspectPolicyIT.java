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
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.alingsas.alfresco.repo.model.AkDmModel;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GoverningAspectPolicyIT extends AbstractComponentIT {

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

        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        // Create a site
        site = createSite("site-dashboard" + System.currentTimeMillis());
        assertNotNull(site);

        // Create a user
        siteManagerUser = "sitemanager" + System.currentTimeMillis();
        siteManagerNodeRef = createUser(siteManagerUser);

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
    }

    @Test
    public void testGoverningAspectPolicy() {

        Date date = new Date();
        testFolder = fileFolderService.create(documentLibrary, "testFolder", ContentModel.TYPE_CONTENT);
        creator = nodeService.getProperty(testFolder.getNodeRef(), ContentModel.PROP_CREATOR);

        // Add mandatory properties
        props = new HashMap<QName, Serializable>();
        //props.put(AkDmModel.PROP_AKDM_GOVERNING_DOC_RESPONSIBLE, creator);
        props.put(AkDmModel.PROP_AKDM_GOVERNING_DECISION_DATE, date);
        props.put(AkDmModel.PROP_AKDM_GOVERNING_GROUP, "Regel");
        props.put(AkDmModel.PROP_AKDM_GOVERNING_TARGET_GROUP, "KLK");
        props.put(AkDmModel.PROP_AKDM_GOVERNING_INSTANCE, "KS");
        // Folder should not have akdm:governing aspect
        assertFalse(nodeService.hasAspect(testFolder.getNodeRef(), AkDmModel.ASPECT_AKDM_GOVERNING));
        nodeService.addAspect(testFolder.getNodeRef(), AkDmModel.ASPECT_AKDM_GOVERNING, props);
        assertTrue(nodeService.hasAspect(testFolder.getNodeRef(), AkDmModel.ASPECT_AKDM_GOVERNING));

        //jämför testFolders skapare med aspekt propertyn dokumentansvarig (AkDmModel.PROP_AKDM_GOVERNING_DOC_RESPONSIBLE)
        assertEquals(creator, nodeService.getProperty(testFolder.getNodeRef(), AkDmModel.PROP_AKDM_GOVERNING_DOC_RESPONSIBLE));

    }

}
