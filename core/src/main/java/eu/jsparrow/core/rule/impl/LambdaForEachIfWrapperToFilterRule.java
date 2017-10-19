package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see LambdaForEachIfWrapperToFilterASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterRule extends AbstractRefactoringRule<LambdaForEachIfWrapperToFilterASTVisitor> {

	public LambdaForEachIfWrapperToFilterRule() {
		super();
		this.visitorClass = LambdaForEachIfWrapperToFilterASTVisitor.class;
		this.name = Messages.LambdaForEachIfWrapperToFilterRule_name;
		this.description = Messages.LambdaForEachIfWrapperToFilterRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
