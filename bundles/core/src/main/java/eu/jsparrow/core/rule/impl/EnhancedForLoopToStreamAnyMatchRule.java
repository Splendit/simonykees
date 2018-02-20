package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see EnhancedForLoopToStreamAnyMatchASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchRule extends RefactoringRule<EnhancedForLoopToStreamAnyMatchASTVisitor> {

	public EnhancedForLoopToStreamAnyMatchRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamAnyMatchASTVisitor.class;
		this.id = "EnhancedForLoopToStreamAnyMatch"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamAnyMatchRule_name,
				Messages.EnhancedForLoopToStreamAnyMatchRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
