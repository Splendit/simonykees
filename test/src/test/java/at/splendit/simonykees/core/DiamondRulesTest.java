package at.splendit.simonykees.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.DiamondOperatorRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.DiamondOperatorASTVisitor;

/**
 * Testing diamond operator rule.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class DiamondRulesTest extends AbstractRulesTest {
	
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.diamondOperator";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/diamondOperator";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public DiamondRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new DiamondOperatorRule(DiamondOperatorASTVisitor.class));
	}
	
	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		return AbstractRulesTest.load(POSTRULE_DIRECTORY);
	}

	@Test
	public void testTransformation() throws Exception {
		Assert.fil();
		//super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
