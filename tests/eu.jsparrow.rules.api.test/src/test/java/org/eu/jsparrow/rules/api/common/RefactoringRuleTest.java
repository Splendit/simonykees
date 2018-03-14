package org.eu.jsparrow.rules.api.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.jdtunit.util.CompilationUnitBuilder;
import eu.jsparrow.jdtunit.util.JavaProjectBuilder;
import eu.jsparrow.jdtunit.util.PackageFragmentBuilder;
import eu.jsparrow.rules.api.test.dummies.DummyRefactoringRule;
import eu.jsparrow.rules.common.RefactoringRule;


public class RefactoringRuleTest {

	private RefactoringRule<?> rule;

	private IJavaProject project;

	private IPackageFragment fragment;

	@Before
	public void setUp() throws Exception {
		rule = new DummyRefactoringRule();
		project = new JavaProjectBuilder().build();
		fragment = new PackageFragmentBuilder(project).build();
	}

	@After
	public void tearDown() throws Exception {
		project.getProject()
			.delete(true, null);
	}

	@Test
	public void enabledForProject_projectOnJava7_isEnabled() {
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.7");

		rule.calculateEnabledForProject(project);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void enabledForProject_projectOnJava5_isDisabled() {
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.5");

		rule.calculateEnabledForProject(project);

		assertFalse(rule.isEnabled());
	}

	@Test
	public void appyRuleImpl_whenVisitorDoesNothing_shouldReturnNull() throws Exception {
		ICompilationUnit workingCopy = new CompilationUnitBuilder(fragment).setContent("")
			.build();

		DocumentChange result = rule.applyRule(workingCopy);

		assertNull(result);
	}

}
