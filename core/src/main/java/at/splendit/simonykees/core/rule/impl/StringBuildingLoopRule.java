package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.StringBuildingLoopASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see StringBuildingLoopASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class StringBuildingLoopRule extends RefactoringRule<StringBuildingLoopASTVisitor> {

	private JavaVersion javaVersion;
	
	public StringBuildingLoopRule(Class<StringBuildingLoopASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringBuildingLoopRule_name;
		this.description = Messages.StringBuildingLoopRule_description;
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
	protected StringBuildingLoopASTVisitor visitorFactory() {
		return new StringBuildingLoopASTVisitor(javaVersion);
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
