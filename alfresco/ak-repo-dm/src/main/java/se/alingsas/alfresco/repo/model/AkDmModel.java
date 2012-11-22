package se.alingsas.alfresco.repo.model;

import org.alfresco.service.namespace.QName;

public interface AkDmModel {
	/**
	 * Namespaces
	 */
	public static final String AKDM_URI = "http://www.alingsas.se/alfresco/model/dm/1.0";

	/**
	 * Types
	 */
	public static final QName TYPE_AKDM_DOCUMENT = QName.createQName(AKDM_URI,
			"document");
	public static final QName TYPE_AKDM_INSTRUCTION = QName.createQName(
			AKDM_URI, "instruction");
	public static final QName TYPE_AKDM_ORDINANCE = QName.createQName(AKDM_URI,
			"ordinance");
	public static final QName TYPE_AKDM_MANUAL = QName.createQName(AKDM_URI,
			"manual");
	public static final QName TYPE_AKDM_PROTOCOL = QName.createQName(AKDM_URI,
			"protocol");
	public static final QName TYPE_AKDM_GENERAL_DOC = QName.createQName(
			AKDM_URI, "generaldocument");
	public static final QName TYPE_AKDM_ECONOMY_DOC = QName.createQName(
			AKDM_URI, "economydocument");
	public static final QName TYPE_AKDM_FOLDER = QName.createQName(AKDM_URI,
			"folder");

	/**
	 * Aspects
	 */
	public static final QName ASPECT_AKDM = QName.createQName(AKDM_URI,
			"aspect");

	public static final QName ASPECT_AKDM_COMMON = QName.createQName(AKDM_URI,
			"commonAspect");
	public static final QName PROP_AKDM_DOC_NUMBER = QName.createQName(
			AKDM_URI, "documentNumber");
	public static final QName PROP_AKDM_DOC_STATUS = QName.createQName(
			AKDM_URI, "documentStatus");
	public static final QName PROP_AKDM_DOC_SECRECY = QName.createQName(
			AKDM_URI, "documentSecrecy");

	public static final QName ASPECT_AKDM_INSTRUCTION = QName.createQName(
			AKDM_URI, "instructionAspect");
	public static final QName PROP_AKDM_INSTRUCTION_DECISION_DATE = QName
			.createQName(AKDM_URI, "instructionDecisionDate");
	public static final QName PROP_AKDM_INSTRUCTION_GROUP = QName.createQName(
			AKDM_URI, "instructionGroup");

	public static final QName ASPECT_AKDM_MANUAL = QName.createQName(AKDM_URI,
			"manualAspect");
	public static final QName PROP_AKDM_MANUAL_FUNCTION = QName.createQName(
			AKDM_URI, "manualFunction");
	public static final QName PROP_AKDM_MANUAL_MANUAL = QName.createQName(
			AKDM_URI, "manualManual");

	public static final QName ASPECT_AKDM_PROTOCOL = QName.createQName(
			AKDM_URI, "protocolAspect");
	public static final QName PROP_AKDM_PROTOCOL_ID = QName.createQName(
			AKDM_URI, "protocolId");

	public static final QName ASPECT_AKDM_GENERAL_DOC = QName.createQName(
			AKDM_URI, "generalDocumentAspect");
	public static final QName PROP_AKDM_GENERAL_DOCUMENT_DESCRIPTION = QName
			.createQName(AKDM_URI, "generalDocumentDescription");
	public static final QName PROP_AKDM_GENERAL_DOCUMENT_DATE = QName
			.createQName(AKDM_URI, "generalDocumentDate");

}
