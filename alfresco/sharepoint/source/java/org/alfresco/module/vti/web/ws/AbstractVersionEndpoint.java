/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.vti.web.ws;

import java.util.List;

import org.alfresco.module.vti.handler.VersionsServiceHandler;
import org.alfresco.module.vti.metadata.model.DocumentVersionBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

/**
 * Parent class of Version Endpoints, which need to return
 *  a list of Versions for a Document.
 *
 * @author Nick Burch
 */
public abstract class AbstractVersionEndpoint extends AbstractEndpoint
{
    // xml namespace prefix
    private static String prefix = "versions";

    private static Log logger = LogFactory.getLog(AbstractVersionEndpoint.class);
   
    // handler that provides methods for operating with documents and folders
    protected VersionsServiceHandler handler;
    public AbstractVersionEndpoint(VersionsServiceHandler handler)
    {
        this.handler = handler;
    }

    /**
     * Does the version work, and returns the new version info 
     */
    protected abstract List<DocumentVersionBean> executeVersionAction(
          VtiSoapRequest soapRequest, String dws, String fileName, Element fileVersion) throws Exception;
    
    public void execute(VtiSoapRequest soapRequest, VtiSoapResponse soapResponse) throws Exception
    {
        if (logger.isDebugEnabled())
            logger.debug("Soap Method with name " + getName() + " is started.");
        
        // mapping xml namespace to prefix
        SimpleNamespaceContext nc = new SimpleNamespaceContext();
        nc.addNamespace(prefix, namespace);
        nc.addNamespace(soapUriPrefix, soapUri);
        
        String host = getHost(soapRequest);
        String context = soapRequest.getAlfrescoContextName();
        String dws = getDwsFromUri(soapRequest);        

        // getting fileName parameter from request
        XPath fileNameXPath = new Dom4jXPath(buildXPath(prefix, "/"+getName()+"/fileName"));
        fileNameXPath.setNamespaceContext(nc);
        String fileName = getFileName(soapRequest, fileNameXPath);

        // getting fileVersion parameter from request
        XPath fileVersionXPath = new Dom4jXPath(buildXPath(prefix, "/"+getName()+"/fileVersion"));
        fileVersionXPath.setNamespaceContext(nc);
        Element fileVersion = (Element) fileVersionXPath.selectSingleNode(soapRequest.getDocument().getRootElement());
        
        // Action the given file version
        List<DocumentVersionBean> versions = executeVersionAction(soapRequest, dws, fileName, fileVersion);
        
        // creating soap response
        Element root = soapResponse.getDocument().addElement(getName()+"Response", namespace);
        Element deleteVersionResult = root.addElement(getName()+"Result");

        Element results = deleteVersionResult.addElement("results", namespace);

        results.addElement("list").addAttribute("id", "");
        results.addElement("versioning").addAttribute("enabled", "1");
        results.addElement("settings").addAttribute("url", host + context + dws + "/documentDetails.vti?doc=" + dws + "/" + fileName);

        boolean isCurrent = true;
        for (DocumentVersionBean version : versions)
        {
            Element result = results.addElement("result");
            if (isCurrent)
            {
                // prefix @ means that it is current working version, it couldn't be restored or deleted
                result.addAttribute("version", "@" + version.getVersion());
                String url = host + context + dws + "/" + fileName.trim();
                result.addAttribute("url", url);
                isCurrent = false;
            }
            else
            {
                result.addAttribute("version", version.getVersion());
                String url = host + context + dws + version.getUrl();
                result.addAttribute("url", url);
            }
            
            result.addAttribute("created", version.getCreatedTime());
            result.addAttribute("createdBy", version.getCreatedBy());
            result.addAttribute("size", String.valueOf(version.getSize()));
            result.addAttribute("comments", version.getComments());
        }
        
        if (logger.isDebugEnabled()) {
           String versionsStr = "";
           for (DocumentVersionBean version : versions)
           {
               versionsStr += version.getVersion() + " ";
           }
           logger.debug("The folloving versions [ "+ versionsStr + "] were found");
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Soap Method with name " + getName() + " is finished.");
    }
}
