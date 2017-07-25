package at.splendit.simonykees.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.RemoveToStringOnStringRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.RemoveToStringOnStringASTVisitor;

/**
 * Tests the Simonykees-Rule {@link RemoveToStringOnStringRule}
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class RemoveToStringRulesTest extends AbstractRulesTest {

	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.toString";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/toString";

	private String fileName;
	private Path preRule, postRule;

	public RemoveToStringRulesTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new RemoveToStringOnStringRule(RemoveToStringOnStringASTVisitor.class));
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
