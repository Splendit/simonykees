package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.UseIsEmptyRuleASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see UseIsEmptyRuleASTVisitor
 * 
 * @author Martin Huter
 * @since 2.1.0
 */
public class UseIsEmptyRule extends RefactoringRule<UseIsEmptyRuleASTVisitor> {

	public UseIsEmptyRule(Class<UseIsEmptyRuleASTVisitor> visitor) {
		super(visitor);
		this.name = "Use isEmpty() instead of equals relation to zero";
		this.description = Messages.RemoveToStringOnStringRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		// string 1.6, collection 1.2, map 1.2
		return JavaVersion.JAVA_1_6;
	}
}
