package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/** 
 * @see ArithmethicAssignmentASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class ArithmethicAssignmentRule extends RefactoringRule<ArithmethicAssignmentASTVisitor> {

	public ArithmethicAssignmentRule(Class<ArithmethicAssignmentASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ArithmethicAssignmentRule_name;
		this.description = Messages.ArithmethicAssignmentRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_4;
	}
}
