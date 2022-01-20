package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamFindFirstASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see EnhancedForLoopToStreamFindFirstASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamFindFirstRule
		extends RefactoringRuleImpl<EnhancedForLoopToStreamFindFirstASTVisitor> {

	public static final String RULE_ID = "EnhancedForLoopToStreamFindFirst"; //$NON-NLS-1$

	public EnhancedForLoopToStreamFindFirstRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamFindFirstASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamFindFirstRule_name,
				Messages.EnhancedForLoopToStreamFindFirstRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
