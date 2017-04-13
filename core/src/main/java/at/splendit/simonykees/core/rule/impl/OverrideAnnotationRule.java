package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.OverrideAnnotationRuleASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see OverrideAnnotationRuleASTVisitor
 * 
 * Required java version is {@value JavaVersion#JAVA_1_6} because
 * with previous version it was not possible to annotate the methods
 * inherited from the interfaces.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class OverrideAnnotationRule extends RefactoringRule<OverrideAnnotationRuleASTVisitor> {

	public OverrideAnnotationRule(Class<OverrideAnnotationRuleASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.OverrideAnnotationRule_name;
		this.description = Messages.OverrideAnnotationRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_6;
	}

}
