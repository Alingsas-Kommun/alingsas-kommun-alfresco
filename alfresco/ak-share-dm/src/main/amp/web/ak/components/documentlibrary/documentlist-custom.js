/*
 * Copyright (C) 2012-2013 Alingsås Kommun
 *
 * This file is part of Alfresco customizations made for Alingsås Kommun
 *
 * The Alfresco customizations made for Alingsås Kommun is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Alfresco customizations made for Alingsås Kommun is distributed in the 
 * hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Alfresco customizations made for Alingsås Kommun. 
 * If not, see <http://www.gnu.org/licenses/>.
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
