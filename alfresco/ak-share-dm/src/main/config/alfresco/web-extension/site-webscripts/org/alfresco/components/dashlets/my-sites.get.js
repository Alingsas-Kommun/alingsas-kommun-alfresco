/*function getUserGroups(person) {
	// person is a Person Node
   	var containerGroups = people.getContainerGroups(person);
   	var groupNames = new Array();
  	for (var i=0;i<containerGroups.length;i++) {
    	var group = containerGroups[i].getQnamePath();
      	var p = group.split(":");
      	var groupName = p[p.length-1];
      	groupNames.push(groupName);
    }
   	return groupNames;
}

function checkAllowCreateSite(person) {
  var groups = getUserGroups(person);
  for (var i=0;i<groups.length;i++) {
      if (groups[i] == "GROUP_ALFRESCO_ADMINISTRATORS" || 
          groups[i] == "GROUP_PLATSINITIATORER") {
          return true
      }
      //logger.log(groups[i]); 
  }
  return false;
}

//Call the repo to return a specific list of site metadata
result = connector.post("/api/sites/query", jsonUtils.toJSONString(query), "application/json");

if (result.status == 200)
{
   var sites = eval('(' + result + ')'), site;

   // Extract site titles
   for (var i = 0, ii = sites.length; i < ii; i++)
   {
      site = sites[i];
      siteTitles[site.shortName] = site.title;
   }
}*/

function main()
{
   // Check for IMAP server status
   var result = remote.call("/imap/servstatus"),
      imapServerEnabled = (result.status == 200 && result == "enabled");

   // Prepare the model for the template
   model.imapServerEnabled = imapServerEnabled;
   
   result = remote.call("/alingsas/user/createsiteenabled");
   // Check if user is allowed to create a site
   model.createSiteEnabled = (result.status == 200 && result == "true");;
   
   logger.log("SitesResult: "+result+", Status:"+result.status);
   
   
}

main();