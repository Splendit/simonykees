package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.MultiVariableDeclarationLineASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see MultiVariableDeclarationLineASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class MultiVariableDeclarationLineRule extends RefactoringRule<MultiVariableDeclarationLineASTVisitor> {

	public MultiVariableDeclarationLineRule() {
		super();
		this.visitor = MultiVariableDeclarationLineASTVisitor.class;
		this.name = Messages.MultiVariableDeclarationLineRule_name;
		this.description = Messages.MultiVariableDeclarationLineRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
