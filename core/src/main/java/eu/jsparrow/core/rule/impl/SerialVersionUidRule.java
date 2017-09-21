package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.SerialVersionUidASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see SerialVersionUidRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class SerialVersionUidRule extends RefactoringRule<SerialVersionUidASTVisitor> {

	public SerialVersionUidRule(Class<SerialVersionUidASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.SerialVersionUidRule_name;
		this.description = Messages.SerialVersionUidRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
