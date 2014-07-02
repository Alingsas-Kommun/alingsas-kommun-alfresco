<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/workflow/workflow-details-actions.get.html.ftl -->
<#include "../../include/alfresco-macros.lib.ftl" />
<#assign el=args.htmlid?js_string>
<#-- Disable cancel action
<script type="text/javascript">//<![CDATA[
new Alfresco.component.WorkflowDetailsActions("${el}").setOptions(
{
   submitUrl: "${siteURL("my-tasks")}"
}).setMessages(
   ${messages}
);
//]]></script>


<div id="${el}-body" class="form-manager workflow-details-actions">
   <div class="actions hidden">
      <button id="${el}-cancel">${msg("button.cancelWorkflow")}</button>
   </div>
</div>
-->