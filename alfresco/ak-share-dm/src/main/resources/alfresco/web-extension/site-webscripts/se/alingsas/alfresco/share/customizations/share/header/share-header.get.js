<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">
// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.js

/**
 Hide the create site button for non admins
 https://forums.alfresco.com/forum/end-user-discussions/alfresco-share/make-options-header-admin-only-alfresco-42d-09022013-1746
 https://issues.alfresco.com/jira/browse/ALF-19911
**/
//Disable create site menu if not allowed to
function disableCreateSite() {
  var shareMenu = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_SITES_MENU");
  result = remote.call("/alingsas/user/createsiteenabled");
  // Check if user is allowed to create a site
  model.showCreateSite = (result.status == 200 && result == "true");
  shareMenu.config.showCreateSite = model.showCreateSite;
}
disableCreateSite();

/** 
 Set default search scope to site:
 https://issues.alfresco.com/jira/browse/MNT-13350
**/
function setDefaultSearchScopeToSite() {
  var searchMenuBase = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_SEARCH");
  //searchMenuBase.config.site = page.url.templateArgs.site; //Does not work
  var scope = page.url.templateArgs.site;
  if (scope===null) {
  	scope = "all_sites";
  }
  searchMenuBase.config.defaultSearchScope = scope;
}
setDefaultSearchScopeToSite();

/**
 Customize site dashboard is not visible for presets with a custom title-id
 https://issues.alfresco.com/jira/browse/MNT-12103
 Here we have the following ids: 
  page.akdm.siteDashboard.public.title
  page.akdm.siteDashboard.secrecy.title
  page.akdm.siteDashboard.template.title
**/
function addCustomizeDashboardLink() {
  //Show the become site manager button for admins which are not members of a site!
  var siteConfig = widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITE_CONFIGURATION_DROPDOWN");
  if (siteConfig != null) {
    var siteData = getSiteData();
    if (siteData != null) {
      if (siteData.userIsSiteManager) {
        //If the user is a site manager then add the missing customze dashboard link
        if (page.titleId == "page.akdm.siteDashboard.public.title" || page.titleId == "page.akdm.siteDashboard.secrecy.title" || page.titleId == "page.akdm.siteDashboard.template.title")
        {
          // Add Customize Dashboard
          siteConfig.config.widgets.splice(0,0,{
            id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
            name: "alfresco/menus/AlfMenuItem",
            config: {
              id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
              label: "customize_dashboard.label",
              iconClass: "alf-cog-icon",
              targetUrl: "site/" + page.url.templateArgs.site + "/customise-site-dashboard"
            }
          });
        }
      }
    }
  }
}
addCustomizeDashboardLink();

function addAlingsasSitePresets(){
  var siteService = widgetUtils.findObject(model.jsonModel, "id", "SITE_SERVICE");
  if (siteService && siteService.config)
  {
    siteService.config.sitePresetsToRemove = ["site-dashboard"];
    siteService.config.additionalSitePresets = [{ label: "preset.ak-dm-public.title", value: "ak-dm-public" }, { label: "preset.ak-dm-secrecy.title", value: "ak-dm-secrecy" }, { label: "preset.ak-dm-template.title", value: "ak-dm-template" }];
  }
}
addAlingsasSitePresets();