package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.FlatMapInsteadOfNestedLoopsRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.FlatMapInsteadOfNestedLoopsASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class FlatMapInsteadOfNestedLoopsRuleTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.flatMapInsteadOfNestedLoops";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY
			+ "/postRule/flatMapInsteadOfNestedLoops";

	private String fileName;
	private Path preRule, postRule;

	public FlatMapInsteadOfNestedLoopsRuleTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new FlatMapInsteadOfNestedLoopsRule(FlatMapInsteadOfNestedLoopsASTVisitor.class));
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
