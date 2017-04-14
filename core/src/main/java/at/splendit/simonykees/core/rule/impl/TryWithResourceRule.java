package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.tryStatement.TryWithResourceASTVisitor;
/** 
 * @see TryWithResourceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class TryWithResourceRule extends RefactoringRule<TryWithResourceASTVisitor> {

	public TryWithResourceRule(Class<TryWithResourceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.TryWithResourceRule_name;
		this.description = Messages.TryWithResourceRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_7;
	}

}
