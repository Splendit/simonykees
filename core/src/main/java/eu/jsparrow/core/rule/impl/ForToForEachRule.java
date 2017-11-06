package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule() {
		super();
		this.visitorClass = ForToForEachASTVisitor.class;
		this.name = Messages.ForToForEachRule_name;
		this.description = Messages.ForToForEachRule_description;
		this.id = "ForToForEach"; //$NON-NLS-1$
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
}
