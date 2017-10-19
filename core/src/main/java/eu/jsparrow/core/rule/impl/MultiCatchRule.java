package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.trycatch.MultiCatchASTVisitor;
import eu.jsparrow.i18n.Messages;
/** 
 * @see MultiCatchASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class MultiCatchRule extends AbstractRefactoringRule<MultiCatchASTVisitor> {

	public MultiCatchRule() {
		super();
		this.visitorClass = MultiCatchASTVisitor.class;
		this.name = Messages.MultiCatchRule_name;
		this.description = Messages.MultiCatchRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}
}
