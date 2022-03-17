package eu.jsparrow.core.renaming;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.AbstractRulesTest;
import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

public class RenamingTestHelper {

	private RenamingTestHelper() {
		/*
		 * Hiding the default constructor.
		 */
	}

	/**
	 * Loads the compilation units in the provided package and finds the fields
	 * to be renamed and their references.
	 * 
	 * @param prerulePackage
	 *            the package name containing the original files
	 * @param postRulePackagePath
	 *            the path to the package containing the expected result
	 * @param publicModifier
	 *            flag for renaming public fields or not.
	 * @param packagePrivate
	 *            flag for renaming package private fields or not.
	 * @param protectedModifier
	 *            flag for renaming protected fields or not.
	 * @param privateModifier
	 *            flag for renaming private fields or not.
	 * @param addTodos
	 *            flag for adding todos in the fields that cannot be renamed.
	 * @return the {@link FieldDeclarationASTVisitor} containing information
	 *         about all fields to be renamed.
	 * @throws Exception
	 *             if the compilation units cannot be loaded from the package
	 */
	public static FieldDeclarationASTVisitor findFieldsToBeRenamed(String prerulePackage, String postRulePackagePath,
			boolean publicModifier, boolean packagePrivate, boolean protectedModifier, boolean privateModifier,
			boolean addTodos) throws Exception {
		IPackageFragmentRoot root = AbstractRulesTest.createRootPackageFragment();
		IPackageFragment packageFragment = root.createPackageFragment(prerulePackage, true, null);
		List<CompilationUnit> compilationUnits = loadCompilationUnits(packageFragment, postRulePackagePath);

		FieldDeclarationASTVisitor referencesVisitor = new FieldDeclarationASTVisitor(
				new IJavaElement[] { packageFragment });
		referencesVisitor.setRenamePublicField(publicModifier);
		referencesVisitor.setRenamePackageProtectedField(packagePrivate);
		referencesVisitor.setRenameProtectedField(protectedModifier);
		referencesVisitor.setRenamePrivateField(privateModifier);
		referencesVisitor.setAddTodo(addTodos);
		for (CompilationUnit compilationUnit : compilationUnits) {
			compilationUnit.accept(referencesVisitor);
		}
		return referencesVisitor;
	}

	private static List<CompilationUnit> loadCompilationUnits(IPackageFragment packageFragment, String packagePath)
			throws Exception {
		List<CompilationUnit> compilationUnits = new ArrayList<>();
		for (Path renamingPath : AbstractRulesTest.loadUtilityClasses(packagePath)) {
			String renamingClassName = renamingPath.getFileName()
				.toString();
			String renamingSource = new String(Files.readAllBytes(renamingPath), StandardCharsets.UTF_8);
			ICompilationUnit iCompilationUnit = packageFragment.createCompilationUnit(renamingClassName, renamingSource,
					true, null);
			compilationUnits.add(RefactoringUtil.parse(iCompilationUnit));
		}
		return compilationUnits;
	}

	/**
	 * Applies the {@link FieldsRenamingRule} to the target compilation
	 * units of the given {@link FieldDeclarationASTVisitor}.
	 * 
	 * 
	 * @param referencesVisitor
	 *            containing information about the fields to be renamed and
	 *            their references
	 * @return the list of the modified compilation units after applying the
	 *         rule.
	 * @throws RefactoringException
	 * @throws RuleException
	 * @throws ReconcileException
	 *             if the rule cannot be applied.
	 */
	public static List<ICompilationUnit> applyRenamingRule(FieldDeclarationASTVisitor referencesVisitor)
			throws RefactoringException, RuleException, ReconcileException {
		List<RefactoringRule> rules = new ArrayList<>();
		FieldsRenamingRule rule = new FieldsRenamingRule(referencesVisitor.getFieldMetaData(),
				referencesVisitor.getUnmodifiableFieldMetaData());
		rules.add(rule);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(rules);

		IProgressMonitor monitor = new NullProgressMonitor();

		List<ICompilationUnit> compilationUnits = referencesVisitor.getTargetIJavaElements()
			.stream()
			.collect(Collectors.toList());
		refactoringPipeline.prepareRefactoring(compilationUnits, monitor);
		refactoringPipeline.doRefactoring(monitor);
		refactoringPipeline.commitRefactoring();

		return compilationUnits;
	}

