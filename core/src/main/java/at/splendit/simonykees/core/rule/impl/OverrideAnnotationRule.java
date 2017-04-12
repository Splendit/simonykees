package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.OverrideAnnotationRuleASTVisitor;

/**
 * 
 * @author Ardit Ymeri
 *
 */
public class OverrideAnnotationRule extends RefactoringRule<OverrideAnnotationRuleASTVisitor> {

	public OverrideAnnotationRule(Class<OverrideAnnotationRuleASTVisitor> visitor) {
		super(visitor);
		this.name = "@Override annotation rule";
		this.description = "@Override annotation should be used on overriding and imlementing methods";
		this.requiredJavaVersion = JavaVersion.JAVA_1_5;
	}

}
