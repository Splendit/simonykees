package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamAnyMatchASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchRule extends RefactoringRule<EnhancedForLoopToStreamAnyMatchASTVisitor> {

	public EnhancedForLoopToStreamAnyMatchRule() {
		super();
		this.visitor = EnhancedForLoopToStreamAnyMatchASTVisitor.class;
		this.name = Messages.EnhancedForLoopToStreamAnyMatchRule_name;
		this.description = Messages.EnhancedForLoopToStreamAnyMatchRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
