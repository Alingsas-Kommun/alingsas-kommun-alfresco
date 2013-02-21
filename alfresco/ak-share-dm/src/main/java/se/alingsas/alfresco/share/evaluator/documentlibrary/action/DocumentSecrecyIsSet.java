package se.alingsas.alfresco.share.evaluator.documentlibrary.action;

/**
 * @author Marcus Svensson - Redpill Linpro AB
 *
 */

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.springframework.util.StringUtils;

/**
 * Evaluator to determine if a document is completed or not
 * 
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class DocumentSecrecyIsSet extends BaseEvaluator {

	private static final String DOC_SECRECY_PROPERTY = "akdm:documentSecrecy";

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
					DOC_SECRECY_PROPERTY);

			if (StringUtils.hasText(property)) {
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
