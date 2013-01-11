package se.alingsas.alfresco.repo.utils.byggreda;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
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
	public static Set<ByggRedaDocument> read(final InputStream inputStream, List<String> globalMessages) {
		if (inputStream == null) {
			return null;
		}
		Set<ByggRedaDocument> result = new HashSet<ByggRedaDocument>();
		LineIterator lineIterator = null;
		String line = "";
		int lineNumber = 1;
		try {
			lineIterator = IOUtils.lineIterator(inputStream, "ISO-8859-1");
			// Skip first line which is a header line
			if (lineIterator.hasNext()) {

				line = lineIterator.nextLine();
				if (!line.startsWith("\"Film\";\"") && !line.startsWith("Film;")) {
					globalMessages.add("#1: Sidhuvud ej funnet på första raden i styrfilen. Första raden var: "+line);
					LOG.error("No header found on the first line in the document. First line was: "
							+ line +". Aborting...");
					return result;
				} else {
					LOG.debug("Line #"+lineNumber+": Skipping header");
				}
			}
			while (lineIterator.hasNext()) {
				lineNumber++;
				line = lineIterator.nextLine();
				// if it's an empty line or a comment, skip
				if (!StringUtils.hasText(line) || line.startsWith("#")) {
					globalMessages.add("#"+lineNumber+": Tom rad, eller bortkommenterad rad funnel, skippas");
					LOG.info("Line #"+lineNumber+": Skipping comment or empty line");
					continue;
				}
				// Validation and error handling
				ByggRedaDocument document = parseAndValidate(line);
				document.lineNumber = lineNumber;
				if (!document.readSuccessfully) {
					// An error occured, we need to log this
					LOG.error("Line #"+document.lineNumber+": "+document.statusMsg);
				} else {
					// Document successfully read
					LOG.debug("Line #"+document.lineNumber+": "+"Successfully read record. , Record number: "
							+ document.recordDisplay);
				}
				result.add(document);
			}
		} catch (final Exception ex) {
			globalMessages.add("#"+lineNumber+ ": Fel vid inläsning av rad "+lineNumber+" från styrfil. Radens innehåll: "+line+" Systemmeddelande: "+ex.getMessage());
			LOG.error("Error on line '" + lineNumber + "'. Line contents: "+line, ex);
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
		String[] parts = StringUtils.delimitedListToStringArray(line,
				"\";\"");
		if (parts.length <= 1) {
			//If parts were not found, try without quotes
			parts = StringUtils.delimitedListToStringArray(line, ";");
		}
		document.readSuccessfully = false;
		
		for (String part : parts) {
			if (part.indexOf(";")!=-1) {
				document.statusMsg = "Semikolon påträffades i något av fälten på raden";
				return document;
			}
		}
		
		// Verify that all parts were extracted
		if (parts.length != 14) {
			document.statusMsg = "Fel antal delar i rad. Antal delar funna:"
					+ parts.length + " Förväntat antal delar: " + EXPECTED_NUM_PARTS
					+ " Rad: " + line;
		} else {
			// All parts were extracted successfully
			int i = 0;
			document.readSuccessfully = true;
			document.film = parts[i++]; 
			if (document.film.indexOf("\"")==0) {
				// Remove prefix "
				document.film = document.film.substring(1);
			}				
			document.serialNumber = parts[i++];
			try {
				document.recordYear = Integer.parseInt(parts[i++]);
			} catch (NumberFormatException e) {
				document.readSuccessfully = false;
				document.statusMsg = "Ogiltigt diarieår";
				return document;
			}
			document.recordNumber =  parts[i++];
			if (!StringUtils.hasText(document.recordNumber)) {
				document.readSuccessfully = false;
				document.statusMsg = "Diarienummer saknas";
				return document;
			}
			document.recordDisplay = document.recordYear +RECORD_DISPLAY_SEPARATOR+ document.recordNumber; 
			document.buildingDescription = parts[i++];
			if (!StringUtils.hasText(document.buildingDescription)) {
				document.readSuccessfully = false;
				document.statusMsg = "Fastighetsbeteckning saknas";
				return document;
			}
			document.lastBuildingDescription = parts[i++];
			document.address = parts[i++];
			document.lastAddress = parts[i++];
			document.decision = parts[i++];
			document.forA = parts[i++];
			document.issuePurpose = parts[i++];
			document.note = parts[i++];
			document.records = parts[i++];
			document.fileName = parts[i];
			if (!StringUtils.hasText(document.fileName)) {
				document.readSuccessfully = false;
				document.statusMsg = "Filnamn saknas";
				return document;
			}
			if (document.fileName.indexOf("\"") ==(document.fileName.length()-1)) {
				// Remove postfix " from last part
				document.fileName = document.fileName.substring(0, document.fileName.length() - 1);
			}
			
			document.mimetype = CommonFileUtil.getMimetypeByExtension(FilenameUtils.getExtension(document.fileName));
			String part1 = document.buildingDescription.substring(0, 1).toUpperCase().replace("/", "_").replace(':', '_');
			if (part1.endsWith(".")) {
				part1 = part1.substring(0, part1.length()-1);
			}
			String part2 = document.buildingDescription.toUpperCase().replace("/", "_").replace(':', '_');
			if (part2.endsWith(".")) {
				part2 = part2.substring(0, part2.length()-1);
			}
			String part3 = document.recordDisplay + " "+ document.issuePurpose.toUpperCase().replace("/", "_").replace(':', '_');
			if (part3.endsWith(".")) {
				part3 = part3.substring(0, part3.length()-1);
			}
			document.path = part1 + "/" + part2 + "/"+ part3;
			document.title = document.recordDisplay+ " " + document.issuePurpose.replace("/", "_").replace(':', '_');
		}
		return document;
	}
}
