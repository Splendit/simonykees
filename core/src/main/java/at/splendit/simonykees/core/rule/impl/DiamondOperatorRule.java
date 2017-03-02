package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.DiamondOperatorASTVisitor;

/**
 * @see DiamondOperatorASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorRule extends RefactoringRule<DiamondOperatorASTVisitor> {

	public DiamondOperatorRule(Class<DiamondOperatorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.DiamondOperatorRule_name;
		this.description = Messages.DiamondOperatorRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_7;
	}

}
