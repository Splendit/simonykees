package eu.jsparrow.dummies;

import java.time.Duration;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.exception.RefactoringException;

/**
 * This class serves as a dummy for a refactoring rule. Strictly for use in unit
 * tests.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class DummyRule implements RefactoringRule {

	@Override
	public String getRequiredJavaVersion() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isFree() {
		return false;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		return;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		return false;
	}

	@Override
	public DocumentChange applyRule(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {
		return null;
	}

	@Override
	public String requiredLibraries() {
		return null;
	}

	@Override
	public boolean isSatisfiedJavaVersion() {
		return false;
	}

	@Override
	public boolean isSatisfiedLibraries() {
		return false;
	}

	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription("dummy", "dummyDescription", Duration.ofMinutes(5)); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
