package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends AbstractRefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule() {
		super();
		this.visitorClass = ForToForEachASTVisitor.class;
		this.name = Messages.ForToForEachRule_name;
		this.description = Messages.ForToForEachRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
}
