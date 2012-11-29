package se.alingsas.alfresco.repo.utils.byggreda;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class ByggRedaDocumentTest {
	public static final String HEADER = "\"Film\";\"Löpnr\";\"Diarieår\";\"Dnr\";\"Fastbet\";\"TidFastbet\";\"Adress\";\"TidAdress\";\"Beslut\";\"Avser\";\"Ärendemening\";\"Anteckning\";\"Handlingar\";\"Filnamn \"";
	public static final String VALID_LINE = "\"10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.lmn\"";
	public static final String INVALID_LINE_START = "10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.lmn\"";
	public static final String INVALID_LINE_END = "\"10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.lmn";
	public static final String INVALID_LINE_MISSING = "\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.lmn\"";
	public static final String INVALID_LINE_EXTRA = "\"10\";\"10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.lmn\"";
	public static final String VALID_LINE_EMPTY = "";
	public static final String VALID_LINE_COMMENT = "#Comment";
	public static final String LINE_BREAK_1 = "\n";
	public static final String LINE_BREAK_2 = "\r\n";

	@Test
	public void testRead() {

		// Test valid document with just header
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(HEADER + LINE_BREAK_1);
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
					"UTF-8"));
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is);
			assertNotNull(read);
			assertEquals(0, read.size());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Test valid document with one line
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(HEADER + LINE_BREAK_1);
			sb.append(VALID_LINE + LINE_BREAK_1);
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
					"UTF-8"));
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is);
			assertNotNull(read);
			assertEquals(1, read.size());
			assertEquals(1, checkNoSuccessful(read));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Test valid document with several lines
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(HEADER + LINE_BREAK_1);
			sb.append(VALID_LINE + LINE_BREAK_1);
			sb.append(VALID_LINE + LINE_BREAK_1);
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
					"UTF-8"));
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is);
			assertNotNull(read);
			assertEquals(2, read.size());
			assertEquals(2, checkNoSuccessful(read));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Test valid document with several lines with other linebreak
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(HEADER + LINE_BREAK_2);
			sb.append(VALID_LINE + LINE_BREAK_2);
			sb.append(VALID_LINE + LINE_BREAK_2);
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
					"UTF-8"));
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is);
			assertNotNull(read);
			assertEquals(2, read.size());
			assertEquals(2, checkNoSuccessful(read));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// Test valid document with several lines
				try {
					StringBuilder sb = new StringBuilder();
					sb.append(HEADER + LINE_BREAK_1);
					sb.append(VALID_LINE + LINE_BREAK_1);
					sb.append(INVALID_LINE_START + LINE_BREAK_1);
					sb.append(INVALID_LINE_END + LINE_BREAK_1);
					sb.append(VALID_LINE_EMPTY + LINE_BREAK_1);
					sb.append(VALID_LINE_COMMENT + LINE_BREAK_1);
					sb.append(INVALID_LINE_MISSING + LINE_BREAK_1);
					sb.append(INVALID_LINE_EXTRA + LINE_BREAK_1);
					sb.append(VALID_LINE_EMPTY + LINE_BREAK_1);
					sb.append(VALID_LINE_EMPTY + LINE_BREAK_1);
					InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
							"UTF-8"));
					Set<ByggRedaDocument> read = ReadMetadataDocument.read(is);
					assertNotNull(read);
					assertEquals(5, read.size());
					assertEquals(1, checkNoSuccessful(read));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

	}

	private int checkNoSuccessful(Set<ByggRedaDocument> read) {
		Iterator<ByggRedaDocument> it = read.iterator();
		int result = 0;
		while (it.hasNext()) {
			ByggRedaDocument next = it.next();
			if (next.readSuccessfully) {
				result++;
			}
		}
		return result;
	}
}
