package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.impl.UseIsEmptyRuleASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see UseIsEmptyRuleASTVisitor
 * 
 * @author Martin Huter
 * @since 2.1.0
 */
public class UseIsEmptyRule extends AbstractRefactoringRule<UseIsEmptyRuleASTVisitor> {

	public UseIsEmptyRule() {
		super();
		this.visitorClass = UseIsEmptyRuleASTVisitor.class;
		this.name = Messages.UseIsEmptyRule_name;
		this.description = Messages.UseIsEmptyRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		// string 1.6, collection 1.2, map 1.2
		return JavaVersion.JAVA_1_6;
	}
}
