package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.MultiCatchRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.tryStatement.MultiCatchASTVisitor;

/**
 * Testing {@link MultiCatchRule}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class MultiCatchRuleTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.multiCatch";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/multiCatch";

	private String fileName;
	private Path preRule, postRule;

	public MultiCatchRuleTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new MultiCatchRule(MultiCatchASTVisitor.class));
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
