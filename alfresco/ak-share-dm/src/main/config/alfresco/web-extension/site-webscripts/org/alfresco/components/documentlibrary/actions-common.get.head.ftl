<#include "../component.head.inc">
<#-- Document Library Actions: Supports concatenated JavaScript files via build scripts -->
<#if DEBUG>
   <script type="text/javascript" src="${page.url.context}/res/ak/components/documentlibrary/actions.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/actions-util.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/simple-dialog.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/global-folder.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/copy-move-to.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/components/people-finder/people-finder.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/permissions.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/aspects.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/social-publish.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/cloud-folder.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/cloud-auth.js"></script>
<#else>
   <script type="text/javascript" src="${page.url.context}/res/ak/components/documentlibrary/actions-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/actions-util-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/simple-dialog-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/global-folder-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/copy-move-to-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/components/people-finder/people-finder-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/permissions-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/aspects-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/social-publish-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/cloud-folder-min.js"></script>
   <script type="text/javascript" src="${page.url.context}/res/modules/cloud-auth-min.js"></script>
</#if>
<#-- Common actions styles -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/actions.css" />
<#-- Global Folder Picker (req'd by Copy/Move To) -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/documentlibrary/global-folder.css" />
<#-- People Finder Assets (req'd by Assign Workflow)  -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/people-finder/people-finder.css" />
<#-- Manage Permissions -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/documentlibrary/permissions.css" />
<#-- Manage Aspects -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/documentlibrary/aspects.css" />
<#-- Social Publishing -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/social-publish.css" />
<#-- Cloud Authentication -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/cloud/cloud-auth-form.css" />
<#-- Cloud Folder Picker -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/cloud/cloud-folder-picker.css" />
<#-- Cloud Sync Status -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/cloud/cloud-sync-status.css" />
