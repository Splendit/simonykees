package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.i18n.Messages;

/** 
 * @see BracketsToControlASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class BracketsToControlRule extends AbstractRefactoringRule<BracketsToControlASTVisitor> {

	public BracketsToControlRule() {
		super();
		this.visitorClass = BracketsToControlASTVisitor.class;
		this.name = Messages.BracketsToControlRule_name;
		this.description = Messages.BracketsToControlRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
