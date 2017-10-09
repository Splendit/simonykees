package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class with helper methods for updating maven project dependencies
 * 
 * @author Andreja Sambolec
 * @since 2.1.1
 */
@SuppressWarnings("nls")
public class MavenUtil {

	private MavenUtil() {
		// hiding
	}

	public static IClasspathEntry generateMavenEntryFromDepedencyString(String groupId, String artifactId,
			String version) throws SAXException, IOException, ParserConfigurationException {
		Path jarPath = new Path(getM2Repository() + File.separator + toPath(groupId) + File.separator + artifactId
				+ File.separator + version + File.separator + artifactId + "-" + version + ".jar");
		if (!jarPath.toFile().exists()) {
			throw new IllegalArgumentException(String.format(
					"Maven Dependency :[%s:%s:%s] not found in local repository, add it to ../sample/pom.xml in the maven-dependency-plugin and execute package to download",
					groupId, artifactId, version));
		}
		return JavaCore.newLibraryEntry(jarPath, null, null);
	}

	private static String getM2Repository() throws SAXException, IOException, ParserConfigurationException {
		final String userHome = System.getProperty("user.home");
		final File m2Settings = new File(userHome + "/.m2/settings.xml");
		if (m2Settings.exists() && m2Settings.isFile()) {
			final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(m2Settings);

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
		for (Node node : asList(nodes)) {
			if (nodeName.equals(node.getNodeName())) {
				return node;
			}
		}
		return null;
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

	public static void addToClasspath(IJavaProject javaProject, List<IClasspathEntry> classpathEntries)
			throws JavaModelException {
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
			javaProject.setRawClasspath(newEntries, null);
		}
	}
}
