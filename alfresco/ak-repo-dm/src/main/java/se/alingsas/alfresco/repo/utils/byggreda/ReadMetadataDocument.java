package se.alingsas.alfresco.repo.utils.byggreda;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import se.alingsas.alfresco.repo.utils.CommonFileUtil;

public class ReadMetadataDocument {
	private static final Logger LOG = Logger
			.getLogger(ReadMetadataDocument.class);
	private static final int EXPECTED_NUM_PARTS = 14;
	private static final String RECORD_DISPLAY_SEPARATOR = "-";

	/**
	 * Takes an input stream which should point to a metadata document for
	 * byggreda. Validates and parses the data and returns a set of
	 * ByggRedaDocument
	 * 
	 * @param inputStream
	 * @return
	 */
	public static Set<ByggRedaDocument> read(final InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}
		Set<ByggRedaDocument> result = new HashSet<ByggRedaDocument>();
		LineIterator lineIterator = null;
		int lineNumber = 1;
		try {
			lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			// Skip first line which is a header line
			if (lineIterator.hasNext()) {

				final String line = lineIterator.nextLine();
				if (!line.startsWith("\"Film\";\"")) {
					LOG.error("No header found on the first line in the document. First line was: "
							+ line +". Aborting...");
					return result;
				} else {
					LOG.debug("Line #"+lineNumber+": Skipping header");
				}
			}
			while (lineIterator.hasNext()) {
				lineNumber++;
				final String line = lineIterator.nextLine();
				// if it's an empty line or a comment, skip
				if (!StringUtils.hasText(line) || line.startsWith("#")) {
					LOG.info("Line #"+lineNumber+": Skipping comment or empty line");
					continue;
				}
				// Validation and error handling
				ByggRedaDocument document = parseAndValidate(line);
				document.lineNumber = lineNumber;
				if (!document.readSuccessfully) {
					// An error occured, we need to log this
					LOG.error("Line #"+document.lineNumber+": "+document.errorMsg);
				} else {
					// Document successfully read
					LOG.info("Line #"+document.lineNumber+": "+"Successfully read record. Serial number: "
							+ document.serialNumber + ", Record number: "
							+ document.recordNumber);
				}
				result.add(document);
			}
		} catch (final Exception ex) {
			throw new RuntimeException("Error on line '" + lineNumber + "'", ex);
		}  finally {
			IOUtils.closeQuietly(inputStream);
			LineIterator.closeQuietly(lineIterator);
		}
		return result;
	}

	/**
	 * Parse and validate a single line in a metadata document for ByggReda
	 * 
	 * @param line
	 * @return
	 */
	private static ByggRedaDocument parseAndValidate(String line) {
		ByggRedaDocument document = new ByggRedaDocument();
		final String[] parts = StringUtils.delimitedListToStringArray(line,
				"\";\"");
		document.readSuccessfully = false;
		// Verify that all parts were extracted
		if (parts.length != 14) {
			document.errorMsg = "Extracting data from line returned "
					+ parts.length + " parts, expected " + EXPECTED_NUM_PARTS
					+ ". Line contents: " + line;
		} else if (parts[0].indexOf("\"") != 0) {
			document.errorMsg = "Expected \" as first character of the line. Line contents: "
					+ line;
		} else if (parts[parts.length - 1].indexOf("\"") != parts[parts.length - 1]
				.length() - 1) {
			document.errorMsg = "Expected \" as last character of the line. Line contents: "
					+ line;
		} else {
			// All parts were extracted successfully
			int i = 0;
			document.readSuccessfully = true;
			document.film = parts[i++].substring(1); // Remove prefix "
			document.serialNumber = parts[i++];
			document.recordYear = Integer.parseInt(parts[i++]);
			document.recordNumber =  parts[i++];
			document.recordDisplay = document.recordYear +RECORD_DISPLAY_SEPARATOR+ document.recordNumber; 
			document.buildingDescription = parts[i++];
			document.lastBuildingDescription = parts[i++];
			document.address = parts[i++];
			document.lastAddress = parts[i++];
			document.decision = parts[i++];
			document.forA = parts[i++];
			document.issuePurpose = parts[i++];
			document.note = parts[i++];
			document.records = parts[i++];
			// Remove postfix " from last part
			document.fileName = parts[i].substring(0, parts[i++].length() - 1);
			document.mimetype = CommonFileUtil.getMimetypeByExtension(FilenameUtils.getExtension(document.fileName));
			
			document.path = document.buildingDescription.substring(0, 1).toUpperCase().replace("/", "_").replace(':', '_')
			+ "/" + document.buildingDescription.toUpperCase().replace("/", "_").replace(':', '_') + "/"+
			document.recordDisplay + " "+ document.issuePurpose.toUpperCase().replace("/", "_").replace(':', '_');
			document.title = document.recordDisplay+ " " + document.issuePurpose.replace("/", "_").replace(':', '_');
		}
		return document;
	}
}
