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

package se.alingsas.alfresco.repo.scripts.ddmigration;

import java.io.File;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class AlingsasDocument {

	// General properties
	public String documentNumber;
	public String createdBy;
	public Date createdDate;
	public String fileName;
	public String title;
	public String documentType;
	public String documentStatus;
	public String description;
	public String secrecy;
	public String version;

	// Anvisning properties
	public String protocolId;
	public String decisionDate;
	public String group;

	// Författning properties
	// public String protocolId;
	// public String decisionDate;
	// public String group;

	// Handbok properties
	public String function;
	public String handbook;

	// General document
	public Date generalDocumentDate;
	public String generalDocumentDescription;

	// Protokoll properties
	// public String protocolId;

	// Internal migration properties
	public int lineNumber;
	public QName documentTypeQName;
	public File file;
	public String mimetype;
	public String filePath;
	public NodeRef documentNodeRef;
	public String ddUUID;
	public String fileExtension;
	public String tmpStorageFolder;
	public Date meetingDate;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof AlingsasDocument)) {
			return false;
		}

		final AlingsasDocument that = (AlingsasDocument) object;

		final EqualsBuilder builder = new EqualsBuilder();

		builder.append(this.documentNumber.toLowerCase(),
				that.documentNumber.toLowerCase());
		builder.append(this.version, that.version);

		return builder.isEquals();
	}

	@Override
	public int hashCode() {
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(documentNumber.toLowerCase());
		builder.append(version);

		return builder.hashCode();
	}

}
