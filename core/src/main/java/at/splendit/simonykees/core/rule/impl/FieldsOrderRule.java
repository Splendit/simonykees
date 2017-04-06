package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.FieldsOrderASTVisitor;

/**
 * @see FieldsOrderASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.1
 */
public class FieldsOrderRule extends RefactoringRule<FieldsOrderASTVisitor> {

	public FieldsOrderRule(Class<FieldsOrderASTVisitor> visitor) {
		super(visitor);
		this.name = "Fields Order Rule";//FIXME SIM-340 find a better name
		this.description = "Members of classes and interfaces should appear in a predefiend order.";//FIXME SIM-340 write a better description
		this.requiredJavaVersion = JavaVersion.JAVA_0_9;
	}

}
