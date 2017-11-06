package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.InefficientConstructorASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see InefficientConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorRule extends RefactoringRule<InefficientConstructorASTVisitor> {

	public InefficientConstructorRule() {
		super();
		this.visitorClass = InefficientConstructorASTVisitor.class;
		this.name = Messages.InefficientConstructorRule_name;
		this.description = Messages.InefficientConstructorRule_description;
		this.id = "InefficientConstructor"; //$NON-NLS-1$
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
}
