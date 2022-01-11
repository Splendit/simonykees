package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see EnhancedForLoopToStreamAnyMatchASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchRule
		extends RefactoringRuleImpl<EnhancedForLoopToStreamAnyMatchASTVisitor> {

	public static final String RULE_ID = "EnhancedForLoopToStreamAnyMatch"; //$NON-NLS-1$

	public EnhancedForLoopToStreamAnyMatchRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamAnyMatchASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamAnyMatchRule_name,
				Messages.EnhancedForLoopToStreamAnyMatchRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
