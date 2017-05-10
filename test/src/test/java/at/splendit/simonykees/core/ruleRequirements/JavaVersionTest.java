package at.splendit.simonykees.core.ruleRequirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class JavaVersionTest {

	IJavaProject testproject = null;

	@Before
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@After
	public void tearDown() {
		testproject = null;
	}

	// TODO: ask Martin about this test count things
	@Parameters(name = "{index}: test java version[{0}]")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { JavaCore.VERSION_1_1, 10 }, { JavaCore.VERSION_1_2, 11 },
				{ JavaCore.VERSION_1_3, 11 }, { JavaCore.VERSION_1_4, 12 }, { JavaCore.VERSION_1_5, 18 },
				{ JavaCore.VERSION_1_6, 19 }, { JavaCore.VERSION_1_7, 22 }, { JavaCore.VERSION_1_8, 23 } });
	}

	private String javaVersion;

	private long numberOfRules;

	public JavaVersionTest(String javaVersion, long numberOfRules) {
		this.javaVersion = javaVersion;
		this.numberOfRules = numberOfRules;
	}

	@Test
	public void filterWithoutStringUtilsIsPresent() {
		testproject.setOption(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		Assert.assertEquals(
				String.format("Number of rules that support Java Version %s have changed. Check it!", javaVersion), //$NON-NLS-1$
				numberOfRules, numberOfActiveRulesForJavaVersion(javaVersion));
	}

	@Test
	public void filterWithStringUtilsIsPresent() throws Exception {
		testproject.setOption(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		RulesTestUtil.addToClasspath(testproject, entries);
		Assert.assertEquals(
				String.format("Number of rules that support Java Version %s have changed. Check it!", javaVersion), //$NON-NLS-1$
				numberOfRules, numberOfActiveRulesForJavaVersion(javaVersion));
	}

	private long numberOfActiveRulesForJavaVersion(String version) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer
				.getRulesForProject(testproject);
		return rules.stream().filter(r -> r.isEnabled()).count();
	}
}
