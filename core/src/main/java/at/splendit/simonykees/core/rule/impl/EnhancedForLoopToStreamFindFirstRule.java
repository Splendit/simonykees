package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamFindFirstASTVisitor;

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
		this.name = "Enhanced For-Loop to Stream::findFirst";
		this.description = "Transforms enhanced for-loops which are only used for assigining a variable to a Stream::findFirst";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
