package eu.jsparrow.maven;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.it.Verifier;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import eu.jsparrow.maven.stubs.RefactorProjectStub;

@SuppressWarnings("nls")
public class RefactorMojoTest extends AbstractMojoTestCase {

	private RefactorMojo mojo;
	private MavenProjectStub project;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// required for mojo lookups to work
		super.setUp();

		project = new RefactorProjectStub();
		MavenSession session = newMavenSession(project);

		mojo = new RefactorMojo();
		mojo = (RefactorMojo) configureMojo(mojo, extractPluginConfiguration("jsparrow-maven-plugin", ((RefactorProjectStub) project).getPom()));

		setVariableValueToObject(mojo, "project", project);
		setVariableValueToObject(mojo, "mavenHome", "");
		setVariableValueToObject(mojo, "mavenSession", session);
	}

	/** {@inheritDoc} */
	protected void tearDown() throws Exception {
		// required
		super.tearDown();
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

//	public void testErrorFreeRefactorGoal() throws Exception {
//		assertNotNull(mojo);
//
//		((RefactorProjectStub) project).createYamlFile();
//
//		File configFile = new File(((RefactorProjectStub) project).getBasedir() + "/jsparrow.yml");
//		setVariableValueToObject(mojo, "configFile", configFile);
//		setVariableValueToObject(mojo, "profile", "");
//
////		File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/jsparrow-maven-test");
//
//		Verifier verifier;
//
//		// verifier = new Verifier(testDir.getAbsolutePath());
//		verifier = new Verifier(((RefactorProjectStub) project).getBasedir()
//			.getAbsolutePath());
//		verifier.executeGoal("eclipse:eclipse");
//		verifier.verifyTextInLog("BUILD SUCCESS");
//
//		verifier.executeGoal("jsparrow:refactor");
//		verifier.verifyTextInLog("BUILD SUCCESS");
//		verifier.verifyTextInLog("Selected Profile: profile1");
//	}
//
//	public void testRefactorGoal_errorYaml() throws Exception {
//		assertNotNull(mojo);
//
//		((RefactorProjectStub) project).createYamlFileWithError();
//
//		File configFile = new File(((RefactorProjectStub) project).getBasedir() + "/jsparrow.yml");
//		setVariableValueToObject(mojo, "configFile", configFile);
//		setVariableValueToObject(mojo, "profile", "");
//
//		Verifier verifier;
//
//		verifier = new Verifier(((RefactorProjectStub) project).getBasedir()
//			.getAbsolutePath());
//		verifier.executeGoal("eclipse:eclipse");
//		verifier.verifyTextInLog("BUILD SUCCESS");
//
//		try {
//			verifier.executeGoal("jsparrow:refactor");
//		} catch (VerificationException e) {
//
//		}
//		verifier.verifyTextInLog("BUILD FAILURE");
//	}

	public void testRefactorMojo_parameter() throws Exception {
		assertNotNull(mojo);

//		((RefactorProjectStub) project).createYamlFile();

//		File configFile = new File(((RefactorProjectStub) project).getBasedir() + "/jsparrow.yml");
//		setVariableValueToObject(mojo, "configFile", configFile);
//		setVariableValueToObject(mojo, "profile", "");

		((RefactorProjectStub) project).createYamlFile();

		Verifier verifier = new Verifier(((RefactorProjectStub) project).getBasedir()
			.getAbsolutePath());

//		Properties props = new Properties(System.getProperties());
//		props.put("configFile", "conf.yml");
//		props.put("profile", "profile2");

//		verifier.getCliOptions().add("-DconfigFile=conf.yml");
		verifier.getCliOptions().add("-DdefaultConfiguration");
		verifier.executeGoal("jsparrow:refactor");

	}
}
