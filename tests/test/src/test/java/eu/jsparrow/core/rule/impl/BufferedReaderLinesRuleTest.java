package eu.jsparrow.core.rule.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class BufferedReaderLinesRuleTest {

	private BufferedReaderLinesRule rule;

	@BeforeEach
	public void setUp() {
		rule = new BufferedReaderLinesRule();
	}

	@Test
	public void test_ruleId() {
		assertEquals("BufferedReaderLines", rule.getId());
	}

	@Test
	public void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use BufferedReader::lines", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.IO_OPERATIONS),
				description.getTags());
		assertEquals(5, description.getRemediationCost()
			.toMinutes());
		assertEquals("Replaces loops iterating over lines of a file by BufferedReader::lines stream.",
				description.getDescription());
	}
}
