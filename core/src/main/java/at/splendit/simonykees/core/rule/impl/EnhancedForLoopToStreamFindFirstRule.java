package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamFindFirstASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamFindFirstASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class EnhancedForLoopToStreamFindFirstRule extends RefactoringRule<EnhancedForLoopToStreamFindFirstASTVisitor> {

	public EnhancedForLoopToStreamFindFirstRule(Class<EnhancedForLoopToStreamFindFirstASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.EnhancedForLoopToStreamFindFirstRule_name;
		this.description = Messages.EnhancedForLoopToStreamFindFirstRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
