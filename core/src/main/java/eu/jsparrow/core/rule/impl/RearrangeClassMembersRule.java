package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.impl.RearrangeClassMembersASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see RearrangeClassMembersASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.1
 */
public class RearrangeClassMembersRule extends AbstractRefactoringRule<RearrangeClassMembersASTVisitor> {

	public RearrangeClassMembersRule() {
		super();
		this.visitorClass = RearrangeClassMembersASTVisitor.class;
		this.name = Messages.RearrangeClassMembersRule_name;
		this.description = Messages.RearrangeClassMembersRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
