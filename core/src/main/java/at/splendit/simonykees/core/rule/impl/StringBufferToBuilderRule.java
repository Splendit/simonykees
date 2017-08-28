package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.StringBufferToBuilderASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderRule extends RefactoringRule<StringBufferToBuilderASTVisitor> {

	public StringBufferToBuilderRule(Class<StringBufferToBuilderASTVisitor> visitor) {
		super(visitor);
		this.name = "StringBufferToBuilderRule";
		this.description = "StringBufferToBuilderRule";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
