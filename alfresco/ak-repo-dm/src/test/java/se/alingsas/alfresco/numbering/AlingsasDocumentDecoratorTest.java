package se.alingsas.alfresco.repo.numbering;

import java.text.SimpleDateFormat;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Marcus Svartmark - Redpill Linpro AB
 */
public class AlingsasDocumentDecoratorTest {

  @Test
  public void testAlingsasDecorator() {
    AlingsasDocumentDecorator decorator = new AlingsasDocumentDecorator();
    decorator.setDatePattern("yyyy-MM-dd");
    decorator.setNumberSeparator("-");
    decorator.setZeroPadding(6);
    decorator.setPrefixSeparator("-");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String decorate = decorator.decorate(1, null);
    assertEquals(sdf.format(new Date()) + "-000-001", decorate);
    
    decorate = decorator.decorate(123, null);
    assertEquals(sdf.format(new Date()) + "-000-123", decorate);
    
    decorate = decorator.decorate(1234, null);
    assertEquals(sdf.format(new Date()) + "-001-234", decorate);
    
    decorate = decorator.decorate(12345, null);
    assertEquals(sdf.format(new Date()) + "-012-345", decorate);
    
    decorate = decorator.decorate(123456, null);
    assertEquals(sdf.format(new Date()) + "-123-456", decorate);
    
    decorate = decorator.decorate(1234567, null);
    assertEquals(sdf.format(new Date()) + "-123-4567", decorate);
    
    decorate = decorator.decorate(12345678, null);
    assertEquals(sdf.format(new Date()) + "-1234-5678", decorate);
    
    decorate = decorator.decorate(123456789, null);
    assertEquals(sdf.format(new Date()) + "-1234-56789", decorate);
    
    decorate = decorator.decorate(1123456789, null);
    assertEquals(sdf.format(new Date()) + "-11234-56789", decorate);
  }
}
