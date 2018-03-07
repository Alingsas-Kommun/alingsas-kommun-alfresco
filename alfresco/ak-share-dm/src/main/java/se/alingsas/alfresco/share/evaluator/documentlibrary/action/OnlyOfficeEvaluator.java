/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.alingsas.alfresco.share.evaluator.documentlibrary.action;

import java.util.Map;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

/**
 *
 * @author jimmie
 */
public class OnlyOfficeEvaluator extends BaseEvaluator {

  @Override
  public boolean evaluate(JSONObject jsonObject) {

    RequestContext context = ThreadLocalRequestContext.getRequestContext();
    Map<String, String> uriTokens = context.getUriTokens();
    String nodeRef = uriTokens.get("nodeRef");
    if (nodeRef == null) {
      nodeRef = context.getParameter("nodeRef");
    }
    if (nodeRef == null) {
      return false;
    }

    try {
      final Connector conn = context.getServiceRegistry().getConnectorService().getConnector("alfresco", context.getUserId(), ServletUtil.getSession());

      final Response response = conn.call("/alingsas/user/sitehasaspect?nodeRef=" + nodeRef + "&aspect=onlyofficeAspect");

      String text = response.getText();
      Object object = new JSONParser().parse(text);
      JSONObject jsonObj = (JSONObject) object;
      boolean hasaspect = (boolean) jsonObj.get("hasaspect");
      return hasaspect;

    } catch (Exception e) {
    }
    return false;
  }
}
