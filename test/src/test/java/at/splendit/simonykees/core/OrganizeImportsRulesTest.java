package at.splendit.simonykees.core;

import java.nio.file.Path;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class OrganizeImportsRulesTest extends AbstractRulesTest {
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.organiseImports";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/organiseImports";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public OrganizeImportsRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		rulesList.add(new OrganiseImportsRule(AbstractASTRewriteASTVisitor.class));
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
