<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/workflow/workflow-details-actions.get.html.ftl -->

<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/workflow/workflow-details-actions.css" group="workflow"/>
</@>

<#--
<@markup id="widgets">
   <@createWidgets group="workflow"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#include "../../include/alfresco-macros.lib.ftl" />
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="form-manager workflow-details-actions">
         <div id="${el}-cancel-button" class="actions hidden">
            <button id="${el}-cancel">${msg("button.cancelWorkflow")}</button>
         </div>
         <div id="${el}-delete-button" class="actions hidden">
            <button id="${el}-delete">${msg("button.deleteWorkflow")}</button>
         </div>
      </div>
   </@>
</@>
-->