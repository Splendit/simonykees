package eu.jsparrow.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer
 * @since 0.9
 */
public class RulesTestUtil {

	/**
	 * relative reference to the maven sample module
	 */
	public static final String SAMPLE_MODULE_PATH = "../sample/";

	private static final Path[] EMPTY_PATHS = new Path[0];
	public static final String RULE_SUFFIX = "*Rule.java";

	public static final String BASE_PACKAGE = "package eu.jsparrow.sample";
	public static final String PRERULE_PACKAGE = "package eu.jsparrow.sample.preRule";
	public static final String BASE_DIRECTORY = SAMPLE_MODULE_PATH + "src/test/java/eu/jsparrow/sample";
	public static final String PRERULE_DIRECTORY = SAMPLE_MODULE_PATH + "src/test/java/eu/jsparrow/sample/preRule";

	private RulesTestUtil() {
		// hiding
	}

	public static IPackageFragmentRoot getPackageFragementRoot() throws Exception {
		return getPackageFragementRoot(JavaCore.VERSION_1_8);
	}

	public static IPackageFragmentRoot getPackageFragementRoot(String javaVersion) throws Exception {
		IJavaProject javaProject = createJavaProject("allRulesTest", "bin");
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaVersion);
		javaProject.setOption(JavaCore.COMPILER_SOURCE, javaVersion);
		IPackageFragmentRoot root = addSourceContainer(javaProject, "/allRulesTestRoot");
		addToClasspath(javaProject, extractMavenDependenciesFromPom(SAMPLE_MODULE_PATH + "pom.xml"));

