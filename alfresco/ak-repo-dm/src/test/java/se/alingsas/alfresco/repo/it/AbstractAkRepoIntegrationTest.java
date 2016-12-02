package se.alingsas.alfresco.repo.it;

import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Marcus Svartmark - Redpill Linpro AB
 */
@ContextConfiguration({ "classpath:alfresco/application-context.xml" , "classpath:alfresco/web-scripts-application-context.xml"})
public abstract class AbstractAkRepoIntegrationTest extends AbstractRepoIntegrationTest {
  
}
