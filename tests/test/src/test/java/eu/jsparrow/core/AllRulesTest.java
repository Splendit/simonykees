package eu.jsparrow.core;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.util.RulesTestUtil;
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
public class AllRulesTest extends AbstractRulesTest {

	public static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.allRules";
	public static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/allRules";

	public static Stream<Arguments> data() throws Exception {
		List<Arguments> data = new ArrayList<>();

		try (DirectoryStream<Path> directoryStream = Files
			.newDirectoryStream(Paths.get(RulesTestUtil.PRERULE_DIRECTORY), RulesTestUtil.RULE_SUFFIX)) {
			for (Path preRulePath : directoryStream) {
				/*
				 * FIXME remove this filter as soon as SIM-1826 is solved.
				 */
				if (!"TestUseComparatorMethodsRule.java".equals(preRulePath.getFileName()
					.toString())) {
					Path postRulePath = Paths.get(POSTRULE_DIRECTORY, preRulePath.getFileName()
						.toString());
					data.add(Arguments.of(preRulePath.getFileName()
						.toString(), preRulePath, postRulePath));
				}
			}
		}
		return data.stream();
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testTransformation(String fileName, Path preRule, Path postRule) throws Exception {
		super.testTransformation(postRule, preRule, fileName, POSTRULE_PACKAGE);
	}
}
