package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.loop.ForToForEachASTVisitor;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule(Class<ForToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ForToForEachRule_name;
		this.description = Messages.ForToForEachRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_5;
	}

}
