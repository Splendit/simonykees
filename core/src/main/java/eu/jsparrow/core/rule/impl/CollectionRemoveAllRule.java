package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.CollectionRemoveAllASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see CollectionRemoveAllASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class CollectionRemoveAllRule extends RefactoringRule<CollectionRemoveAllASTVisitor> {

	public CollectionRemoveAllRule() {
		super();
		this.visitorClass = CollectionRemoveAllASTVisitor.class;
		this.name = Messages.CollectionRemoveAllRule_name;
		this.description = Messages.CollectionRemoveAllRule_description;
		this.id = "CollectionRemoveAll"; //$NON-NLS-1$
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}
}
