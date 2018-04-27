package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

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

	public EnhancedForLoopToStreamFindFirstRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamFindFirstASTVisitor.class;
		this.id = "EnhancedForLoopToStreamFindFirst"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamFindFirstRule_name,
				Messages.EnhancedForLoopToStreamFindFirstRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
