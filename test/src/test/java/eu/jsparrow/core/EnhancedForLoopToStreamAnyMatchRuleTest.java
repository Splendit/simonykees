package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamAnyMatchRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;

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
