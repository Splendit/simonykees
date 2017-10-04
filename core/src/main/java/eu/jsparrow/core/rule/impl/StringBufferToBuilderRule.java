package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.StringBufferToBuilderASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderRule extends RefactoringRule<StringBufferToBuilderASTVisitor> {

	public StringBufferToBuilderRule() {
		super();
		this.visitor = StringBufferToBuilderASTVisitor.class;
		this.name = Messages.StringBufferToBuilderRule_name;
		this.description = Messages.StringBufferToBuilderRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
