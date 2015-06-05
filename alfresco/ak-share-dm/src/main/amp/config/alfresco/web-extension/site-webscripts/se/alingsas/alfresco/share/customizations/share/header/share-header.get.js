// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.js

/**
 Hide the create site button for non admins
 https://forums.alfresco.com/forum/end-user-discussions/alfresco-share/make-options-header-admin-only-alfresco-42d-09022013-1746
 https://issues.alfresco.com/jira/browse/ALF-19911
**/

//Disable create site menu if not allowed to
function akDisableCreateSite() {
	var shareMenu = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_SITES_MENU");
	result = remote.call("/alingsas/user/createsiteenabled");
	// Check if user is allowed to create a site
	model.showCreateSite = (result.status == 200 && result == "true");
	shareMenu.config.showCreateSite = model.showCreateSite;
}

akDisableCreateSite();