		return root;
	}

	public static List<IClasspathEntry> extractMavenDependenciesFromPom(String classpathFile) throws Exception {
		List<IClasspathEntry> collectedEntries = new ArrayList<>();
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document document = builder.parse(new File(classpathFile));

		final Node projectNode = getNodeByNodeName(document.getChildNodes(), "project");
		if (projectNode == null) {
			return collectedEntries;
		}

		final Node dependencyNode = getNodeByNodeName(projectNode.getChildNodes(), "dependencies");
		if (dependencyNode == null) {
			return collectedEntries;
		}

		final List<Node> dependencies = asList(dependencyNode.getChildNodes());
		for (Node dependency : dependencies) {
			final NodeList children = dependency.getChildNodes();

			final Node groupIdNode = getNodeByNodeName(children, "groupId");
			final Node artifactIdNode = getNodeByNodeName(children, "artifactId");
			final Node versionNode = getNodeByNodeName(children, "version");

			if (groupIdNode == null || artifactIdNode == null || versionNode == null) {
				collectedEntries.clear();
				return collectedEntries;
			}

			String groupId = groupIdNode.getTextContent();
			String artifactId = artifactIdNode.getTextContent();
			String version = versionNode.getTextContent();

			if (groupId == null || artifactId == null || version == null) {
				collectedEntries.clear();
				return collectedEntries;
			}

			collectedEntries.add(generateMavenEntryFromDepedencyString(groupId, artifactId, version));
		}

		return collectedEntries;
	}

	public static IClasspathEntry generateMavenEntryFromDepedencyString(String groupId, String artifactId,
			String version) throws Exception {
		Path jarPath = new Path(getM2Repository() + File.separator + toPath(groupId) + File.separator + artifactId
				+ File.separator + version + File.separator + artifactId + "-" + version + ".jar");
		if (!jarPath.toFile()
			.exists()) {
			throw new IllegalArgumentException(String.format(
					"Maven Dependency :[%s:%s:%s] not found in local repository, add it to ../sample/pom.xml in the maven-dependency-plugin and execute package to download",
					groupId, artifactId, version));
		}

		return JavaCore.newLibraryEntry(jarPath, null, null);
	}

	private static String getM2Repository() throws Exception {
		final String userHome = System.getProperty("user.home");
		final File m2Settings = new File(userHome + "/.m2/settings.xml");
		if (m2Settings.exists() && m2Settings.isFile()) {
			final Document document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(m2Settings);

			final Node settingsNode = getNodeByNodeName(document.getChildNodes(), "settings");
			if (settingsNode != null) {
				final Node localRepoNode = getNodeByNodeName(settingsNode.getChildNodes(), "localRepository");
				if (localRepoNode != null) {
					return localRepoNode.getTextContent();
				}
			}
		}
		final File m2Repo = new File(userHome + "/.m2/repository");
		if (m2Repo.exists() && m2Repo.isDirectory()) {
			return m2Repo.getPath();
		}
		throw new RuntimeException("Cannot determine maven repository." + " Tried \"" + m2Settings + "\" file"
				+ " and \"" + m2Repo + "\" directory.");
	}

	private static String toPath(String groupId) {
		return StringUtils.replace(groupId, ".", "/");
	}

	private static Node getNodeByNodeName(NodeList nodes, String nodeName) {
		return asList(nodes).stream()
			.filter(node -> nodeName.equals(node.getNodeName()))
			.findFirst()
			.orElse(null);
	}

	private static List<Node> asList(NodeList nodeList) {
		final List<Node> results = new ArrayList<>();
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			final Node item = nodeList.item(i);
			if (item.getNodeType() != Node.TEXT_NODE) {
				results.add(item);
			}
		}
		return results;
	}

	public static IPackageFragmentRoot addSourceContainer(IJavaProject javaProject, String containerName)
			throws Exception {
		IProject project = javaProject.getProject();
		IFolder folder = project.getFolder(containerName);
		createFolder(folder);
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);
		IClasspathEntry classpathEntry = JavaCore.newSourceEntry(root.getPath(), EMPTY_PATHS, EMPTY_PATHS, null);
		addToClasspath(javaProject, Collections.singletonList(classpathEntry));
		return root;
	}

	public static void addToClasspath(IJavaProject javaProject, List<IClasspathEntry> classpathEntries)
			throws Exception {
		if (!classpathEntries.isEmpty()) {
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries;
			if (oldEntries.length != 0) {
				Set<IClasspathEntry> set = new HashSet<>(Arrays.asList(oldEntries));
				set.addAll(classpathEntries);
				newEntries = set.toArray(new IClasspathEntry[set.size()]);
			} else {
				newEntries = classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
			}
			javaProject.setRawClasspath(newEntries, new NullProgressMonitor());
		}
	}

	public static IJavaProject createJavaProject(String projectName, String binFolderName) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject project = workspaceRoot.getProject(projectName);

		if (!project.exists()) {
			project.create(new NullProgressMonitor());
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}

		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}

		IFolder binFolder = project.getFolder(binFolderName);

		createFolder(binFolder);
		addNature(project, JavaCore.NATURE_ID);
		IClasspathEntry jreEntry = JavaRuntime.getDefaultJREContainerEntry();
		IJavaProject javaProject = JavaCore.create(project);
		javaProject.setOutputLocation(binFolder.getFullPath(), new NullProgressMonitor());
		javaProject.setRawClasspath(new IClasspathEntry[] { jreEntry }, new NullProgressMonitor());

		/*
		 * The following options are extracted from our internal eclipse code
		 * formatter at
		 * https://bitbucket.splendit.loc/projects/INT/repos/eclipse-settings/
		 * browse/splendit_default_formatter_20171019.xml . This has been done
		 * for being able to format our unit tests with eclipse and not breaking
		 * them by doing it. With this options the junit test project and
		 * simonykees itself use the same formatting options.
		 */
		Map<String, String> options = javaProject.getOptions(false);
		options.put("org.eclipse.jdt.core.formatter.alignment_for_enum_constants", "49");
		options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_enum_constant", "48");
		options.put("org.eclipse.jdt.core.formatter.comment.count_line_length_from_starting_position", "false");
		options.put("org.eclipse.jdt.core.formatter.alignment_for_selector_in_method_invocation", "85");
		options.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_enum_declaration", "48");
		javaProject.setOptions(options);

		return javaProject;
	}

	private static void addNature(IProject project, String natureId) throws Exception {
		if (project.hasNature(natureId)) {
			return;
		}

		IProjectDescription projectDescription = project.getDescription();

		String[] oldNatures = (projectDescription.getNatureIds());
		String[] newNatures = Arrays.copyOf(oldNatures, oldNatures.length + 1);
		newNatures[oldNatures.length] = natureId;

		projectDescription.setNatureIds(newNatures);
		project.setDescription(projectDescription, null);

	}

	private static void createFolder(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent);
			}
			folder.create(false, true, null);
		}
	}

}
