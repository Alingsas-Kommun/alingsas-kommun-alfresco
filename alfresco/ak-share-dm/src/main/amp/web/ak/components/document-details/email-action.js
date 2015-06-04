
(function(_getActionUrls) {
  //Alfresco.doclib.Actions.prototype.getActionUrls = function (record, siteId) {
  Alfresco.DocumentActions.prototype.getActionUrls = function (record, siteId) {
    var actionUrls = _getActionUrls.call(this, record, siteId);

    var jsNode = record.jsNode,
      nodeRef = jsNode.isLink ? jsNode.linkedNode.nodeRef : jsNode.nodeRef,
      nodeRef = jsNode.isLink && !$isValueSet(nodeRef) ? "invalidlink" : nodeRef,
      strNodeRef = nodeRef.toString(),
      nodeRefUri = nodeRef.uri,
      contentUrl = jsNode.contentURL,
      workingCopy = record.workingCopy || {},
      recordSiteId = Alfresco.util.isValueSet(record.location.site) ? record.location.site.name : null,
      fnPageURL = Alfresco.util.bind(function(page)
      {
         return Alfresco.util.siteURL(page,
         {
            site: YAHOO.lang.isString(siteId) ? siteId : recordSiteId
         });
      }, this);

    var currentBrowserBaseUrl = window.location.protocol + "//" +window.location.host;    
    var currentMailToLink = "mailto:?subject=" + encodeURIComponent(jsNode.properties.cm_name) + "&amp;body=" + encodeURIComponent(Alfresco.util.combinePaths(currentBrowserBaseUrl, fnPageURL("document-details?nodeRef=" + strNodeRef)));
    actionUrls.mailtoLink = currentMailToLink;
    return actionUrls;
  };
}(Alfresco.doclib.Actions.prototype.getActionUrls))