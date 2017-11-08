package eu.jsparrow.dummies;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.rule.RuleDescription;

/**
 * This class serves as a dummy for a refactoring rule. Strictly for use in unit
 * tests.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class DummyRule implements RefactoringRuleInterface {

	@Override
	public JavaVersion getRequiredJavaVersion() {
		return null;
	}

	@Override
	public boolean isEnabled() {
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
	public DocumentChange applyRule(ICompilationUnit workingCopy)
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
		return new RuleDescription("dummy", "dummyDescription", Duration.ofMinutes(5));
	}

}
