package eu.jsparrow.core.rule.impl;


import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.core.visitor.PrimitiveObjectUseEqualsASTVisitor;

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
