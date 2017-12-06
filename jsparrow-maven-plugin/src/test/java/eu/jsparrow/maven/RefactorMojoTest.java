package eu.jsparrow.maven;

import java.io.File;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

@SuppressWarnings("nls")
public class RefactorMojoTest extends AbstractMojoTestCase {

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

		String artifactId = pluginPomDom.getChild("artifactId")
			.getValue();
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

		RefactorMojo mojo = (RefactorMojo) lookupMojo("refactor", testPom);
		assertNotNull(mojo);
		assertNotNull(mojo);
	}

}
