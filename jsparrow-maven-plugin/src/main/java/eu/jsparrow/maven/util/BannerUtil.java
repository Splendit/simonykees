package eu.jsparrow.maven.util;

/**
 * Used to create an ASCII art jSparrow banner with the current version
 * information.
 * 
 * @since 2.0.0
 */
public class BannerUtil {

	private BannerUtil() {
		// Hidden constructor
	}

	/**
	 * @return version from MANIFEST.MF
	 */
	private static String getArtifactVersion() {
		return BannerUtil.class.getPackage()
			.getImplementationVersion();
	}

	@SuppressWarnings("nls")
	private static String getLogo() {
		return "    _  _____                                     \n"
				+ "   (_)/ ____|                                    \n"
				+ "    _| (___  _ __   __ _ _ __ _ __ _____      __ \n"
				+ "   | |\\___ \\| '_ \\ / _` | '__| '__/ _ \\ \\ /\\ / / \n"
				+ "   | |____) | |_) | (_| | |  | | | (_) \\ V  V /  \n"
				+ "   | |_____/| .__/ \\__,_|_|  |_|  \\___/ \\_/\\_/   \n"
				+ "  _/ |      | |                                  \n"
				+ " |__/       |_|                                  \n"
				+ "                                                 \n";
	}

	@SuppressWarnings("nls")
	private static String getSeparator() {
		return "=================================================\n";
	}

	private static String getVersionInformation() {
		return getVersionInformation(getArtifactVersion());
	}

	/**
	 * Returns a fixed length version information String
	 * 
	 * @param version
	 *            the version to use
	 * @return fixed length version information String
	 */
	@SuppressWarnings("nls")
	static String getVersionInformation(String version) {
		return String.format("jSparrow Maven Plugin" + "%1$28s", "v" + version);
	}

	@SuppressWarnings("nls")
	public static String getBanner() {
		return String.format("%n%s%s%s%n%s%n", getLogo(), getSeparator(), getVersionInformation(), getSeparator());
	}

}
