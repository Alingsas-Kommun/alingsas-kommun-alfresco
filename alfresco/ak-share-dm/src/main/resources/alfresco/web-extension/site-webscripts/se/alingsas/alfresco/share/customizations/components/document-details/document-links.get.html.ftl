<#-- // @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-links.get.html.ftl -->
<#-- Customization which adds a link to download a document and also add server url to prevent JS errors due to post render proxy filtering of urls -->
<@markup id="ak-js" target="js" action="after">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/ak/components/document-details/document-links.js" group="document-details"/>
</@>

<@markup id="ak-widgets" target="widgets" action="replace">
  <#if document??>
      <#if document.workingCopy??>
          <!-- Don't display links since this nodeRef points to one of a working copy pair -->
      <#else>
         <#assign el=args.htmlid?html>
         <#assign linkUrl = url.server?replace(":443", "")>    
         <@inlineScript group="document-details">
            new Alfresco.DocumentLinks("${el}").setOptions(
            {
               nodeRef: "${nodeRef?js_string}",
               fileName: "${document.fileName?js_string}",
               siteId: <#if site??>"${site?js_string}"<#else>null</#if>,
               actualHostName: <#list linkUrl?split(".") as var>"${var?js_string}"<#if var_has_next>+ "." +</#if></#list>,
               actualHostContext: "${url.getContext()?js_string}",
            }).setMessages(${messages});
                  
            YAHOO.util.Event.onContentReady("${args.htmlid?js_string}-heading", function() {
               Alfresco.util.createTwister("${args.htmlid?js_string}-heading", "DocumentLinks");
            });
         </@>
      </#if>
   </#if>
</@>

<@markup id="ak-html" target="html" action="replace">
   <@uniqueIdDiv>
      <#if document??>
         <#assign el=args.htmlid?html>
         <#if document.workingCopy??>
            <!-- Don't display links since this nodeRef points to one of a working copy pair -->
         <#else>
         <div id="${el}-body" class="document-links document-details-panel">
            <h2 id="${el}-heading" class="thin dark">${msg("header")}</h2>
            <div class="panel-body">
               <!-- Current page url - (javascript will prefix with the current browser location) -->
               <h3 class="thin dark">${msg("document-details.share.url.document-preview")}</h3>
               <div class="link-info">
                  <input id="${el}-page" value=""/>
                  <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("page.copy")}</a>
               </div>
              <h3 class="thin dark">${msg("document-details.share.url.preview")}</h3>
               <div class="link-info">
                  <input id="${el}-preview" value=""/>
                  <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("page.copy")}</a>
               </div>
               <h3 class="thin dark">${msg("document-details.share.url.download")}</h3>
               <div class="link-info">
                  <input id="${el}-download" value=""/>
                  <a href="#" name=".onCopyLinkClick" class="${el} hidden">${msg("page.copy")}</a>
               </div>

            </div>
         </div>
         </#if>
      </#if>
   </@>
</@>
