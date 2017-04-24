package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.FieldNameConventionASTVisitor;

/**
 * 
 * @author Ardit Ymeri
 *
 */
public class FieldNameConventionRule extends RefactoringRule<FieldNameConventionASTVisitor> {

	public FieldNameConventionRule(Class<FieldNameConventionASTVisitor> visitor) {
		super(visitor);
		this.name = "";
		this.description = "";
		this.requiredJavaVersion = JavaVersion.JAVA_0_9;
	}

}
