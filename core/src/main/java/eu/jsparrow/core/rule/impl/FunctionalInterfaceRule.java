package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.functionalInterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;

/** 
 * @see FunctionalInterfaceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class FunctionalInterfaceRule extends RefactoringRule<FunctionalInterfaceASTVisitor> {

	public FunctionalInterfaceRule(Class<FunctionalInterfaceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.FunctionalInterfaceRule_name;
		this.description = Messages.FunctionalInterfaceRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
}
