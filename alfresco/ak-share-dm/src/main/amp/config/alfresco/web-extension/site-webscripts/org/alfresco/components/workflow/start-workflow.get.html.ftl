<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/workflow/start-workflow.get.html.ftl -->
<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new Alfresco.component.StartWorkflow("${args.htmlid?js_string}").setOptions(
   {
      failureMessage: "message.failure",
      submitButtonMessageKey: "button.startWorkflow",
      defaultUrl: "${siteURL("my-tasks")?js_string}",
      selectedItems: "${(page.url.args.selectedItems!"")?js_string}",
      destination: "${(page.url.args.destination!"")?js_string}",
      startWorkflow: "${(page.url.args.startWorkflow!"")?js_string}",
      workflowDefinitions:
      [<#list workflowDefinitions as workflowDefinition>
      	<#if page.url.args.startWorkflow == workflowDefinition.name>
         {
            name: "${(workflowDefinition.name!"")?js_string}",
            title: "${(workflowDefinition.title!"")?js_string}",
            description: "${(workflowDefinition.description!"")?replace("\\n", "\\\\n")?js_string}"
         }<#if workflowDefinition_has_next>,</#if>
         </#if>
      </#list>]
   }).setMessages(
      ${messages}
   );
//]]></script>
<div id="${el}-body" class="form-manager start-workflow">
   <h1>${msg("header")}:  <#list workflowDefinitions as workflowDefinition>
               <#if page.url.args.startWorkflow == workflowDefinition.name>
               
                  ${workflowDefinition.title!workflowDefinition.id?html}

               </#if>
               </#list> </h1>
   <div>
      
      <#-- Workflow type menu button  -->
      <span class="selected-form-button" style="visibility:hidden">
         <span id="${el}-workflow-definition-button" class="yui-button yui-menu-button">
            <span class="first-child">
               <button type="button" tabindex="0"></button>
            </span>
         </span>
      </span>
      <#-- Workflow type menu -->
      <div id="${el}-workflow-definition-menu" class="yuimenu" style="visibility:hidden">
         <div class="bd">
            <ul>
               <#list workflowDefinitions as workflowDefinition>
               <#if page.url.args.startWorkflow == workflowDefinition.name>
               <li>
                  <span class="title" tabindex="0">${workflowDefinition.title!workflowDefinition.id?html}</span>
                  <span class="description">${(workflowDefinition.description!"")?html}</span>
               </li>
               </#if>
               </#list>
            </ul>
         </div>
      </div>
   </div>
</div>
<div id="${el}-workflowFormContainer"></div>
