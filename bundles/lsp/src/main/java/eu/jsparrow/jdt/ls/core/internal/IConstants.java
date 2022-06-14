package eu.jsparrow.jdt.ls.core.internal;

public class IConstants {

	/**
	 * Plugin id
	 */
	public static final String PLUGIN_ID = "eu.jsparrow.jdt.ls.core";

	/**
	 * Is workspace initialized
	 */
	public static final String WORKSPACE_INITIALIZED = "workspaceInitialized";

	/**
	 * Jobs family id
	 */
	public static final String JOBS_FAMILY = PLUGIN_ID + ".jobs";

	/**
	 * Update project job family id
	 */
	public static final String UPDATE_PROJECT_FAMILY = JOBS_FAMILY + ".updateProject";

	/**
	 * Update workspace folders job family id
	 */
	public static final String UPDATE_WORKSPACE_FOLDERS_FAMILY = JOBS_FAMILY + ".updateWorkspaceFolders";

	public static final String CHANGE_METHOD_SIGNATURE = "org.eclipse.jdt.ls.change.method.signature";
}
