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
      this.metadataBannerViewName = "newdetailed";
      this.metadataLineViewName = "newdetailed";
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
})();
