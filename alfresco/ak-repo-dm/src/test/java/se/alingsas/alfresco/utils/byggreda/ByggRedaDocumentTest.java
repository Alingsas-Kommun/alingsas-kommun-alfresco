package se.alingsas.alfresco.repo.utils.byggreda;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author mars
 */
public class ByggRedaDocumentTest {

  @Test
  public void recordNumberPaddingTest() {
    ByggRedaDocument doc = new ByggRedaDocument();
    doc.setRecordNumber("5959");
    assertEquals("5959", doc.getRecordNumber());

    doc.setRecordNumber("959");
    assertEquals("0959", doc.getRecordNumber());

    doc.setRecordNumber("59");
    assertEquals("0059", doc.getRecordNumber());

    doc.setRecordNumber("9");
    assertEquals("0009", doc.getRecordNumber());

    doc.setRecordNumber("");
    assertEquals("0000", doc.getRecordNumber());
  }
}
