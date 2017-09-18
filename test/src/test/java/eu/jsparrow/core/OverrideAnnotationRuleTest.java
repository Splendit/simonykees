package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.OverrideAnnotationRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.OverrideAnnotationRuleASTVisitor;

@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class OverrideAnnotationRuleTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.overrideAnnotation";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/overrideAnnotation";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public OverrideAnnotationRuleTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new  OverrideAnnotationRule(OverrideAnnotationRuleASTVisitor.class));
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
