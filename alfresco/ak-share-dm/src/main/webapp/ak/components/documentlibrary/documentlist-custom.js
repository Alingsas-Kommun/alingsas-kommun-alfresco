/**
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is part of an unsupported extension to Alfresco.
 */

/**
 * @module DocumentLibrary
 */

/**
 * New detailed view extension of DocumentListViewRenderer component.
 * 
 * @namespace Alfresco
 * @class Alfresco.DocumentListNewDetailedViewRenderer
 * @extends Alfresco.DocumentListViewRenderer
 */
(function()
{
   /**
	 * YUI Library aliases
	 */
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       Anim = YAHOO.util.Anim;
   
   /**
	 * Alfresco Slingshot aliases
	 */
   var $html = Alfresco.util.encodeHTML;
   
   /**
	 * LargeViewRenderer constructor.
	 * 
	 * @param name
	 *            {String} The name of the LargeViewRenderer
	 * @return {Alfresco.DocumentListNewDetailedViewRenderer} The new
	 *         LargeViewRenderer instance
	 * @constructor
	 */
   Alfresco.DocumentListNewDetailedViewRenderer = function(name)
   {
      Alfresco.DocumentListNewDetailedViewRenderer.superclass.constructor.call(this, name);
      // Defaults to large but we'll copy the metadata from detailed view
      // this.metadataBannerViewName = "detailed";
      // this.metadataLineViewName = "detailed";
      // this.thumbnailColumnWidth = 200;
      return this;
   };
   
   /**
	 * Extend from Alfresco.DocumentListViewRenderer
	 */
   YAHOO.extend(Alfresco.DocumentListNewDetailedViewRenderer, Alfresco.DocumentListViewRenderer);
   
   // record.displayName
   // oRecord.setData("displayName"
   // record.jsNode.properties.title
   
   Alfresco.DocumentListNewDetailedViewRenderer.prototype.renderCellDescription = function NewDetailed_renderCellDescription(scope, elCell, oRecord, oColumn, oData) {
	   var desc = "", i, j,
       record = oRecord.getData(),
       jsNode = record.jsNode,
       properties = jsNode.properties,
       isContainer = jsNode.isContainer,
       isLink = jsNode.isLink,
       title = "",
       titleHTML = "",
       version = "",
       canComment = jsNode.permissions.user.CreateChildren;
	   if (jsNode.properties.title && jsNode.properties.title!="") {
		   oRecord.setData("displayName", jsNode.properties.title);
		   jsNode.properties.title = jsNode.properties.name;
   	   }
	   Alfresco.DocumentListViewRenderer.prototype.renderCellDescription(scope, elCell, oRecord, oColumn, oData);
   }
   
   /*
	 * Alfresco.DocumentListNewDetailedViewRenderer.prototype.renderCellThumbnail =
	 * function Example_renderCellThumbnail(scope, elCell, oRecord, oColumn,
	 * oData, imgIdSuffix) { var record = oRecord.getData(), node =
	 * record.jsNode, properties = node.properties, name = record.displayName,
	 * isContainer = node.isContainer, isLink = node.isLink, extn =
	 * name.substring(name.lastIndexOf(".")), imgId = node.nodeRef.nodeRef; //
	 * DD added
	 * 
	 * var containerTarget; // This will only get set if thumbnail represents a
	 * container
	 * 
	 * oColumn.width = this.thumbnailColumnWidth; Dom.setStyle(elCell, "width",
	 * oColumn.width + "px"); Dom.setStyle(elCell.parentNode, "width",
	 * oColumn.width + "px");
	 * 
	 * if (isContainer || (isLink && node.linkedNode.isContainer)) {
	 * elCell.innerHTML = '<span class="folder documents-large">' + (isLink ? '<span
	 * class="link"></span>' : '') + (scope.dragAndDropEnabled ? '<span
	 * class="droppable"></span>' : '') +
	 * Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img
	 * id="' + imgId + '" src="' + Alfresco.constants.URL_RESCONTEXT +
	 * 'components/documentlibrary/images/folder-64.png" /></a>';
	 * containerTarget = new YAHOO.util.DDTarget(imgId); // Make the folder a
	 * target } else { elCell.innerHTML = '<span class="thumbnail
	 * documents-large">' + (isLink ? '<span class="link"></span>' : '') +
	 * Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img
	 * id="' + imgId + '" src="' +
	 * Alfresco.DocumentList.generateThumbnailUrl(record, 'imgpreview') + '"
	 * alt="' + extn + '" title="' + $html(name) + '" /></a></span>'; } var
	 * dnd = new Alfresco.DnD(imgId, scope); };
	 */

})();
