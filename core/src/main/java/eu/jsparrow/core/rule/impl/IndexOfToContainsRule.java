package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.IndexOfToContainsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
public class IndexOfToContainsRule extends RefactoringRule<IndexOfToContainsASTVisitor> {

	public IndexOfToContainsRule() {
		super();
		this.visitorClass = IndexOfToContainsASTVisitor.class;
		this.name = Messages.IndexOfToContainsRule_name;
		this.description = Messages.IndexOfToContainsRule_description;
		this.id = "IndexOfToContains"; //$NON-NLS-1$
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5; // for lists 1.2, but for strings 1.5
	}

}
