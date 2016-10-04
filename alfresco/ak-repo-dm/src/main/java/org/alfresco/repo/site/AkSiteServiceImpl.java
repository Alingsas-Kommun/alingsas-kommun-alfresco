// @overridden projects/repository/source/java/org/alfresco/repo/site/SiteServiceImpl.java
/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.site;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Custom Site Service Implementation. Also bootstraps the site DM stores.
 * Customization of site service. Adds sorting on title on list and search.
 *
 * @author Roy Wetherall
 * @author Marcus SVesson
 */
public class AkSiteServiceImpl extends SiteServiceImpl {

  /**
   * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String,
   * java.lang.String, int)
   */
  @Override
  public List<SiteInfo> listSites(final String filter, final String sitePresetFilter, int size) {
    List<SiteInfo> result = Collections.emptyList();

    NodeRef siteRoot = getSiteRoot();
    if (siteRoot != null) {
      final boolean filterHasValue = filter != null && filter.length() != 0;
      final boolean sitePresetFilterHasValue = sitePresetFilter != null && sitePresetFilter.length() > 0;

      //AK Customization begin
      List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>();
      sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, true));
      //AK Customization end

      PagingRequest pagingRequest = new PagingRequest(size <= 0 ? Integer.MAX_VALUE : size);
      List<FilterProp> filterProps = new ArrayList<FilterProp>();

      if (filterHasValue) {
        filterProps.add(new FilterPropString(ContentModel.PROP_NAME, filter, FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_TITLE, filter, FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_DESCRIPTION, filter, FilterTypeString.STARTSWITH_IGNORECASE));
      }
      if (sitePresetFilterHasValue) {
        filterProps.add(new FilterPropString(SiteModel.PROP_SITE_PRESET, sitePresetFilter, FilterTypeString.EQUALS));
      }

      PagingResults<SiteInfo> allSites = listSites(filterProps, sortProps, pagingRequest);
      result = allSites.getPage();
    }

    return result;
  }

}
