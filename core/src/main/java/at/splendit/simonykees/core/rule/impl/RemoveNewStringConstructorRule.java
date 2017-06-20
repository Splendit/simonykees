package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.RemoveNewStringConstructorASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see RemoveNewStringConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveNewStringConstructorRule extends RefactoringRule<RemoveNewStringConstructorASTVisitor> {

	public RemoveNewStringConstructorRule(Class<RemoveNewStringConstructorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.RemoveNewStringConstructorRule_name;
		this.description = Messages.RemoveNewStringConstructorRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
