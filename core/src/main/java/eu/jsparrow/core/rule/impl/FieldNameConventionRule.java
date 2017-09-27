package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.renaming.FieldNameConventionASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see FieldNameConventionASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class FieldNameConventionRule extends RefactoringRule<FieldNameConventionASTVisitor> {

	public FieldNameConventionRule() {
		super();
		this.visitor = FieldNameConventionASTVisitor.class;
		this.name = Messages.FieldNameConventionRule_name;
		this.description = Messages.FieldNameConventionRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
