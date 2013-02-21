package se.alingsas.alfresco.share.evaluator.documentlibrary.action;

/**
 * @author Marcus Svensson - Redpill Linpro AB
 *
 */

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

/**
 * Evaluator to determine if a document is completed or not
 * 
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class DocumentStatusIsComplete extends BaseEvaluator {

	private static final String DOC_STATUS_PROPERTY = "akdm:documentStatus";
	private static final String DOC_STATUS_COMPLETE_VALUE = "Färdigt dokument";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.web.evaluator.BaseEvaluator#evaluate(org.json.simple.JSONObject
	 * )
	 */
	@Override
	public boolean evaluate(JSONObject jsonObject) {
		try {
			String property = (String) getProperty(jsonObject,
					DOC_STATUS_PROPERTY);

			if (DOC_STATUS_COMPLETE_VALUE.equalsIgnoreCase(property)) {
				return true;
			}
			return false;
		} catch (Exception err) {
			throw new AlfrescoRuntimeException(
					"JSONException whilst running action evaluator: "
							+ err.getMessage());
		}
	}

}
