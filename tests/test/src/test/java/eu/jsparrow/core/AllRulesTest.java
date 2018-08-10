package eu.jsparrow.core;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.java10.LocalVariableTypeInferenceRule;

/**
 * Parameterized tests for applying all rules in
 * {@link RulesContainer#getAllRules(boolean)} in the sample project.
 * {@link LocalVariableTypeInferenceRule} is temporary skipped until we upgrade
 * to Java 10.
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
	private Path preRule;
	private Path postRule;

	public AllRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;

		List<RefactoringRule> allRules = RulesContainer.getAllRules(false)
			.stream()
			/*
			 * we cannot apply Local Variable Type Inference rule until we
			 * upgrade to java 10.
			 */
			.filter(r -> !r.getId()
				.equals("LocalVariableTypeInference"))
			.collect(Collectors.toList());

		StandardLoggerRule standardLoggerRule = new StandardLoggerRule();
		Map<String, String> options = standardLoggerRule.getDefaultOptions();
		options.put("new-logging-statement", "error");
		options.put("system-out-print-exception", "error");
		standardLoggerRule.activateOptions(options);
		rulesList.add(standardLoggerRule);
		rulesList.addAll(allRules);
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

		try (DirectoryStream<Path> directoryStream = Files
			.newDirectoryStream(Paths.get(RulesTestUtil.PRERULE_DIRECTORY), RulesTestUtil.RULE_SUFFIX)) {
			for (Path preRulePath : directoryStream) {
				Path postRulePath = Paths.get(POSTRULE_DIRECTORY, preRulePath.getFileName()
					.toString());
				data.add(new Object[] { preRulePath.getFileName()
					.toString(), preRulePath, postRulePath });
			}
		}
		return data;
	}

	@Test
	public void testTransformation() throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
