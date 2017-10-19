package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsRule extends AbstractRefactoringRule<ImmutableStaticFinalCollectionsASTVisitor> {

	public ImmutableStaticFinalCollectionsRule() {
		super();
		this.visitorClass = ImmutableStaticFinalCollectionsASTVisitor.class;
		this.name = Messages.ImmutableStaticFinalCollectionsRule_name;
		this.description = Messages.ImmutableStaticFinalCollectionsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}

}
