package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.renaming.FieldNameConventionASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see FieldNameConventionASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class FieldNameConventionRule extends RefactoringRule<FieldNameConventionASTVisitor> {

	public FieldNameConventionRule(Class<FieldNameConventionASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.FieldNameConventionRule_name;
		this.description = Messages.FieldNameConventionRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
