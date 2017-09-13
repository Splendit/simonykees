package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.LambdaForEachCollectRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachCollectASTVisitor;

/**
 * Testing stream forEach to stream collect.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class LambdaForEachCollectRuleTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.lambdaForEachCollect";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/lambdaForEachCollect";
	
	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public LambdaForEachCollectRuleTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new  LambdaForEachCollectRule(LambdaForEachCollectASTVisitor.class));
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
