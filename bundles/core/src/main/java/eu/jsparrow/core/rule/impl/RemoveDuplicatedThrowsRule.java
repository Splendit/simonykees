package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveDuplicatedThrowsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveDuplicatedThrowsASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class RemoveDuplicatedThrowsRule extends RefactoringRuleImpl<RemoveDuplicatedThrowsASTVisitor> {

	public RemoveDuplicatedThrowsRule() {
		this.visitorClass = RemoveDuplicatedThrowsASTVisitor.class;
		this.id = "RemoveDuplicatedThrows"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveDuplicatedThrowsRule_name, Messages.RemoveDuplicatedThrowsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
