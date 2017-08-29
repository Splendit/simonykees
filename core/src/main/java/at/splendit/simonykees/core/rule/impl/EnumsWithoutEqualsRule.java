package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.EnumsWithoutEqualsASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see EnumsWithoutEqualsRuleASTVisitor
 * 
 * @author Hans-Jörg Schrödl 
 * @since 2.1.1
 */
public class EnumsWithoutEqualsRule extends RefactoringRule<EnumsWithoutEqualsASTVisitor> {

	
	public EnumsWithoutEqualsRule(Class<EnumsWithoutEqualsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.EnumsWithoutEqualsRule_name;
		this.description = Messages.EnumsWithoutEqualsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		// Enums exist since 1.5, 
		return JavaVersion.JAVA_1_5;
	}

	
}
