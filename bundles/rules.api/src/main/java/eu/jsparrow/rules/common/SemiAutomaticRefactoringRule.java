package eu.jsparrow.rules.common;

import java.util.Map;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;


/**
 * A super class for all the rules that require some user interaction.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 * @param <T>
 *            type of the visitor that creates the changes for this rule.
 */
public abstract class SemiAutomaticRefactoringRule<T extends AbstractASTRewriteASTVisitor> extends RefactoringRuleImpl<T> {

	public SemiAutomaticRefactoringRule() {
		super();
	}

	/**
	 * Sets the default options of the user interaction.
	 */
	protected abstract void activateDefaultOptions();

	/**
	 * Sets the given options of the user interaction.
	 */
	protected abstract void activateOptions(Map<String, String> options);
}
