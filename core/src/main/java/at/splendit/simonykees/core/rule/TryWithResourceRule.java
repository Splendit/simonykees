package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
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
	}

}
