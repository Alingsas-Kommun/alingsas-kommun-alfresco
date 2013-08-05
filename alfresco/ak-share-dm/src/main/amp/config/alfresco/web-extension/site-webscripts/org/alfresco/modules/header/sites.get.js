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

/**
 * Sites module GET method
 */

const PREF_FAVOURITE_SITES = "org.alfresco.share.sites.favourites";

function sortByTitle(site1, site2)
{
   return (site1.title > site2.title) ? 1 : (site1.title < site2.title) ? -1 : 0;
}

function main()
{
   // Model variables - must have defaults
   var favouriteSites = [],
      currentSiteIsFav = false,
      siteTitle = "";

   var prefs,
      favourites;

   // Call the repo for the user's favourite sites
   // TODO: Clean-up old favourites here?
   var result = remote.call("/api/people/" + encodeURIComponent(user.name) + "/preferences");
   if (result.status == 200 && result != "{}")
   {
      prefs = eval('(' + result + ')');
      
      // Populate the favourites object literal for easy look-up later
      favourites = eval('try{(prefs.' + PREF_FAVOURITE_SITES + ')}catch(e){}');
   }

   if (typeof favourites != "object")
   {
      favourites = {};
   }

   // Call the repo to return a specific list of site metadata i.e. those in the fav list
   // and ensure the current user is a member of each before adding to fav list
   var query =
   {
      shortName:
      {
         match: "exact-membership",
         values: []
      }
   },
      currentSite = args.siteId || "",
      ignoreCurrentSite = false,
      shortName;
   
   for (shortName in favourites)
   {
      if (favourites[shortName])
      {
         query.shortName.values.push(shortName);
      }
   }
   
   // Also tack the current site onto the query, so we can pass the Site Title to header.js
   if (currentSite !== "" && !favourites[currentSite])
   {
      query.shortName.values.push(currentSite);
      ignoreCurrentSite = true;
   }
   
   var connector = remote.connect("alfresco");
   result = connector.post("/api/sites/query", jsonUtils.toJSONString(query), "application/json");
   
   if (result.status == 200)
   {
      var i, ii;
      
      // Create javascript objects from the server response
      // Each item is a favourite site that the user is a member of
      var sites = eval('(' + result + ')'), site;
      
      if (sites.length != 0)
      {
         // Sort the sites by title
         sites.sort(sortByTitle);
         
         for (i = 0, ii = sites.length; i < ii; i++)
         {
            site = sites[i];
            if (site.shortName == currentSite)
            {
               siteTitle = site.title;
               if (ignoreCurrentSite)
               {
                  // The current site was piggy-backing the query call; it's not a favourite
                  continue;
               }
               currentSiteIsFav = true;
            }
            favouriteSites.push(site);
         }
      }
   }
   
   result = remote.call("/alingsas/user/createsiteenabled");
   // Check if user is allowed to create a site
   model.createSiteEnabled = (result.status == 200 && result == "true");
   
   logger.log("SitesResult: "+result+", Status:"+result.status);

   // Prepare the model for the template
   model.currentSiteIsFav = currentSiteIsFav;
   model.favouriteSites = favouriteSites;
   model.siteTitle = siteTitle;
}

main();