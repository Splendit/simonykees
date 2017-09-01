package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.ImmutableStaticFinalCollectionsASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsRule extends RefactoringRule<ImmutableStaticFinalCollectionsASTVisitor> {

	public ImmutableStaticFinalCollectionsRule(Class<ImmutableStaticFinalCollectionsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ImmutableStaticFinalCollectionsRule_name;
		this.description = Messages.ImmutableStaticFinalCollectionsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}

}
