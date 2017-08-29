package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamSumASTVisitor;

/***
 * 
 * @author Ardit Ymeri
 *
 */
public class EnhancedForLoopToStreamSumRule extends RefactoringRule<EnhancedForLoopToStreamSumASTVisitor> {

	public EnhancedForLoopToStreamSumRule(Class<EnhancedForLoopToStreamSumASTVisitor> visitor) {
		super(visitor);
		this.name = "";
		this.description = "";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
