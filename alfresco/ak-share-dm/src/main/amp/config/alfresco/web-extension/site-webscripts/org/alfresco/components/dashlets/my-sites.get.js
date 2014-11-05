//@overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-sites.get.js
/*
 * Copyright (C) 2012-2013 Alingsås Kommun
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


function main()
{
   // Check for IMAP server status
   var result = remote.call("/imap/servstatus"),
      imapServerEnabled = (result.status == 200 && result == "enabled");

   // Prepare the model for the template
   model.imapServerEnabled = imapServerEnabled;
   
   result = remote.call("/alingsas/user/createsiteenabled");
   // Check if user is allowed to create a site
   model.createSiteEnabled = (result.status == 200 && result == "true");
   
   logger.log("SitesResult: "+result+", Status:"+result.status);
   
   
}

main();