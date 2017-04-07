package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.RemoveToStringOnStringASTVisitor;

/**
 * @see RemoveToStringOnStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringRule extends RefactoringRule<RemoveToStringOnStringASTVisitor> {

	public RemoveToStringOnStringRule(Class<RemoveToStringOnStringASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.RemoveToStringOnStringRule_name;
		this.description = Messages.RemoveToStringOnStringRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_1;
	}

}
