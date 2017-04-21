package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.i18n.Messages;

public enum GroupEnum {
	JAVA_1("1.0", Messages.GroupEnum_JAVA_1), //$NON-NLS-1$
	JAVA_2("1.2", Messages.GroupEnum_JAVA_2), //$NON-NLS-1$
	JAVA_3("1.3", Messages.GroupEnum_JAVA_3), //$NON-NLS-1$
	JAVA_4("1.4", Messages.GroupEnum_JAVA_4), //$NON-NLS-1$
	JAVA_5("1.5", Messages.GroupEnum_JAVA_5), //$NON-NLS-1$
	JAVA_6("1.6", Messages.GroupEnum_JAVA_6), //$NON-NLS-1$
	JAVA_7("1.7", Messages.GroupEnum_JAVA_7), //$NON-NLS-1$
	JAVA_8("1.8", Messages.GroupEnum_JAVA_8), //$NON-NLS-1$
	JAVA_9("1.9", Messages.GroupEnum_JAVA_9); //$NON-NLS-1$

	private String javaVersion;
	private String groupName;

	private GroupEnum(String javaVersion, String groupName) {
		this.javaVersion = javaVersion;
		this.groupName = groupName;
	}

	public String getJavaVersion() {
		return javaVersion;
	}
	
	public String getGroupName() {
		return groupName;
	}
}
