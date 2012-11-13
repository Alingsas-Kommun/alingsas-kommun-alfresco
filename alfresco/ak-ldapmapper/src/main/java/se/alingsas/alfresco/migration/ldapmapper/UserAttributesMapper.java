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