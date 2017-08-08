package at.splendit.simonykees.core;

import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.IndexOfToContainsRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.IndexOfToContainsASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.0.4
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class IndexOfToContainsRulesTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.indexOfToContains";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/indexOfToContains";

	private String fileName;
	private Path preRule;
	private Path postRule;

	static {
		javaVersion = JavaCore.VERSION_1_7;
	}

	public IndexOfToContainsRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new IndexOfToContainsRule(IndexOfToContainsASTVisitor.class));

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
