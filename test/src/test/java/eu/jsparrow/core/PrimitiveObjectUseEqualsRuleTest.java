package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.PrimitiveObjectUseEqualsRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.PrimitiveObjectUseEqualsASTVisitor;

@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class PrimitiveObjectUseEqualsRuleTest extends AbstractRulesTest {

	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.primitiveObjectUseEquals";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY
			+ "/postRule/primitiveObjectUseEquals";

	private String fileName;
	private Path preRule, postRule;

	public PrimitiveObjectUseEqualsRuleTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new PrimitiveObjectUseEqualsRule(PrimitiveObjectUseEqualsASTVisitor.class));
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
