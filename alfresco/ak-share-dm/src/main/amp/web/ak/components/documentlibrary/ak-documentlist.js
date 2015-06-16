(function(renderCellDescription) {

   Alfresco.DocumentListViewRenderer.prototype.renderCellDescription = function(scope, elCell, oRecord, oColumn, oData) {

      var record = oRecord.getData();

      if (record.jsNode.properties.title !== null && record.jsNode.properties.title !== undefined && record.jsNode.properties.title !== "") {
         var tmp = record.displayName;
         record.displayName = record.jsNode.properties.title;
         record.jsNode.properties.title = tmp;
      }

      renderCellDescription.call(this, scope, elCell, oRecord, oColumn, oData);
   };

}(Alfresco.DocumentListViewRenderer.prototype.renderCellDescription));
