package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.RemoveToStringOnStringASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see RemoveToStringOnStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringRule extends RefactoringRule<RemoveToStringOnStringASTVisitor> {

	public RemoveToStringOnStringRule() {
		super();
		this.visitor = RemoveToStringOnStringASTVisitor.class;
		this.name = Messages.RemoveToStringOnStringRule_name;
		this.description = Messages.RemoveToStringOnStringRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
