package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.ReImplementingInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceRule extends RefactoringRule<ReImplementingInterfaceASTVisitor> {

	public ReImplementingInterfaceRule() {
		super();
		this.visitorClass = ReImplementingInterfaceASTVisitor.class;
		this.name = Messages.ReImplementingInterfaceRule_name;
		this.description = Messages.ReImplementingInterfaceRule_description;
		this.id = "ReImplementingInterface"; //$NON-NLS-1$
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
