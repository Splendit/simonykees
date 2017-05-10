package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.MultiVariableDeclarationLineASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see MultiVariableDeclarationLineASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class MultiVariableDeclarationLineRule extends RefactoringRule<MultiVariableDeclarationLineASTVisitor> {

	public MultiVariableDeclarationLineRule(Class<MultiVariableDeclarationLineASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.MultiVariableDeclarationLineRule_name;
		this.description = Messages.MultiVariableDeclarationLineRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_0_9;
	}
}
