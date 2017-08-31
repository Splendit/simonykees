package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.ReImplementingInterfaceASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceRule extends RefactoringRule<ReImplementingInterfaceASTVisitor> {

	public ReImplementingInterfaceRule(Class<ReImplementingInterfaceASTVisitor> visitor) {
		super(visitor);
		this.name = "ReImplementingInterfaceRule";
		this.description = "ReImplementingInterfaceRule";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
