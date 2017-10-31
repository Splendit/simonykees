package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see OverrideAnnotationRuleASTVisitor
 * 
 *      Required java version is {@value JavaVersion#JAVA_1_6} because with
 *      previous version it was not possible to annotate the methods inherited
 *      from the interfaces.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class OverrideAnnotationRule extends RefactoringRule<OverrideAnnotationRuleASTVisitor> {

	public OverrideAnnotationRule() {
		super();
		this.visitorClass = OverrideAnnotationRuleASTVisitor.class;
		this.name = Messages.OverrideAnnotationRule_name;
		this.description = Messages.OverrideAnnotationRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_6;
	}
}
