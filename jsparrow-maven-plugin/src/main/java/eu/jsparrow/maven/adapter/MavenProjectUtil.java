package eu.jsparrow.maven.adapter;

import org.apache.maven.project.MavenProject;

/**
 * A utility class for extracting information from a {@link MavenProject}.
 * 
 * @since 1.1.0
 */
public class MavenProjectUtil {

	private MavenProjectUtil() {
		/*
		 * Hiding public constructor
		 */
	}

	/**
	 * Concatenates the groupId and the artifactId of the project.
	 * 
	 * @param mavenProject
	 *            project to generate identifier for.
	 * @return the computed identifier.
	 */
	public static String findProjectIdentifier(MavenProject mavenProject) {
		String groupId = mavenProject.getGroupId();
		String artifactId = mavenProject.getArtifactId();
		return groupId + MavenAdapter.DOT + artifactId;
	}

}
