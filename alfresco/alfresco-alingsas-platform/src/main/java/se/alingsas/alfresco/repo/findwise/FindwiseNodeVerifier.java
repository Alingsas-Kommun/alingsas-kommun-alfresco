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

package se.alingsas.alfresco.repo.findwise;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.redpill.alfresco.repo.findwise.SearchIntegrationService;
import org.redpill.alfresco.repo.findwise.processor.DefaultVerifierProcessor;

import se.alingsas.alfresco.repo.model.AkDmModel;

/**
 * Verifies that a node can be exported to index service
 * 
 * @author Marcus Svartmark - Redpill Linpro AB
 *
 */
public class FindwiseNodeVerifier extends DefaultVerifierProcessor {

  private static final Logger LOG = Logger.getLogger(FindwiseNodeVerifier.class);
  protected SiteService siteService;
  
  private static final String SECRET_PRESET = "ak-dm-secrecy";
  
  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  @Override
  public boolean verifyDocument(final NodeRef node) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Starting to execute FindwiseNodeVerifier#verifyDocument for node: " + node.getId());
    }
    boolean result = super.verifyDocument(node);
    if (result) {
      // Check so that only public documents are pushed
      String property = (String) nodeService.getProperty(node, AkDmModel.PROP_AKDM_DOC_SECRECY);
      if (!AkDmModel.CONST_SECRECY_STATUS_PUBLIC.equalsIgnoreCase(property)) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Verification failed - Document is not public");
        }
        result = false;
      }
      // Check so that only completed documents are published
      property = (String) nodeService.getProperty(node, AkDmModel.PROP_AKDM_DOC_STATUS);
      if (AkDmModel.CONST_STATUS_WORKING_DOCUMENT.equalsIgnoreCase(property)) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Verification failed - Document is not completed");
        }
        result = false;
      }
      // Check so that only documents within sites are handled
      SiteInfo site = siteService.getSite(node);
      if (site == null) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Verification failed - Document is not located within a site");
        }
        result = false;
      }

      if (site != null && SECRET_PRESET.equalsIgnoreCase(site.getSitePreset())) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Verification failed - Document is not located within a public site");
        }
        result = false;
      }
    }

    if (LOG.isDebugEnabled() && !result) {
      LOG.debug("Node " + node + " did not meet the verification criteria.");
    }
    return result;
  }

  public void setSearchIntegrationService(SearchIntegrationService searchIntegrationService) {
    searchIntegrationService.setNodeVerifierProcessor(this);
  }

}
