package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsRule extends RefactoringRule<ImmutableStaticFinalCollectionsASTVisitor> {

	public ImmutableStaticFinalCollectionsRule() {
		super();
		this.visitor = ImmutableStaticFinalCollectionsASTVisitor.class;
		this.name = Messages.ImmutableStaticFinalCollectionsRule_name;
		this.description = Messages.ImmutableStaticFinalCollectionsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}

}
