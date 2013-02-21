package se.alingsas.alfresco.share.evaluator.documentlibrary.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.connector.User;

/**
 * Validate that the user is an admin user
 * @author Marcus Svensson - Redpill Linpro AB
 * 
 */
public class IsAdmin extends BaseEvaluator {
;

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
			RequestContext rc = ThreadLocalRequestContext.getRequestContext();
			User user = rc.getUser();			

			return (user != null && user.isAdmin());
		} catch (Exception err) {
			throw new AlfrescoRuntimeException(
					"Exception while running action evaluator: "
							+ err.getMessage());
		}
	}

}