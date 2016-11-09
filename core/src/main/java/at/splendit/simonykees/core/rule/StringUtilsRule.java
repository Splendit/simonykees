package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;
/** 
 * @see StringUtilsASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class StringUtilsRule extends RefactoringRule<StringUtilsASTVisitor> {

	public StringUtilsRule(Class<StringUtilsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringUtilsRule_name;
		this.description = Messages.StringUtilsRule_description;
	}
}
