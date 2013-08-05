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
    // Get the filter parameters
    var nameFilter = args["nf"];
    var sitePreset = args["spf"];
    var sizeString = args["size"];
    
    
    //Alingsås customization start
    //Filter out user-homes from the result
    var resultSites = [];
    // Get the list of sites
    var allSites = siteService.getSites(nameFilter, sitePreset, sizeString != null ? parseInt(sizeString) : -1);
    
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