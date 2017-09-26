package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.DiamondOperatorRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.DiamondOperatorASTVisitor;

/**
 * Testing diamond operator rule.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class DiamondRulesJ7Test extends AbstractRulesTest {
	
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.diamondOperatorJ7";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/diamondOperatorJ7";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	static {		
		javaVersion = JavaCore.VERSION_1_7;
	}
	
	public DiamondRulesJ7Test(String fileName, Path preRule, Path postRule) {
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
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
