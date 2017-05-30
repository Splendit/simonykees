package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.loop.whileToForEach.WhileToForEachASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see WhileToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class WhileToForEachRule extends RefactoringRule<WhileToForEachASTVisitor> {
	
	Logger logger = LoggerFactory.getLogger(WhileToForEachASTVisitor.class);

	public WhileToForEachRule(Class<WhileToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.WhileToForEachRule_name;
		this.description = Messages.WhileToForEachRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
}
