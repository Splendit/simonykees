package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveDoubleNegationASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class RemoveDoubleNegationRule extends RefactoringRuleImpl<RemoveDoubleNegationASTVisitor> {

	public RemoveDoubleNegationRule() {
		this.visitorClass = RemoveDoubleNegationASTVisitor.class;
		this.id = "RemoveDoubleNegationRule"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("name",
				"description", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}