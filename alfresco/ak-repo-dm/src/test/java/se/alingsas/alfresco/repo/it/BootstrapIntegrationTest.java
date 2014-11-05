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
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;

/**
 * Bootstrap test, used to assure that everything is loaded okay
 * @author Marcus Svensson
 *
 */
public class BootstrapIntegrationTest extends AbstractRepoIntegrationTest {

  static SiteInfo site;
  static String siteManagerUser;

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

  @Test
  public void test() {

    Set<NodeRef> allRootNodes = _nodeService.getAllRootNodes(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

    assertNotNull(allRootNodes);
    assertTrue(allRootNodes.size() > 0);
  }

}
