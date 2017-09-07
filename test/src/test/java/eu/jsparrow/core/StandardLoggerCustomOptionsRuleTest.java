package eu.jsparrow.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.impl.standardLogger.StandardLoggerConstants;
import eu.jsparrow.core.rule.impl.standardLogger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.semiAutomatic.StandardLoggerASTVisitor;

@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class StandardLoggerCustomOptionsRuleTest extends AbstractRulesTest {
	
	private static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.standardLoggerCustomOptions";
	private static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/standardLoggerCustomOptions";

	private String fileName;
	private Path preRule;
	private Path postRule;
	
	public StandardLoggerCustomOptionsRuleTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		
		StandardLoggerRule standardLoggerRule = new StandardLoggerRule(StandardLoggerASTVisitor.class);
				
		Map<String, String> selectedOptions = new HashMap<>();
		selectedOptions.put(StandardLoggerConstants.SYSTEM_OUT_PRINT, ""); // -->> Leave as is
		selectedOptions.put(StandardLoggerConstants.SYSTEM_ERR_PRINT, "debug");
		selectedOptions.put(StandardLoggerConstants.PRINT_STACKTRACE, "warn");
		
		standardLoggerRule.setSelectedOptions(selectedOptions);
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
