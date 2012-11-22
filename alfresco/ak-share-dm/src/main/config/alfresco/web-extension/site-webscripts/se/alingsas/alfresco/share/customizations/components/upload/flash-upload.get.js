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
	} ];

	return contentTypes;
}
model.contentTypes = getContentTypes();