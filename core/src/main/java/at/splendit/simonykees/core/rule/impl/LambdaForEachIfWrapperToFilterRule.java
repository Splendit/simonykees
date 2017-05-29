package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.LambdaForEachIfWrapperToFilterASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see LambdaForEachIfWrapperToFilterASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterRule extends RefactoringRule<LambdaForEachIfWrapperToFilterASTVisitor> {

	public LambdaForEachIfWrapperToFilterRule(Class<LambdaForEachIfWrapperToFilterASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.LambdaForEachIfWrapperToFilterRule_name;
		this.description = Messages.LambdaForEachIfWrapperToFilterRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
