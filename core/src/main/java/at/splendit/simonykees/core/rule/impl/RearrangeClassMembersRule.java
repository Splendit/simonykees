package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.RearrangeClassMembersASTVisitor;

/**
 * @see RearrangeClassMembersASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.1
 */
public class RearrangeClassMembersRule extends RefactoringRule<RearrangeClassMembersASTVisitor> {

	public RearrangeClassMembersRule(Class<RearrangeClassMembersASTVisitor> visitor) {
		super(visitor);
		this.name = "Rearrange class members";
		this.description = "Members of classes and interfaces should appear in a predefiend order.";
		this.requiredJavaVersion = JavaVersion.JAVA_0_9;
	}

}
