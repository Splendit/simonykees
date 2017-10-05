package eu.jsparrow.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class AllRulesTest extends AbstractRulesTest {

	public static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.allRules";
	public static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/allRules";

	private String fileName;
	private Path preRule;
	private Path postRule;

	public AllRulesTest(String fileName, Path preRule, Path postRule) {
		super();
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
		
		StandardLoggerRule standardLoggerRule = new StandardLoggerRule();
		Map<String, String> options = standardLoggerRule.getDefaultOptions();
		options.put("new-logging-statement", "error");
		standardLoggerRule.activateOptions(options);
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
