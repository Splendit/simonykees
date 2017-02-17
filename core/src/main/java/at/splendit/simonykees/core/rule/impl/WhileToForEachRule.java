package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.loop.WhileToForEachASTVisitor;

/**
 * @see WhileToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class WhileToForEachRule extends RefactoringRule<WhileToForEachASTVisitor> {

	public WhileToForEachRule(Class<WhileToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.WhileToForRule_name;
		this.description = Messages.WhileToForRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_5;
	}

}
