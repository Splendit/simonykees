package at.splendit.simonykees.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.util.RulesTestUtil;
import at.splendit.simonykees.core.visitor.semiAutomatic.StandardLoggerASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Ardit Ymeri
 * @since 0.9
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class AllRulesTest extends AbstractRulesTest {

	public static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.allRules";
	public static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/allRules";

	private String fileName;
	private Path preRule, postRule;

	public AllRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		
		StandardLoggerRule standardLoggerRule = new StandardLoggerRule(StandardLoggerASTVisitor.class);
		standardLoggerRule.activateDefaultOptions();
		rulesList.add(standardLoggerRule);
		
		rulesList.addAll(RulesContainer.getAllRules());
	}

	/**
	 * All files in the preRule package are matched with its corresponding
	 * allRules match. If an preRule File exists with no postRule complement
	 * there is an file not found exception raised. This assures that a postRule
	 * file for each preRule File exists in case of the allRules test
	 * 
	 * @return the object array list used for tests
	 * @throws Exception
	 *             junit test default
	 */
	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> data = new ArrayList<>();
		for (Path preRulePath : Files.newDirectoryStream(Paths.get(RulesTestUtil.PRERULE_DIRECTORY),
				RulesTestUtil.RULE_SUFFIX)) {
			Path postRulePath = Paths.get(POSTRULE_DIRECTORY, preRulePath.getFileName().toString());
			data.add(new Object[] { preRulePath.getFileName().toString(), preRulePath, postRulePath });
		}
		return data;
	}
	
	@Test
	public void testTransformation() throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
