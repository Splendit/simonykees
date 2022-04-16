package eu.jsparrow.core.visitor.unused.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedTypesASTVisitor extends AbstractASTRewriteASTVisitor {

	private CompilationUnit compilationUnit;
	private List<UnusedTypeWrapper> unusedTypes;
	private Map<AbstractTypeDeclaration, UnusedTypeWrapper> relevantDeclarations;
	private Map<MethodDeclaration, UnusedTypeWrapper> relevantTestCaseDeclarations;
	private Map<ImportDeclaration, UnusedTypeWrapper> relevantImportDeclarations;

	public RemoveUnusedTypesASTVisitor(List<UnusedTypeWrapper> unusedTypes) {
		this.unusedTypes = unusedTypes;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);
		this.compilationUnit = compilationUnit;
		IPath currentPath = compilationUnit.getJavaElement()
			.getPath();

		Map<AbstractTypeDeclaration, UnusedTypeWrapper> declarations = new HashMap<>();
		Map<MethodDeclaration, UnusedTypeWrapper> testCaseDeclarations = new HashMap<>();
		Map<ImportDeclaration, UnusedTypeWrapper> importDeclarations = new HashMap<>();
		for (UnusedTypeWrapper unusedTypeWrapper : unusedTypes) {
			IPath path = unusedTypeWrapper.getDeclarationPath();
			if (path.equals(currentPath)) {
				if (unusedTypeWrapper.isMainType()) {
					removeAllContent(unusedTypeWrapper);
					return false;
				} else {
					AbstractTypeDeclaration declaration = unusedTypeWrapper.getTypeDeclaration();
					declarations.put(declaration, unusedTypeWrapper);
				}
			} else {
				List<TestReferenceOnType> testReferencesOnType = unusedTypeWrapper.getTestReferencesOnType();
				for (TestReferenceOnType testReferenceOnType : testReferencesOnType) {
					IPath testPath = testReferenceOnType.getICompilationUnit()
						.getPath();
					if (currentPath.equals(testPath)) {
						testReferenceOnType.getTestTypesReferencingType()
							.forEach(type -> declarations.put(type, unusedTypeWrapper));
						testReferenceOnType.getTestMethodsReferencingType()
							.forEach(method -> testCaseDeclarations.put(method, unusedTypeWrapper));
						testReferenceOnType.getUnusedTypeImportDeclarations()
							.forEach(importDeclaration -> importDeclarations.put(importDeclaration,
									unusedTypeWrapper));
					}
				}
			}
		}
		this.relevantImportDeclarations = importDeclarations;
		this.relevantDeclarations = declarations;
		this.relevantTestCaseDeclarations = testCaseDeclarations;
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration importDeclaration) {
		UnusedTypeWrapper designated = isDesignatedForRemoval(importDeclaration, relevantImportDeclarations)
			.orElse(null);
		if (designated != null) {
			TextEditGroup editGroup = designated.getTextEditGroup((ICompilationUnit) getCompilationUnit()
				.getJavaElement());
			astRewrite.remove(importDeclaration, editGroup);
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		UnusedTypeWrapper designated = isDesignatedForRemoval(typeDeclaration, relevantDeclarations)
			.orElse(null);
		if (designated != null) {
			TextEditGroup editGroup = designated.getTextEditGroup((ICompilationUnit) getCompilationUnit()
				.getJavaElement());
			ASTNode nodeToRemove = typeDeclaration;
			if (nodeToRemove.getLocationInParent() == TypeDeclarationStatement.DECLARATION_PROPERTY) {
				astRewrite.remove(typeDeclaration.getParent(), editGroup);
			} else {
				astRewrite.remove(typeDeclaration, editGroup);
			}
			if (designated.isMainType()) {
				astRewrite.remove(compilationUnit.getPackage(), editGroup);
				ASTNodeUtil.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
					.forEach(importDeclaration -> astRewrite.remove(importDeclaration, editGroup));
			}
			onRewrite();
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		UnusedTypeWrapper designated = isDesignatedForRemoval(methodDeclaration, relevantTestCaseDeclarations)
			.orElse(null);
		if (designated != null) {
			TextEditGroup editGroup = designated.getTextEditGroup((ICompilationUnit) getCompilationUnit()
				.getJavaElement());
			astRewrite.remove(methodDeclaration, editGroup);
		}
		return true;
	}

	private void removeAllContent(UnusedTypeWrapper unusedTypeWrapper) {
		TextEditGroup editGroup = unusedTypeWrapper.getTextEditGroup((ICompilationUnit) getCompilationUnit()
			.getJavaElement());
		astRewrite.remove(compilationUnit.getPackage(), editGroup);

		ASTNodeUtil.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.forEach(importDeclaration -> astRewrite.remove(importDeclaration, editGroup));

		ASTNodeUtil.convertToTypedList(compilationUnit.types(), AbstractTypeDeclaration.class)
			.forEach(typeDeclaration -> astRewrite.remove(typeDeclaration, editGroup));

		onRewrite();
	}

	private Optional<UnusedTypeWrapper> isDesignatedForRemoval(AbstractTypeDeclaration typeDeclaration,
			Map<AbstractTypeDeclaration, UnusedTypeWrapper> map) {
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		for (Map.Entry<AbstractTypeDeclaration, UnusedTypeWrapper> entry : map.entrySet()) {
			AbstractTypeDeclaration relevantDeclaration = entry.getKey();
			ITypeBinding unusedTypeBinding = relevantDeclaration.resolveBinding();
			UnusedTypeWrapper unusedTypeWrapper = entry.getValue();
			if (typeBinding.isEqualTo(unusedTypeBinding)) {
				return Optional.of(unusedTypeWrapper);
			}
		}
		return Optional.empty();
	}

	private Optional<UnusedTypeWrapper> isDesignatedForRemoval(MethodDeclaration methodDeclaration,
			Map<MethodDeclaration, UnusedTypeWrapper> map) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		for (Map.Entry<MethodDeclaration, UnusedTypeWrapper> entry : map.entrySet()) {
			MethodDeclaration relevantDeclaration = entry.getKey();
			IMethodBinding unusedMethodBinding = relevantDeclaration.resolveBinding();
			UnusedTypeWrapper unusedMethod = entry.getValue();
			if (unusedMethodBinding.isEqualTo(methodBinding)) {
				return Optional.of(unusedMethod);
			}
		}
		return Optional.empty();
	}

	private Optional<UnusedTypeWrapper> isDesignatedForRemoval(ImportDeclaration importDeclaration,
			Map<ImportDeclaration, UnusedTypeWrapper> map) {
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding == null) {
			return Optional.empty();
		}
		for (Map.Entry<ImportDeclaration, UnusedTypeWrapper> entry : map.entrySet()) {
			ImportDeclaration relevantDeclaration = entry.getKey();
			IBinding unusedTypeImportBinding = relevantDeclaration.resolveBinding();
			UnusedTypeWrapper unusedType = entry.getValue();
			if (importBinding.isEqualTo(unusedTypeImportBinding)) {
				return Optional.of(unusedType);
			}
		}
		return Optional.empty();
	}

}
