package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceLoggingWithForeignClassRule {
	private TestReplaceLoggingWithForeignClassRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Logger logger = LoggerFactory.getLogger(HelloWorld.class);
}