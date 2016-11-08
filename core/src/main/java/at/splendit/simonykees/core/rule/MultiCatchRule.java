package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.tryStatement.MultiCatchASTVisitor;
/** 
 * @see MultiCatchASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class MultiCatchRule extends RefactoringRule<MultiCatchASTVisitor> {

	public MultiCatchRule(Class<MultiCatchASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.MultiCatchRule_name;
		this.description = Messages.MultiCatchRule_description;
	}

}
