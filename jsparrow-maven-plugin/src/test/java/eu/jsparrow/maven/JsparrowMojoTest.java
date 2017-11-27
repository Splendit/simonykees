package eu.jsparrow.maven;

import java.io.File;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

@SuppressWarnings("nls")
public class JsparrowMojoTest extends AbstractMojoTestCase {

	private static final String pluginPom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"> "
			+ "<modelVersion>4.0.0</modelVersion> " + "<groupId>eu.jsparrow</groupId> "
			+ "<version>2.4.0-SNAPSHOT</version> " + "<artifactId>jsparrow-maven-plugin</artifactId> "
			+ "<packaging>maven-plugin</packaging> " +

			"<name>Sample Parameter-less Maven Plugin</name> " +

			"<properties> " + "	<maven.compiler.source>1.8</maven.compiler.source> "
			+ "	<maven.compiler.target>1.8</maven.compiler.target> "
			+ "	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding> " + "</properties> " +

			"<dependencies> " + "	<dependency> " + "		<groupId>org.apache.maven</groupId> "
			+ "		<artifactId>maven-plugin-api</artifactId> " + "		<version>3.0</version> " + "	</dependency> "
			+

			"	<!-- dependencies to annotations --> " + "	<dependency> "
			+ "		<groupId>org.apache.maven.plugin-tools</groupId> "
			+ "		<artifactId>maven-plugin-annotations</artifactId> " + "		<version>3.4</version> "
			+ "		<scope>provided</scope> " + "	</dependency> " +

			"	<dependency> " + "		<groupId>org.apache.maven</groupId> "
			+ "		<artifactId>maven-project</artifactId> " + "		<version>2.0.6</version> " + "	</dependency> "
			+

			"	<dependency> " + "		<groupId>org.apache.maven</groupId> "
			+ "		<artifactId>maven-compat</artifactId> " + "		<version>3.3.9</version> " + "	</dependency> " +

			"	<dependency> " + "		<groupId>org.eclipse.core</groupId> "
			+ "		<artifactId>runtime</artifactId> " + "		<version>3.10.0-v20140318-2214</version> "
			+ "	</dependency> " +

			"	<!-- https://mvnrepository.com/artifact/org.eclipse.platform/org.eclipse.osgi --> " + "	<dependency> "
			+ "		<groupId>org.eclipse.platform</groupId> " + "		<artifactId>org.eclipse.osgi</artifactId> "
			+ "		<version>3.11.2</version> " + "	</dependency> " +

			"	<!-- https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-dependency-plugin --> "
			+ "	<dependency> " + "		<groupId>org.apache.maven.plugins</groupId> "
			+ "		<artifactId>maven-dependency-plugin</artifactId> " + "		<version>3.0.2</version> "
			+ "	</dependency> " +

			"	<dependency> " + "		<groupId>org.apache.maven.shared</groupId> "
			+ "		<artifactId>maven-invoker</artifactId> " + "		<version>3.0.0</version> " + "	</dependency> "
			+

			"	<!-- For testing --> " + "	<dependency> " + "		<groupId>org.apache.maven.plugin-testing</groupId> "
			+ "		<artifactId>maven-plugin-testing-harness</artifactId> " + "		<version>3.3.0</version> "
			+ "		<scope>test</scope> " + "	</dependency> " +

			"</dependencies> " +

			"<build> " + "	<plugins> " + "		<plugin> " + "			<groupId>org.apache.maven.plugins</groupId> "
			+ "			<artifactId>maven-plugin-plugin</artifactId> " + "			<version>3.4</version> "
			+ "		</plugin> " + "		<plugin> " + "			<groupId>org.apache.maven.plugins</groupId> "
			+ "			<artifactId>maven-compiler-plugin</artifactId> " + "			<version>3.6.2</version> "
			+ "			<configuration> " + "				<source>1.8</source> "
			+ "				<target>1.8</target> " + "			</configuration> " + "		</plugin> "
			+ "		<plugin> " + "			<groupId>eu.jsparrowt</groupId> "
			+ "			<artifactId>jsparrow-maven-plugin</artifactId> "
			+ "			<version>2.4.0-SNAPSHOT</version> " + "		</plugin> " +

			"	</plugins> " + "</build> " + "</project>";

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// required for mojo lookups to work
		super.setUp();
	}

	@Override
	protected Mojo lookupMojo(String goal, File pom) throws Exception {
		File pluginPom = pom;

		Xpp3Dom pluginPomDom = Xpp3DomBuilder.build(ReaderFactory.newXmlReader(pluginPom));

		String artifactId = pluginPomDom.getChild("artifactId").getValue();
		String groupId = resolveFromRootThenParent(pluginPomDom, "groupId");
		String version = resolveFromRootThenParent(pluginPomDom, "version");

		return lookupMojo(groupId, artifactId, version, goal, null);
	}

	/**
	 * @see AbstractMojoTestCase#resolveFromRootThenParent( Xpp3Dom
	 *      pluginPomDom, String element )
	 */
	private String resolveFromRootThenParent(Xpp3Dom pluginPomDom, String element) throws Exception {
		Xpp3Dom elementDom = pluginPomDom.getChild(element);

		// parent might have the group Id so resolve it
		if (elementDom == null) {
			Xpp3Dom pluginParentDom = pluginPomDom.getChild("parent");

			if (pluginParentDom != null) {
				elementDom = pluginParentDom.getChild(element);

				if (elementDom == null) {
					throw new Exception("unable to determine " + element);
				}

				return elementDom.getValue();
			}

			throw new Exception("unable to determine " + element);
		}

		return elementDom.getValue();
	}

	/**
	 * @throws Exception
	 */
	public void testMojoGoal() throws Exception {
		File testPom = new File(getBasedir(), "src/test/resources/plugin-config.xml");
		assertNotNull(testPom);
		assertTrue(testPom.exists());

		JsparrowMojo mojo = (JsparrowMojo) lookupMojo("refactor", testPom);
		assertNotNull(mojo);
		assertNotNull(mojo);
	}

}
