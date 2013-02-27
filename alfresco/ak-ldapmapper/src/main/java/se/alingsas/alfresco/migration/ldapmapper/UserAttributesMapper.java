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

package se.alingsas.alfresco.migration.ldapmapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class UserAttributesMapper implements AttributesMapper {
	 
    @Override
    public LotusUser mapFromAttributes(Attributes attributes) throws NamingException {
 
        LotusUser userObject = new LotusUser();
 
        String commonName = (String)attributes.get("cn").get();
        userObject.setCommonName(commonName);
        if (attributes.get("uid") == null){
            System.out.println("Uid is null for " + commonName);
        }else{
            String uid = attributes.get("uid").get().toString();
            userObject.setUid(uid);
        }
        return userObject;
    }

 
}