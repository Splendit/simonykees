package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamFindFirstASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamFindFirstASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
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
