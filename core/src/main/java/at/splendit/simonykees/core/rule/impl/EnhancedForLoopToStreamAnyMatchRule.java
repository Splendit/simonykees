package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamAnyMatchASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamAnyMatchASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchRule extends RefactoringRule<EnhancedForLoopToStreamAnyMatchASTVisitor> {

	public EnhancedForLoopToStreamAnyMatchRule(Class<EnhancedForLoopToStreamAnyMatchASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.EnhancedForLoopToStreamAnyMatchRule_name;
		this.description = Messages.EnhancedForLoopToStreamAnyMatchRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
