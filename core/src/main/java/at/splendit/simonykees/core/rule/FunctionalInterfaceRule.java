package at.splendit.simonykees.core.rule;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.FunctionalInterfaceASTVisitor;

/**
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class FunctionalInterfaceRule extends RefactoringRule<FunctionalInterfaceASTVisitor> {

	public FunctionalInterfaceRule(Class<FunctionalInterfaceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.FunctionalInterfaceRule_name;
		this.description = Messages.FunctionalInterfaceRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_8;
	}

}
