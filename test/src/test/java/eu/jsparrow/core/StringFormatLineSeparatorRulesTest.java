package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.StringFormatLineSeparatorRule;
import eu.jsparrow.core.util.RulesTestUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.StringFormatLineSeparatorASTVisitor;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class StringFormatLineSeparatorRulesTest extends AbstractRulesTest {

	public static final String POSTRULE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule.stringFormat";
	public static final String POSTRULE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/stringFormat";

	private String fileName;
	private Path preRule, postRule;

	public StringFormatLineSeparatorRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
	}

	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		return AbstractRulesTest.load(POSTRULE_DIRECTORY);
	}

	@Test
	public void testTransformation() throws Exception {
		String expectedSource = new String(Files.readAllBytes(postRule), StandardCharsets.UTF_8);
		String content = new String(Files.readAllBytes(preRule), StandardCharsets.UTF_8);

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rulesList = new ArrayList<>();

		rulesList.add(new StringFormatLineSeparatorRule(StringFormatLineSeparatorASTVisitor.class));

		String compilationUnitSource = processFile(fileName, content, rulesList);

		// Replace the package for comparison
		compilationUnitSource = StringUtils.replace(compilationUnitSource, RulesTestUtil.PRERULE_PACKAGE,
				POSTRULE_PACKAGE);

		// TODO check if tabs and newlines make a difference
		assertEquals(expectedSource, compilationUnitSource);
	}
}