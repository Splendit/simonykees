package at.splendit.simonykees.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.EnhancedForLoopToStreamAnyMatchRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamAnyMatchASTVisitor;

/**
 * Testing {@link EnhancedForLoopToStreamAnyMatchRule}.
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class EnhancedForLoopToStreamAnyMatchRuleTest extends AbstractRulesTest {
	
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.enhancedForLoopToStreamAnyMatch";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/enhancedForLoopToStreamAnyMatch";

	private String fileName;
	private Path preRule, postRule;

	public EnhancedForLoopToStreamAnyMatchRuleTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new EnhancedForLoopToStreamAnyMatchRule(EnhancedForLoopToStreamAnyMatchASTVisitor.class));
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
