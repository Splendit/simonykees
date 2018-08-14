package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamSumASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see EnhancedForLoopToStreamSumASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 */
public class EnhancedForLoopToStreamSumRule extends RefactoringRuleImpl<EnhancedForLoopToStreamSumASTVisitor> {

	public EnhancedForLoopToStreamSumRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamSumASTVisitor.class;
		this.id = "EnhancedForLoopToStreamSum"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamSumRule_name,
				Messages.EnhancedForLoopToStreamSumRule_name, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
