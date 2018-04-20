package eu.jsparrow.core.renaming;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;
import static org.junit.Assert.assertEquals;

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
import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

@SuppressWarnings("nls")
public class RenamingTestHelper {

	public static FieldDeclarationASTVisitor findFields(String prerulePackage, String postRulePackagePath,
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
		/*
		 * Load iCompilationUnits on the prerule.renaming package
		 */
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

	public static List<ICompilationUnit> applyRenamingRule(FieldDeclarationASTVisitor referencesVisitor,
			IPackageFragmentRoot root, String prerulePackageName)
			throws JavaModelException, RefactoringException, RuleException, ReconcileException {
		IPackageFragment packageFragment = root.createPackageFragment(prerulePackageName, true, null);
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = new ArrayList<>();
		PublicFieldsRenamingRule rule = new PublicFieldsRenamingRule(referencesVisitor.getFieldMetaData(),
				referencesVisitor.getUnmodifiableFieldMetaData());
		rules.add(rule);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(rules);

		IProgressMonitor monitor = new NullProgressMonitor();

		rules.stream()
			.forEach(r -> r.calculateEnabledForProject(packageFragment.getJavaProject()));

		List<ICompilationUnit> compilationUnits = referencesVisitor.getTargetIJavaElements()
			.stream()
			.collect(Collectors.toList());
		refactoringPipeline.prepareRefactoring(compilationUnits, monitor);
		refactoringPipeline.doRefactoring(monitor);
		refactoringPipeline.commitRefactoring();

		return referencesVisitor.getTargetIJavaElements()
			.stream()
			.collect(Collectors.toList());
	}

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

	static void assertMatch(Map<String, String> expected, List<ICompilationUnit> compilationUnits,
			String prerulePackageName, String postRulePackageName) throws JavaModelException {
		assertEquals(expected.keySet()
			.size(), compilationUnits.size());
		for (ICompilationUnit icu : compilationUnits) {
			String name = icu.getPath()
				.lastSegment();
			String actual = icu.getSource();
			actual = StringUtils.replace(actual, prerulePackageName, postRulePackageName);
			assertEquals(expected.getOrDefault(name, ""), actual);
		}
	}

	public static List<CompilationUnit> loadCompilationUnitsFromString(IPackageFragment packageFragment,
			Map<String, String> compilationUnitNameContents) throws JavaModelException, IOException {

		List<ICompilationUnit> iCompilationUnits = new ArrayList<>();
		for (Map.Entry<String, String> entry : compilationUnitNameContents.entrySet()) {
			iCompilationUnits.add(packageFragment.createCompilationUnit(entry.getKey(), entry.getValue(), true, null));
		}
		return iCompilationUnits.stream()
			.map(RefactoringUtil::parse)
			.collect(Collectors.toList());
	}

	static List<VariableDeclarationFragment> findFieldDeclarations(List<CompilationUnit> compilationUnits) {
		return compilationUnits.stream()
			.flatMap(cu -> convertToTypedList(cu.types(), TypeDeclaration.class).stream())
			.flatMap(type -> convertToTypedList(type.bodyDeclarations(), FieldDeclaration.class).stream())
			.flatMap(field -> ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class)
				.stream())
			.collect(Collectors.toList());
	}

}
