package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitTimeoutAnnotationPropertyASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReplaceJUnitTimeoutAnnotationPropertyRule 
	extends RefactoringRuleImpl<ReplaceJUnitTimeoutAnnotationPropertyASTVisitor> {
	
	public ReplaceJUnitTimeoutAnnotationPropertyRule() {
		this.visitorClass = ReplaceJUnitTimeoutAnnotationPropertyASTVisitor.class;
		this.id = "ReplaceJUnitTimeoutAnnotationProperty"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				"Replace JUnit Timeout Annotation Property with assertTimeout",
				"",
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.LAMBDA, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return null;
	}

}
