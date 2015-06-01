<#-- @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/site/sites.get.json.ftl -->
<#import "site.lib.ftl" as siteLib/>

[
  <#list sites?sort_by("title") as site>
    <@siteLib.siteJSONManagers site=site roles=roles>
    "isMemberOfGroup": ${site.isMemberOfGroup(person.properties.userName)?string},
    </@siteLib.siteJSONManagers>
    <#if site_has_next>,</#if>
  </#list>
]