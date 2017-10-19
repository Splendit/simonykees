package eu.jsparrow.core.rule;

import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.exception.RefactoringException;

public class DummyRule implements RefactoringRule {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public JavaVersion getRequiredJavaVersion() {
		return null;
	}

	@Override
	public List<Tag> getTags() {
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

}
