package se.alingsas.alfresco.repo.scripts.ddmigration;

import java.util.Comparator;

import org.alfresco.util.VersionNumber;

public class VersionComparator implements Comparator<AlingsasDocument> {

	@Override
	public int compare(final AlingsasDocument document1,
			final AlingsasDocument document2) {
		final String v1 = document1.version;
		final String v2 = document2.version;

		// sort the list descending (ie. most recent first)
		return new VersionNumber(v1).compareTo(new VersionNumber(v2));
	}
	
}
