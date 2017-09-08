package at.splendit.simonykees.core.rule.impl;


import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.PrimitiveObjectUseEqualsASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * This rule replaces ==, != when called on primitive objects with equals. 
 * 
 * @see PrimitiveObjectUseEqualsASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class PrimitiveObjectUseEqualsRule extends RefactoringRule<PrimitiveObjectUseEqualsASTVisitor> {

	public PrimitiveObjectUseEqualsRule(Class<PrimitiveObjectUseEqualsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.PrimitiveObjectUseEqualsRule_name;
		this.description = Messages.PrimitiveObjectUseEqualsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
