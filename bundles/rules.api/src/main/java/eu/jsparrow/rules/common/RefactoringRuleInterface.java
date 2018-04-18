package eu.jsparrow.rules.common;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.exception.RefactoringException;


public interface RefactoringRuleInterface {

	public JavaVersion getRequiredJavaVersion();

	public boolean isEnabled();

	public String getId();

	public void calculateEnabledForProject(IJavaProject project);

	public boolean ruleSpecificImplementation(IJavaProject project);

	public DocumentChange applyRule(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException, RefactoringException;

	public String requiredLibraries();

	public boolean isSatisfiedJavaVersion();

	public boolean isSatisfiedLibraries();
	
	public RuleDescription getRuleDescription();
}
