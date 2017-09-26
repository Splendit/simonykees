package eu.jsparrow.core.requirements;

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

import eu.jsparrow.core.rule.impl.StringUtilsRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.StringUtilsASTVisitor;

/**
 * Tests IJavaProject if a specific version of a library is present. The first
 * parameter contains a list of {@link IClasspathEntriy} that represent the
 * maven dependency in a eclipse appropriate way. The second one is a boolean
 * that represents the enabled state of the tested rule, for the test case.
 * 
 * @author Martin Huter
 * @since 1.2
 *
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class LibraryPrerequestsTest {

	IJavaProject testproject = null;
	private List<IClasspathEntry> entries;
	private boolean enabled;

	public LibraryPrerequestsTest(List<IClasspathEntry> entries, boolean enabled) {
		this.entries = entries;
		this.enabled = enabled;
	}

	@Before
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@After
	public void tearDown() {
		testproject = null;
	}

	@Parameters(name = "{index}: test with pom:[{0}]")
	public static Collection<Object[]> data() throws Exception {
		return Arrays.asList(new Object[][] {
				{ Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
						"commons-lang3", "3.1")), true },
				{ Arrays.asList(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
						"commons-lang3", "3.2.1")), false } });
	}

	@Test
	public void filterWithStringUtilsIsPresent() throws Exception {
		RulesTestUtil.addToClasspath(testproject, entries);

		StringUtilsRule sur = new StringUtilsRule(StringUtilsASTVisitor.class);
		sur.calculateEnabledForProject(testproject);

		Assert.assertEquals(enabled, sur.isEnabled());
	}
}
