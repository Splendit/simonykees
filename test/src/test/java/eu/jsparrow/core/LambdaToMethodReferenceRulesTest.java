package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.LambdaToMethodReferenceRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.LambdaToMethodReferenceASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class LambdaToMethodReferenceRulesTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.lambdaToMethodReference";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/lambdaToMethodReference";

	private String fileName;
	private Path preRule;
	private Path postRule;

	public LambdaToMethodReferenceRulesTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new LambdaToMethodReferenceRule(LambdaToMethodReferenceASTVisitor.class));
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
