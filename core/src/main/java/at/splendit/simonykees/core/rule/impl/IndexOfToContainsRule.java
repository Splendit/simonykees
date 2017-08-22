package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.IndexOfToContainsASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
public class IndexOfToContainsRule extends RefactoringRule<IndexOfToContainsASTVisitor> {

	public IndexOfToContainsRule(Class<IndexOfToContainsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.IndexOfToContainsRule_name;
		this.description = Messages.IndexOfToContainsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5; // for lists 1.2, but for strings 1.5
	}

}
