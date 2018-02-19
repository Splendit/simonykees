package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * @see EnhancedForLoopToStreamForEachASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachRule extends RefactoringRule<EnhancedForLoopToStreamForEachASTVisitor> {

	public EnhancedForLoopToStreamForEachRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamForEachASTVisitor.class;
		this.id = "EnhancedForLoopToStreamForEach"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamForEachRule_name,
				Messages.EnhancedForLoopToStreamForEachRule_description, Duration.ofMinutes(15),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
