package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamForEachASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamForEachASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachRule extends RefactoringRule<EnhancedForLoopToStreamForEachASTVisitor> {

	public EnhancedForLoopToStreamForEachRule(Class<EnhancedForLoopToStreamForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.EnhancedForLoopToStreamForEachRule_name;
		this.description = Messages.EnhancedForLoopToStreamForEachRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
