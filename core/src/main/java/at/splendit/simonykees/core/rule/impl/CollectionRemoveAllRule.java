package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.CollectionRemoveAllASTVisitor;

/**
 * @see CollectionRemoveAllASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class CollectionRemoveAllRule extends RefactoringRule<CollectionRemoveAllASTVisitor> {

	public CollectionRemoveAllRule(Class<CollectionRemoveAllASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.CollectionRemoveAllRule_name;
		this.description = Messages.CollectionRemoveAllRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_2;
	}

}
