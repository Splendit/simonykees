package at.splendit.simonykees.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.GroupEnum;

public class GroupUtil {
	private static final Logger logger = LoggerFactory.getLogger(GroupUtil.class);

	private GroupUtil() {

	}

	/**
	 * Returns a list of all Java versions that are lower than the parameters
	 * one
	 * 
	 * @param javaVersion
	 *            highest present java version
	 * @return list of java version
	 */
	public static List<GroupEnum> allJavaVersionSince(GroupEnum javaVersion) {
		List<GroupEnum> result = new ArrayList<>();
		String jVersion = javaVersion.name();

		if (jVersion.contains("JAVA_")) { //$NON-NLS-1$
			switch (javaVersion) {
			case JAVA_1_1:
				result.add(GroupEnum.JAVA_1_1);
			case JAVA_1_2:
				result.add(GroupEnum.JAVA_1_2);
			case JAVA_1_3:
				result.add(GroupEnum.JAVA_1_3);
			case JAVA_1_4:
				result.add(GroupEnum.JAVA_1_4);
			case JAVA_1_5:
				result.add(GroupEnum.JAVA_1_5);
			case JAVA_1_6:
				result.add(GroupEnum.JAVA_1_6);
			case JAVA_1_7:
				result.add(GroupEnum.JAVA_1_7);
			case JAVA_1_8:
				result.add(GroupEnum.JAVA_1_8);
			case JAVA_1_9:
				result.add(GroupEnum.JAVA_1_9);
			}
		}
		return result;
	}
	/**
	 * 
	 * @param compilerComplianceOption is a String corresponding to the {@link JavaCore#COMPILER_COMPLIANCE} Java Language level definition
	 * @param categories 
	 * @return true if the compilerCompliance is part
	 */
	public static boolean compilerOptionMatchesGroup(String compilerComplianceOption, List<GroupEnum> categories) {
		return categories.stream().anyMatch(c -> c.getJavaVersion().equals(compilerComplianceOption));
	}

}
