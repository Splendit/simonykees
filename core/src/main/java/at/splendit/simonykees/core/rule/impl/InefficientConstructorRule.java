package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.InefficientConstructorASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see InefficientConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorRule extends RefactoringRule<InefficientConstructorASTVisitor> {

	public InefficientConstructorRule(Class<InefficientConstructorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.InefficientConstructorRule_name;
		this.description = Messages.InefficientConstructorRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
}
