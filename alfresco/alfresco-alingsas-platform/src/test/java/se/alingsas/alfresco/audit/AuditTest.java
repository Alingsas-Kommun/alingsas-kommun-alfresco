package se.alingsas.alfresco.audit;

import java.net.URLEncoder;

import org.alfresco.util.ISO9075;
import org.junit.Test;

public class AuditTest {

  @Test
  public void test() {
    System.out
        .println(ISO9075
            .decode("Årsredovisning_x0020_tekniska_x0020_nämnden_x0020_2015.docx"));
    
    System.out.println(URLEncoder.encode("Årsredovisning tekniska nämnden 2015.docx"));
    System.out.println(URLEncoder.encode("Årsredovisning tekniska nämnden 2015 (Working Copy).docx"));
    System.out.println(URLEncoder.encode("Årsredovisning tekniska nämnden 2015 (Working copy).docx"));
    System.out.println(URLEncoder.encode("Årsredovisning tekniska nämnden 2015 (Arbetskopia).docx"));
  }
}
