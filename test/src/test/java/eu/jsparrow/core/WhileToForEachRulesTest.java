package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.WhileToForEachRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.loop.whileToForEach.WhileToForEachASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class WhileToForEachRulesTest extends AbstractRulesTest {

	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.whileToForEach";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/whileToForEach";

	private String fileName;
	private Path preRule, postRule;

	public WhileToForEachRulesTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		RefactoringRule<? extends AbstractASTRewriteASTVisitor> whileRule = new WhileToForEachRule(WhileToForEachASTVisitor.class);
		rulesList.add(whileRule);
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
