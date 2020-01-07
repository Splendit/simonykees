package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.optional.OptionalMapASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @see OptionalMapASTVisitor
 * 
 * @since 3.13.0
 *
 */
public class OptionalMapRule extends RefactoringRuleImpl<OptionalMapASTVisitor> {

	public OptionalMapRule() {
		this.visitorClass = OptionalMapASTVisitor.class;
		this.id = "OptionalMap"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.OptionalMapRule_name,
				Messages.OptionalMapRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.CODING_CONVENTIONS, Tag.LAMBDA, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
