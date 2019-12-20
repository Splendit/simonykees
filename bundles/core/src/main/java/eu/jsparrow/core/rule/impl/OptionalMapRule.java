package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.optional.OptionalMapASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class OptionalMapRule extends RefactoringRuleImpl<OptionalMapASTVisitor> {

	public OptionalMapRule() {
		this.visitorClass = OptionalMapASTVisitor.class;
		this.id = "OptionalIfPresent"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Optional::map",
				"", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.CODING_CONVENTIONS, Tag.LAMBDA));
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
