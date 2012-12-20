package se.alingsas.alfresco.migration.ldapmapper;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.LdapTemplate;

public class LdapMapperTest {

	@Test
	public void testGetUser() {
		ApplicationContext context = new ClassPathXmlApplicationContext("ldap-context.xml");
		LdapMapper ldapMapper = new LdapMapper("", "");
		ldapMapper.setLdapTemplate(context.getBean("ldapTemplate", LdapTemplate.class));
		LotusUser user = ldapMapper
				.getUser("Eva Karlsson", "OU=KLK,O=Alingsas,DC=nodomain");
		assertNotNull(user);
		user = ldapMapper
				.getUser("Malin Wallin", "OU=KLK,O=Alingsas,DC=nodomain");
		assertNotNull(user);
	}

}
