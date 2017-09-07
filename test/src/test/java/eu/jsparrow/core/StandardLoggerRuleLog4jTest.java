package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.standardLogger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.semiAutomatic.StandardLoggerASTVisitor;

/**
 * Testing standard logger rule.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class StandardLoggerRuleLog4jTest extends AbstractRulesTest {

	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.standardLoggerLog4j";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/standardLoggerLog4j";

	private String fileName;
	private Path preRule;
	private Path postRule;

	public StandardLoggerRuleLog4jTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;

		StandardLoggerRule standardLoggerRule = new StandardLoggerRule(StandardLoggerASTVisitor.class);
		standardLoggerRule.activateDefaultOptions();
		rulesList.add(standardLoggerRule);
		
		try {
			IJavaProject javaProject = RulesTestUtil.createJavaProject("allRulesTest", "bin");
			root = RulesTestUtil.addSourceContainer(javaProject, "/allRulesTestRoot");

			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			entries.add(
					RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7"));
			RulesTestUtil.addToClasspath(javaProject, entries);
			RulesTestUtil.addToClasspath(javaProject, RulesTestUtil.getClassPathEntries(root));
		} catch (Exception e) {
		}
	}

	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		return AbstractRulesTest.load(POSTRULE_DIRECTORY);
	}

	@Test
	public void testTransformation() throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
