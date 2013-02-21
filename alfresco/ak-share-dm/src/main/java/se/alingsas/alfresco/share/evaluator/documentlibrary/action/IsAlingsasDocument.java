package se.alingsas.alfresco.share.evaluator.documentlibrary.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Validate that a document is of the correct type, all Alingsås types have the akdm:commonAspect applied to them
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class IsAlingsasDocument extends BaseEvaluator {

	private static final String ASPECT_AK_COMMON = "akdm:commonAspect";

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
			JSONArray nodeAspects = getNodeAspects(jsonObject);

			if (nodeAspects == null) {
				return false;
			} else {
				if (nodeAspects.contains(ASPECT_AK_COMMON)) {
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception err) {
			throw new AlfrescoRuntimeException(
					"JSONException whilst running action evaluator: "
							+ err.getMessage());
		}
	}

}