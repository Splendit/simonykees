package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.DiamondOperatorASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see DiamondOperatorASTVisitor
 * 
 * Minimum java version that supports diamond operator is {@value JavaVersion.JAVA_1_7}
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorRule extends RefactoringRule<DiamondOperatorASTVisitor> {

	private JavaVersion javaVersion;
	
	public DiamondOperatorRule(Class<DiamondOperatorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.DiamondOperatorRule_name;
		this.description = Messages.DiamondOperatorRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}
	
	/**
	 * Stores java compiler compliance level.
	 */
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		String enumRepresentation = convertCompilerComplianceToEnumRepresentation(compilerCompliance);
		javaVersion = JavaVersion.valueOf(enumRepresentation);
		return true;
	}
	
	@Override
	protected DiamondOperatorASTVisitor visitorFactory() {
		return new DiamondOperatorASTVisitor(javaVersion);
	}

}
