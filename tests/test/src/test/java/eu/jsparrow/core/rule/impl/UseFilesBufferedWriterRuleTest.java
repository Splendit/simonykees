package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseFilesBufferedWriterRuleTest {

	private UseFilesBufferedWriterRule rule;

	@BeforeEach
	public void setUp() {
		rule = new UseFilesBufferedWriterRule();
	}

	@Test
	public void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("UseFilesBufferedWriter"));
	}

	@Test
	public void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertEquals("Use Files.newBufferedWriter", description.getName());
		assertEquals(Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.PERFORMANCE, Tag.IO_OPERATIONS), description.getTags());
		assertEquals(5, description.getRemediationCost().toMinutes());
		assertThat(description.getDescription(), equalTo(
				"Java 7 introduced the 'java.nio.file.Files' class that contains some convenience methods for "
						+ "operating on files. This rule makes use of the 'Files.newBufferedWriter' method for initializing "
						+ "'BufferedWriter' objects to write text files in an efficient non-blocking manner."));
	}
}
