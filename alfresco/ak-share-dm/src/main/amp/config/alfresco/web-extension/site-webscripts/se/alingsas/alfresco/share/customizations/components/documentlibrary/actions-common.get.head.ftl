<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/documentlibrary/actions-common.get.head.ftl -->
<#include "/org/alfresco/components/component.head.inc">
<#-- Document Library Actions: Supports concatenated JavaScript files via build scripts -->
<#if DEBUG>
   <script type="text/javascript" src="${page.url.context}/res/ak/components/documentlibrary/actions.js"></script>
<#else>
   <script type="text/javascript" src="${page.url.context}/res/ak/components/documentlibrary/actions-min.js"></script>
</#if>
