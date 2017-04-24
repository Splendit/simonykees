package at.splendit.simonykees.core.rule;

import org.eclipse.jdt.core.JavaCore;

import at.splendit.simonykees.i18n.Messages;

public enum GroupEnum {
	JAVA_1_1(JavaCore.VERSION_1_1, Messages.GroupEnum_JAVA_1),
	JAVA_1_2(JavaCore.VERSION_1_2, Messages.GroupEnum_JAVA_2),
	JAVA_1_3(JavaCore.VERSION_1_3, Messages.GroupEnum_JAVA_3),
	JAVA_1_4(JavaCore.VERSION_1_4, Messages.GroupEnum_JAVA_4),
	JAVA_1_5(JavaCore.VERSION_1_5, Messages.GroupEnum_JAVA_5),
	JAVA_1_6(JavaCore.VERSION_1_6, Messages.GroupEnum_JAVA_6),
	JAVA_1_7(JavaCore.VERSION_1_7, Messages.GroupEnum_JAVA_7),
	JAVA_1_8(JavaCore.VERSION_1_8, Messages.GroupEnum_JAVA_8),
	JAVA_1_9("1.9", Messages.GroupEnum_JAVA_9); //$NON-NLS-1$ // not yet in the JavaCore defined

	private String javaVersion;
	private String groupName;

	private GroupEnum(String javaVersion, String groupName) {
		this.javaVersion = javaVersion;
		this.groupName = groupName;
	}

	/**
	 * Returns the JavaVersion of the Group
	 * @return 
	 */
	public String getJavaVersion() {
		return javaVersion;
	}
	
	public String getGroupName() {
		return groupName;
	}
}