	/**
	 * Loads the contents of the files in the given directory to a map having
	 * file names as keys and their contents as values.
	 * 
	 * @param postruleDirectory
	 *            path of the directory whose contents are to be loaded.
	 * @return the constructed map or an empty map if the path is not a
	 *         directory.
	 * @throws IOException
	 *             if the files cannot be read.
	 */
	public static Map<String, String> loadExpected(String postruleDirectory) throws IOException {
		File directory = new File(postruleDirectory);
		if (!directory.isDirectory()) {
			return Collections.emptyMap();
		}

		Map<String, String> expected = new HashMap<>();
		File[] files = directory.listFiles();
		for (File file : files) {
			String name = file.getName();
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			expected.put(name, content);
		}
		return expected;
	}

	/**
	 * Creates a list of compilation units from the given {@link Map}. Uses the
	 * keys as names and values as contents.
	 * 
	 * @param packageFragment
	 *            the package name to create compilation unit to.
	 * @param compilationUnitNameContents
	 *            contents of the compilation units to be created
	 * @return list of the constructed compilation units.
	 * @throws JavaModelException
	 *             if the compilation unit cannot be created
	 */
	public static List<CompilationUnit> loadCompilationUnitsFromString(IPackageFragment packageFragment,
			Map<String, String> compilationUnitNameContents) throws JavaModelException {

		List<ICompilationUnit> iCompilationUnits = new ArrayList<>();
		for (Map.Entry<String, String> entry : compilationUnitNameContents.entrySet()) {
			iCompilationUnits.add(packageFragment.createCompilationUnit(entry.getKey(), entry.getValue(), true, null));
		}
		return iCompilationUnits.stream()
			.map(RefactoringUtil::parse)
			.collect(Collectors.toList());
	}

	/**
	 * Collects the {@link VariableDeclarationFragment}s of the fields declared
	 * in the top-level type declarations of the provided list of compilation
	 * units.
	 * 
	 * @param compilationUnits
	 *            a list of {@link CompilationUnit}.
	 * @return the list of the collected {@link VariableDeclarationFragment}s.
	 */
	public static List<VariableDeclarationFragment> findFieldDeclarations(List<CompilationUnit> compilationUnits) {
		return compilationUnits.stream()
			.flatMap(cu -> convertToTypedList(cu.types(), TypeDeclaration.class).stream())
			.flatMap(type -> convertToTypedList(type.bodyDeclarations(), FieldDeclaration.class).stream())
			.flatMap(field -> ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class)
				.stream())
			.collect(Collectors.toList());
	}

	/**
	 * Asserts that the sorted concatenation of the given lists are the same.
	 * 
	 * @param expected
	 *            expected values
	 * @param actual
	 *            actual values
	 */
	public static void assertMatch(List<String> expected, List<String> actual) {
		String sortedExpected = expected.stream()
			.sorted()
			.collect(Collectors.joining("\n"));
		String sortedActual = actual.stream()
			.sorted()
			.collect(Collectors.joining("\n"));
		assertEquals(sortedExpected, sortedActual);
	}

	/**
	 * Puts the sources of the given compilation units into a list of strings.
	 * Replaces the package prerule package declaration with the postrule one.
	 * 
	 * @param compilationUnits
	 *            list of {@link CompilationUnit}s.
	 * @param prerulePackageName
	 *            the prerule package declaration to be replaced
	 * @param postRulePackageName
	 *            the new package declaration
	 * @return list of sources of the compilation units with the replaced
	 *         package declaration.
	 * @throws JavaModelException
	 */
	public static List<String> calculateActual(List<ICompilationUnit> compilationUnits, String prerulePackageName,
			String postRulePackageName) throws JavaModelException {
		List<String> actual = new ArrayList<>();
		for (ICompilationUnit icu : compilationUnits) {
			String content = icu.getSource();
			actual.add(StringUtils.replace(content, prerulePackageName, postRulePackageName));
		}
		return actual;
	}
}
