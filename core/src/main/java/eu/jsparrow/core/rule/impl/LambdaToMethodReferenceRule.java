package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see LambdaToMethodReferenceASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceRule extends RefactoringRule<LambdaToMethodReferenceASTVisitor> {
	public LambdaToMethodReferenceRule() {
		super();
		this.visitorClass = LambdaToMethodReferenceASTVisitor.class;
		this.name = Messages.LambdaToMethodReferenceRule_name;
		this.description = Messages.LambdaToMethodReferenceRule_description;
		this.id = "LambdaToMethodReference"; //$NON-NLS-1$
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
}
