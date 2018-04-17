package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see LambdaForEachIfWrapperToFilterASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterRule extends RefactoringRule<LambdaForEachIfWrapperToFilterASTVisitor> {

	public LambdaForEachIfWrapperToFilterRule() {
		super();
		this.visitorClass = LambdaForEachIfWrapperToFilterASTVisitor.class;
		this.id = "LambdaForEachIfWrapperToFilter"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.LambdaForEachIfWrapperToFilterRule_name,
				Messages.LambdaForEachIfWrapperToFilterRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
