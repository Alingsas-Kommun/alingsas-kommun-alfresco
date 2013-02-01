package se.alingsas.alfresco.repo.workflow.model;

public interface CommonWorkflowModel {
	public static final String DOC_STATUS_DONE = "Färdigt dokument";
	public static final String DOC_STATUS_WORKING = "Arbetsdokument";
	public static final String BPM_PACKAGE = "bpm_package";
	public static final String BPM_COMMENT = "bpm_comment";
	public static final String HANDLING = "akwf_handling";
	public static final String INITIATOR = "akwf_initiator";
	public static final String APPROVER = "akwf_approver";
	public static final String TARGET_SITE = "akwf_targetSite";
	public static final String TARGET_FOLDER = "akwf_targetFolder";
	public static final String TARGET_CHOICE = "akwf_reviewDocumentTargetChoice";	
	public static final String TARGET_CHOICE_OPTION_SITE = "site";
	public static final String TARGET_CHOICE_OPTION_MY_HOME_FOLDER = "my_homefolder";
	public static final String TARGET_CHOICE_OPTION_USER_HOME_FOLDER = "user_homefolder";	
	
	public static final String SITE_GROUP = "akwf_siteGroup";
	public static final String SITE = "akwf_site";
	public static final String SITE_MANAGER = "SiteManager";
	public static final String SITE_COLLABORATOR = "SiteCollaborator";
}
