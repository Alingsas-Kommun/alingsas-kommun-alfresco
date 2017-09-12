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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class ReadMetadataDocumentTest {
	public static final String HEADER = "\"Film\";\"Löpnr\";\"Diarieår\";\"Dnr\";\"Fastbet\";\"TidFastbet\";\"Adress\";\"TidAdress\";\"Beslut\";\"Avser\";\"Ärendemening\";\"Anteckning\";\"Handlingar\";\"Filnamn \"";
	public static final String VALID_LINE = "\"10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.pdf\"";
	public static final String INVALID_LINE_START = "10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.pdf\"";
	public static final String INVALID_LINE_END = "\"10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.pdf";
	public static final String INVALID_LINE_MISSING = "\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.pdf\"";
	public static final String INVALID_LINE_EXTRA = "\"10\";\"10\";\"123\";\"2012\";\"0012\";\"Abc\";\"Bcd\";\"Cde 12\";\"Def 11\";\"Efg\";\"Fgh\";\"Ghi\";\"Hij\";\"2011.0001\";\"ijk.pdf\"";
	public static final String VALID_LINE_EMPTY = "";
	public static final String VALID_LINE_COMMENT = "#Comment";
	public static final String LINE_BREAK_1 = "\n";
	public static final String LINE_BREAK_2 = "\r\n";

	@Test
	public void testRead() throws IOException {

		// Test valid document with just header
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(HEADER + LINE_BREAK_1);
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
					"UTF-8"));
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is, new ArrayList<String>());
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
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is, new ArrayList<String>());
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
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is, new ArrayList<String>());
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
			Set<ByggRedaDocument> read = ReadMetadataDocument.read(is, new ArrayList<String>());
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
					//sb.append(INVALID_LINE_START + LINE_BREAK_1);
					//sb.append(INVALID_LINE_END + LINE_BREAK_1);
					sb.append(VALID_LINE_EMPTY + LINE_BREAK_1);
					sb.append(VALID_LINE_COMMENT + LINE_BREAK_1);
					sb.append(INVALID_LINE_MISSING + LINE_BREAK_1);
					sb.append(INVALID_LINE_EXTRA + LINE_BREAK_1);
					sb.append(VALID_LINE_EMPTY + LINE_BREAK_1);
					sb.append(VALID_LINE_EMPTY + LINE_BREAK_1);
					InputStream is = new ByteArrayInputStream(sb.toString().getBytes(
							"UTF-8"));
					Set<ByggRedaDocument> read = ReadMetadataDocument.read(is, new ArrayList<String>());
					assertNotNull(read);
					assertEquals(3, read.size());
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
			if (next.isReadSuccessfully()) {
				result++;
			}
		}
		return result;
	}
}
