<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/repository/site/sites.post.json.js">
//@overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/site/sites.post.json.js
function alingsas_extension() {
  var site = model.site;
  var props = new Array();
  var folderTemplate = null;
  if(json.has("siteFolderTemplate") && site){
    folderTemplate = json.get("siteFolderTemplate");
    if(folderTemplate != ""){
      props["alingsas:siteFolderTemplate"] = folderTemplate;
      site.node.addAspect("alingsas:siteFolderTemplateAspect",props);
      site.node.save();
      site.save();
      model.site = site;
    }
  }
}

// First main() will run in sites.post.json.js. This creates the site. 

alingsas_extension();
