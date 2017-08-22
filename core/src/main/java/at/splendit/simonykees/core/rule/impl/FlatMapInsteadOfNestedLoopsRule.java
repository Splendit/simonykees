package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.FlatMapInsteadOfNestedLoopsASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.0.4
 */
public class FlatMapInsteadOfNestedLoopsRule extends RefactoringRule<FlatMapInsteadOfNestedLoopsASTVisitor> {
	public FlatMapInsteadOfNestedLoopsRule(Class<FlatMapInsteadOfNestedLoopsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.FlatMapInsteadOfNestedLoopsRule_name;
		this.description = Messages.FlatMapInsteadOfNestedLoopsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
}
