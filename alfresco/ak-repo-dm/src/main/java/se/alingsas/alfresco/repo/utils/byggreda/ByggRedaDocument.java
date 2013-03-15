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

package se.alingsas.alfresco.repo.utils.byggreda;

import org.alfresco.service.cmr.repository.NodeRef;

public class ByggRedaDocument {
	public boolean readSuccessfully = false;
	public String statusMsg = "";
	public NodeRef nodeRef = null;
	public int lineNumber;
	public String mimetype = "";
	
	public String film = "";
	public String serialNumber = "";
	public Integer recordYear = 0;
	public String recordNumber = "";
	public String recordDisplay = "";	
	public String buildingDescription = "";
	public String lastBuildingDescription = "";
	public String address = "";
	public String lastAddress = "";
	public String decision = "";
	public String forA= "";
	public String issuePurpose = "";
	public String note = "";
	public String records = "";
	public String fileName = "";
	
	public String path = "";
	public String title = "";
	public String originalPath = "";
	
}
