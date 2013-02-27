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
 * Custom content types
 */
function getContentTypes() {
	// TODO: Data webscript call to return list of available types
	var contentTypes = [ {
		id : "akdm:instruction",
		value : "type.akdm_instruction"
	}, {
		id : "akdm:ordinance",
		value : "type.akdm_ordinance"
	}, {
		id : "akdm:manual",
		value : "type.akdm_manual"
	}, {
		id : "akdm:protocol",
		value : "type.akdm_protocol"
	}, {
		id : "akdm:generaldocument",
		value : "type.akdm_generaldocument"
	}, {
		id : "akdm:economydocument",
		value : "type.akdm_economydocument"
	}, {
		id : "akdm:issuelist",
		value : "type.akdm_issuelist"
	}, {
		id : "akdm:internalvalidation",
		value : "type.akdm_internalvalidation"
	}, {
		id : "akdm:listpul",
		value : "type.akdm_listpul"
	},{
		id : "akdm:byggredadocument",
		value : "type.akdm_byggredadocument"
	} ];

	return contentTypes;
}
model.contentTypes = getContentTypes();