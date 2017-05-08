package at.splendit.simonykees.core.ruleRequirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.StringUtilsRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;

@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class LibraryPrerequestsTest {

	IJavaProject testproject = null;

	@Before
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@After
	public void tearDown() {
		testproject = null;
	}

	@Parameters(name = "{index}: test with pom:[{0}]")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "src/test/resources/apache-commons-lang-pom.xml", true },
				{ "src/test/resources/apache-commons-lang-3-2-1-pom.xml", false } });
	}

	private String pom;
	private boolean enabled;

	public LibraryPrerequestsTest(String pom, boolean enabled) {
		this.pom = pom;
		this.enabled = enabled;
	}

	@Test
	public void filterWithStringUtilsIsPresent() throws Exception {
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		RulesTestUtil.extractClasspathEntries(entries, pom);
		RulesTestUtil.addToClasspath(testproject, entries);

		StringUtilsRule sur = new StringUtilsRule(StringUtilsASTVisitor.class);
		sur.calculateEnabledForProject(testproject);

		Assert.assertEquals(enabled, sur.isEnabled());
	}
}
