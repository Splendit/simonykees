package eu.jsparrow.rules.common;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.exception.RefactoringException;

public interface RefactoringRule {

	public String getRequiredJavaVersion();

	public boolean isEnabled();

	public boolean isFree();

	public String getId();

	public void calculateEnabledForProject(IJavaProject project);

	public boolean ruleSpecificImplementation(IJavaProject project);

	public DocumentChange applyRule(ICompilationUnit workingCopy, CompilationUnit astRoot, List<ASTNode> nodesToIgnore)
			throws ReflectiveOperationException, JavaModelException, RefactoringException;

	public String requiredLibraries();

	public boolean isSatisfiedJavaVersion();

	public boolean isSatisfiedLibraries();

	public RuleDescription getRuleDescription();
}
