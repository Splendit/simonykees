package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.i18n.Messages;
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
		this.name = Messages.RearrangeClassMembersRule_name;
		this.description = Messages.RearrangeClassMembersRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_0_9;
	}

}
