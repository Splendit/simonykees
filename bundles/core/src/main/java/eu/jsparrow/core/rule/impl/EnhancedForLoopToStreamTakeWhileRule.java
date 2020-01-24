package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamTakeWhileASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @see EnhancedForLoopToStreamTakeWhileASTVisitor
 * 
 * @since 3.7.0
 *
 */
public class EnhancedForLoopToStreamTakeWhileRule extends RefactoringRuleImpl<EnhancedForLoopToStreamTakeWhileASTVisitor> {
	
	public EnhancedForLoopToStreamTakeWhileRule() {
		this.visitorClass = EnhancedForLoopToStreamTakeWhileASTVisitor.class;
		this.id = "EnhancedForLoopToStreamTakeWhile"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamTakeWhileRule_name,
				Messages.EnhancedForLoopToStreamTakeWhileRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_9, Tag.LOOP, Tag.LAMBDA));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_9;
	}

}
