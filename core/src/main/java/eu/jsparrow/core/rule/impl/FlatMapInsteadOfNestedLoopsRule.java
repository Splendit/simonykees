package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.FlatMapInsteadOfNestedLoopsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class FlatMapInsteadOfNestedLoopsRule extends RefactoringRule<FlatMapInsteadOfNestedLoopsASTVisitor> {

	public FlatMapInsteadOfNestedLoopsRule() {
		super();
		this.visitor = FlatMapInsteadOfNestedLoopsASTVisitor.class;
		this.name = Messages.FlatMapInsteadOfNestedLoopsRule_name;
		this.description = Messages.FlatMapInsteadOfNestedLoopsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
}
