package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see LambdaToMethodReferenceASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceRule extends RefactoringRuleImpl<LambdaToMethodReferenceASTVisitor> {
	public LambdaToMethodReferenceRule() {
		super();
		this.visitorClass = LambdaToMethodReferenceASTVisitor.class;
		this.id = "LambdaToMethodReference"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.LambdaToMethodReferenceRule_name,
				Messages.LambdaToMethodReferenceRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

	@Override
	public boolean isFree() {
		return true;
	}
}
