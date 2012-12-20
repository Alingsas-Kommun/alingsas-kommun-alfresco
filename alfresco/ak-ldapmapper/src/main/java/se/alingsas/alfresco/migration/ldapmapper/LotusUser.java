package se.alingsas.alfresco.migration.ldapmapper;

/**
 * The Pojo for relevant ldap properties for a notes user
 * @author mars
 *
 */
public class LotusUser {

	private String commonName;
	private String uid;
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

}
