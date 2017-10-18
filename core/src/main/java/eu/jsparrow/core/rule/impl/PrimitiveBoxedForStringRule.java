package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.PrimitiveBoxedForStringASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see PrimitiveBoxedForStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringRule extends RefactoringRule<PrimitiveBoxedForStringASTVisitor> {

	public PrimitiveBoxedForStringRule() {
		super();
		this.visitorClass = PrimitiveBoxedForStringASTVisitor.class;
		this.name = Messages.PrimitiveBoxedForStringRule_name;
		this.description = Messages.PrimitiveBoxedForStringRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
