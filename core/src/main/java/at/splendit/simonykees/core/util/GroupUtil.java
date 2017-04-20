package at.splendit.simonykees.core.util;

import java.util.ArrayList;
import java.util.List;

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
	public static List<GroupEnum> allJavaVersionTo(GroupEnum javaVersion) {
		List<GroupEnum> result = new ArrayList<>();
		String jVersion = javaVersion.name();

		if (jVersion.contains("JAVA_")) { //$NON-NLS-1$
			switch (javaVersion) {
			case JAVA_9:
				result.add(GroupEnum.JAVA_9);
			case JAVA_8:
				result.add(GroupEnum.JAVA_8);
			case JAVA_7:
				result.add(GroupEnum.JAVA_7);
			case JAVA_6:
				result.add(GroupEnum.JAVA_6);
			case JAVA_5:
				result.add(GroupEnum.JAVA_5);
			case JAVA_4:
				result.add(GroupEnum.JAVA_4);
			case JAVA_3:
				result.add(GroupEnum.JAVA_3);
			case JAVA_2:
				result.add(GroupEnum.JAVA_2);
			case JAVA_1:
				result.add(GroupEnum.JAVA_1);
			}
		}
		return result;
	}

}
