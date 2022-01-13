package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see LambdaForEachIfWrapperToFilterASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterRule extends RefactoringRuleImpl<LambdaForEachIfWrapperToFilterASTVisitor> {

	public static final String RULE_ID = "LambdaForEachIfWrapperToFilter"; //$NON-NLS-1$

	public LambdaForEachIfWrapperToFilterRule() {
		super();
		this.visitorClass = LambdaForEachIfWrapperToFilterASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.LambdaForEachIfWrapperToFilterRule_name,
				Messages.LambdaForEachIfWrapperToFilterRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
