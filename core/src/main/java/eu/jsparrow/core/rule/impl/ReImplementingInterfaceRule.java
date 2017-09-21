package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.core.visitor.ReImplementingInterfaceASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceRule extends RefactoringRule<ReImplementingInterfaceASTVisitor> {

	public ReImplementingInterfaceRule(Class<ReImplementingInterfaceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ReImplementingInterfaceRule_name;
		this.description = Messages.ReImplementingInterfaceRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
