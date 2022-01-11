package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see EnhancedForLoopToStreamForEachASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachRule extends RefactoringRuleImpl<EnhancedForLoopToStreamForEachASTVisitor> {

	public static final String RULE_ID = "EnhancedForLoopToStreamForEach"; //$NON-NLS-1$ 
	public EnhancedForLoopToStreamForEachRule() {
		this.visitorClass = EnhancedForLoopToStreamForEachASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.EnhancedForLoopToStreamForEachRule_name,
				Messages.EnhancedForLoopToStreamForEachRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
