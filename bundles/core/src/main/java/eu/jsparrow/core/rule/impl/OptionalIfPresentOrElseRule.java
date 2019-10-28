package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.optional.OptionalIfPresentOrElseASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see OptionalIfPresentOrElseASTVisitor
 * 
 * @since 3.10.0
 *
 */
public class OptionalIfPresentOrElseRule extends RefactoringRuleImpl<OptionalIfPresentOrElseASTVisitor>{
	
	public OptionalIfPresentOrElseRule() {
		this.visitorClass = OptionalIfPresentOrElseASTVisitor.class;
		this.id = "OptionalIfPresentOrElse"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.OptionalIfPresentOrElseRule_name,
				Messages.OptionalIfPresentOrElseRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_9, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.LAMBDA));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return  JavaCore.VERSION_9;
	}

}
