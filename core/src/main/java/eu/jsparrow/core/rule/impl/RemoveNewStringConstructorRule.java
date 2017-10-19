package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.impl.RemoveNewStringConstructorASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see RemoveNewStringConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveNewStringConstructorRule extends AbstractRefactoringRule<RemoveNewStringConstructorASTVisitor> {

	public RemoveNewStringConstructorRule() {
		super();
		this.visitorClass = RemoveNewStringConstructorASTVisitor.class;
		this.name = Messages.RemoveNewStringConstructorRule_name;
		this.description = Messages.RemoveNewStringConstructorRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
