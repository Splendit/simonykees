package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamForEachASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachRule extends AbstractRefactoringRule<EnhancedForLoopToStreamForEachASTVisitor> {

	public EnhancedForLoopToStreamForEachRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamForEachASTVisitor.class;
		this.name = Messages.EnhancedForLoopToStreamForEachRule_name;
		this.description = Messages.EnhancedForLoopToStreamForEachRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
