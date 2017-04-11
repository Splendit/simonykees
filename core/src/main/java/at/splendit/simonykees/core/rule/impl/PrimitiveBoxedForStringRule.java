package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.PrimitiveBoxedForStringASTVisitor;

/**
 * @see PrimitiveBoxedForStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringRule extends RefactoringRule<PrimitiveBoxedForStringASTVisitor> {

	public PrimitiveBoxedForStringRule(Class<PrimitiveBoxedForStringASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.PrimitiveBoxedForStringRule_name;
		this.description = Messages.PrimitiveBoxedForStringRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_1;
	}

}
