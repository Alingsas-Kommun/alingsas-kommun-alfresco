function getUserGroups(person) {
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
          groups[i] == "GROUP_AK-MAN-Siteinitiators") {
          return true
      }
  }
  return false;
}

model.createSiteEnabled = checkAllowCreateSite(person);