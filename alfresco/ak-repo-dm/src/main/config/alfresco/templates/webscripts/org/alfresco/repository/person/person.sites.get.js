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
    // Get the user name of the person to get
    var userName = url.templateArgs.userid;
    
    // Get the person who has that user name
    var person = people.getPerson(userName);
    
    if (person === null)  
    {
        // Return 404 - Not Found      
        status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
        return;
    }
    
    // Get the list of sites
    var size = 0,
        sizeString = args["size"];
    if (sizeString != null)
    {
        size = parseInt(sizeString);
    }
    //Alingsås customization start
    //Filter out user-homes from the result
    var resultSites = [];
    var allSites = siteService.listUserSites(userName, size);
    
    for (var i = 0; i < allSites.length; i++)
    {
       if (allSites[i].getShortName() != "user-homes")
       {
          // Construct a new object with the site details we want...
    	   resultSites.push(allSites[i]);
       }
    }      
    model.sites = resultSites
    //Alingsås customiation end
    model.roles = (args["roles"] !== null ? args["roles"] : "managers");
}

main();