package eu.jsparrow.maven.adapter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

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

	public static String findNatureIds(MavenProject project) {
		if (project.getPackaging()
			.equals("eclipse-plugin")) { //$NON-NLS-1$
			return ConfigurationKeys.ECLIPSE_PLUGIN_PROJECT_NATURE_IDS;
		} else {
			return ConfigurationKeys.MAVEN_PROJECT_NATURE_IDS;
		}
	}

	/**
	 * Checks whether the given projects represents an aggregation of projects.
	 * 
	 * @param project
	 *            the maven project to be checked.
	 * @return if the packaging of the project is {@code pom} or the list of
	 *         modules is not empty
	 */
	public static boolean isAggregateProject(MavenProject project) {
		List<String> modules = project.getModules();
		String packaging = project.getPackaging();
		return "pom".equalsIgnoreCase(packaging) || !modules.isEmpty(); //$NON-NLS-1$
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

	/**
	 * Reads the current java source version from the maven-compiler-plugin
	 * configuration in the pom.xml. If no configuration is found, the java
	 * version is 1.5 by default (as stated in the documentation of
	 * maven-compiler-plugin:
	 * https://maven.apache.org/plugins/maven-compiler-plugin/).
	 * 
	 * Note: This setting determines, which rules can be applied by default. We
	 * must use the same default version as Maven. Otherwise Maven would compile
	 * the sources with Java 1.5 anyways and our rules for 1.6 and above would
	 * result in compilation errors.
	 * 
	 * @return the project's java version
	 */
	public static String getCompilerCompliance(MavenProject project) {
		List<Plugin> buildPlugins = project.getBuildPlugins();

		String sourceFromPlugin = getCompilerComplianceFromCompilerPlugin(buildPlugins);
		if (!sourceFromPlugin.isEmpty()) {
			return sourceFromPlugin;
		}

		Properties projectProperties = project.getProperties();

		String sourceProperty = projectProperties
			.getProperty(ConfigurationKeys.MAVEN_COMPILER_PLUGIN_PROPERTY_SOURCE_NAME);
		if (null != sourceProperty) {
			return sourceProperty;
		}

		return ConfigurationKeys.MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION;
	}

	private static String getCompilerComplianceFromCompilerPlugin(List<Plugin> buildPlugins) {
		for (Plugin plugin : buildPlugins) {
			if (ConfigurationKeys.MAVEN_COMPILER_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
				Xpp3Dom pluginConfig = (Xpp3Dom) plugin.getConfiguration();
				if (pluginConfig != null) {
					for (Xpp3Dom child : pluginConfig.getChildren()) {
						if (ConfigurationKeys.MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME.equals(child.getName())) {
							return child.getValue();
						}
					}
				}
				break;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Finds the path of the source folder of the given maven project. If no
	 * source directory is defined in the project's build, then the default
	 * {@value ConfigurationKeys#DEFAULT_SOURCE_FOLDER_PATH}.
	 * 
	 * @param mavenProject
	 *            an instance of {@link MavenProject}.
	 * @return the relative path w.r.t the absolute path of project's root
	 *         folder.
	 */
	public static String findSourceDirectory(MavenProject mavenProject) {
		Build build = mavenProject.getBuild();
		if (build != null) {
			String sourceDirectory = build.getSourceDirectory();
			if (sourceDirectory != null) {
				File projectDirectory = mavenProject.getBasedir();
				Path sourceAbsolutePath = Paths.get(sourceDirectory);
				Path projectAbsolutePath = projectDirectory.toPath();
				Path relativePath = projectAbsolutePath.relativize(sourceAbsolutePath);
				return relativePath.toString();
			}
		}

		MavenProject parent = mavenProject.getParent();
		if (parent != null) {
			return findSourceDirectory(parent);
		}
		return ConfigurationKeys.DEFAULT_SOURCE_FOLDER_PATH;
	}

}
