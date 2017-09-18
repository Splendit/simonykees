package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.semiautomatic.StandardLoggerASTVisitor;

/**
 * Testing standard logger rule.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class StandardLoggerRuleSlf4jTest extends AbstractRulesTest {
	
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.standardLoggerSlf4j";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/standardLoggerSlf4j";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public StandardLoggerRuleSlf4jTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		
		StandardLoggerRule standardLoggerRule = new StandardLoggerRule(StandardLoggerASTVisitor.class);
		standardLoggerRule.activateDefaultOptions();
		rulesList.add(standardLoggerRule);
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
