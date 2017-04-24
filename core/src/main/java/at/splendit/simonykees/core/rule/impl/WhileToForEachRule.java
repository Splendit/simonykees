package at.splendit.simonykees.core.rule.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.loop.WhileToForEachASTVisitor;
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
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_5));
	}
}
