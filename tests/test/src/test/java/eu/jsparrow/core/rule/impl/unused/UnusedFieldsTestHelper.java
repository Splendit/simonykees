package eu.jsparrow.core.rule.impl.unused;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import eu.jsparrow.core.AbstractRulesTest;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.core.visitor.unused.UnusedFieldsEngine;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodsEngine;
import eu.jsparrow.rules.common.RefactoringRule;

public class UnusedFieldsTestHelper {

	private UnusedFieldsTestHelper() {
		/*
		 * Hide default constructor.
		 */
	}

	public static String applyRemoveUnusedCodeRefactoring(RefactoringRule rule, String packageString,
			Path preFile, IPackageFragmentRoot root) throws Exception {

		IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);
		String fileName = preFile.getFileName()
			.toString();
		String content = new String(Files.readAllBytes(preFile), StandardCharsets.UTF_8);
		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		compilationUnits.add(compilationUnit);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(Arrays.asList(rule));
		IProgressMonitor monitor = new NullProgressMonitor();

		refactoringPipeline.prepareRefactoring(compilationUnits, monitor);
		refactoringPipeline.doRefactoring(monitor);
		refactoringPipeline.commitRefactoring();

		return compilationUnit.getSource();
	}

	public static List<UnusedFieldWrapper> findFieldsToBeRemoved(String prerulePackage, String postRulePackagePath)
			throws Exception {
		IPackageFragmentRoot root = AbstractRulesTest.createRootPackageFragment();
		IPackageFragment packageFragment = root.createPackageFragment(prerulePackage, true, null);
		List<ICompilationUnit> compilationUnits = loadCompilationUnits(packageFragment, postRulePackagePath);

		UnusedFieldsEngine engine = new UnusedFieldsEngine("Project");
		NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
		SubMonitor subMonitor = SubMonitor.convert(nullProgressMonitor, 100);

		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("private-fields", true);
		options.put("protected-fields", true);
		options.put("package-private-fields", true);
		options.put("public-fields", true);
		return engine.findUnusedFields(compilationUnits, options, subMonitor);
	}
	
	public static List<UnusedMethodWrapper> findMethodsToBeRemoved(String prerulePackage, String postRulePackagePath)
			throws Exception {
		IPackageFragmentRoot root = AbstractRulesTest.createRootPackageFragment();
		IPackageFragment packageFragment = root.createPackageFragment(prerulePackage, true, null);
		List<ICompilationUnit> compilationUnits = loadCompilationUnits(packageFragment, postRulePackagePath);

		UnusedMethodsEngine engine = new UnusedMethodsEngine("Project");
		NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
		SubMonitor subMonitor = SubMonitor.convert(nullProgressMonitor, 100);

		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("private-methods", true);
		options.put("protected-methods", true);
		options.put("package-private-methods", true);
		options.put("public-methods", true);
		options.put("remove-test-code", true);
		return engine.findUnusedMethods(compilationUnits, options, subMonitor);
	}

	private static List<ICompilationUnit> loadCompilationUnits(IPackageFragment packageFragment, String packagePath)
			throws Exception {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		for (Path renamingPath : AbstractRulesTest.loadUtilityClasses(packagePath)) {
			String renamingClassName = renamingPath.getFileName()
				.toString();
			String renamingSource = new String(Files.readAllBytes(renamingPath), StandardCharsets.UTF_8);
			ICompilationUnit iCompilationUnit = packageFragment.createCompilationUnit(renamingClassName, renamingSource,
					true, null);
			compilationUnits.add(iCompilationUnit);
		}
		return compilationUnits;
	}

}
