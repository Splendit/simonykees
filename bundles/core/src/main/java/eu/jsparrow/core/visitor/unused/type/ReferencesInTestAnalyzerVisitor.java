package eu.jsparrow.core.visitor.unused.type;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.unused.method.MethodReferencesVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes the references of an unused type in a test class. Determines which
 * parts of the test class should be suggested for removal to gether with the
 * relevant unused type.
 * 
 * @since 4.10.0
 *
 */
public class ReferencesInTestAnalyzerVisitor extends ASTVisitor {

	private String unusedType;
	private String mainClassName;
	private Set<ImportDeclaration> importDeclarations = new HashSet<>();

	/**
	 * References in the field declarations of a nested type.
	 */
	private Set<AbstractTypeDeclaration> nestedTypeReferences = new HashSet<>();

	/**
	 * References in the top-level types.
	 */
	private Set<AbstractTypeDeclaration> topLevelTypeReferences = new HashSet<>();

	/**
	 * References in test cases.
	 */
	private Set<MethodDeclaration> testCases = new HashSet<>();

	public ReferencesInTestAnalyzerVisitor(String unusedType) {
		this.unusedType = unusedType;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		IJavaElement javaElement = compilationUnit.getJavaElement();
		String javaElementName = javaElement.getElementName();
		int lastIndexOfFileExtension = javaElementName.lastIndexOf(".java"); //$NON-NLS-1$
		this.mainClassName = javaElementName.substring(0, lastIndexOfFileExtension);
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration importDeclaration) {
		Name name = importDeclaration.getName();
		String qualifiedName = name.getFullyQualifiedName();
		boolean typeMatch = qualifiedName.equals(unusedType) || qualifiedName.startsWith(unusedType + ".");//$NON-NLS-1$
		if (importDeclaration.isOnDemand()) {
			if (importDeclaration.isStatic() && typeMatch) {
				importDeclarations.add(importDeclaration);
			}
		} else if (importDeclaration.isStatic()) {
			if (qualifiedName.equals(unusedType) || qualifiedName.startsWith(unusedType + ".")) { //$NON-NLS-1$
				importDeclarations.add(importDeclaration);
			}
		} else {
			if (qualifiedName.equals(unusedType)) {
				importDeclarations.add(importDeclaration);
			}
		}
		return false;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		ITypeBinding typeBinding = simpleName.resolveTypeBinding();
		boolean typeMatch = ClassRelationUtil.isContentOfType(typeBinding, unusedType);
		if (!typeMatch) {
			return false;
		}
		Optional<MethodDeclaration> enclosingTestMethod = findOutermostEnclosingMethod(simpleName)
			.filter(MethodReferencesVisitor::isTestAnnotatedMethod);

		if (enclosingTestMethod.isPresent()) {
			MethodDeclaration testCase = enclosingTestMethod.get();
			testCases.add(testCase);
		} else {
			AbstractTypeDeclaration enclosingType = ASTNodeUtil.getSpecificAncestor(simpleName, TypeDeclaration.class);
			if (enclosingType.isPackageMemberTypeDeclaration()) {
				this.topLevelTypeReferences.add(enclosingType);
			} else {
				this.nestedTypeReferences.add(enclosingType);
			}
		}
		return true;
	}

	private Optional<MethodDeclaration> findOutermostEnclosingMethod(SimpleName simpleName) {
		ASTNode node = simpleName;
		MethodDeclaration outermost = null;
		while (true) {
			MethodDeclaration enclosingMethod = ASTNodeUtil.getSpecificAncestor(node, MethodDeclaration.class);
			if (enclosingMethod == null) {
				return Optional.ofNullable(outermost);
			}
			outermost = enclosingMethod;
			node = enclosingMethod;
		}
	}

	public Set<ImportDeclaration> getUnusedTypeImports() {
		return this.importDeclarations;
	}

	public Set<MethodDeclaration> getTestMethodsHavingUnusedTypeReferences() {
		Set<MethodDeclaration> definedInDesignatedTypes = new HashSet<>();
		this.topLevelTypeReferences.forEach(type -> {
			List<MethodDeclaration> methods = ASTNodeUtil.convertToTypedList(type.bodyDeclarations(),
					MethodDeclaration.class);
			definedInDesignatedTypes.addAll(methods);
		});
		this.nestedTypeReferences.forEach(type -> {
			List<MethodDeclaration> methods = ASTNodeUtil.convertToTypedList(type.bodyDeclarations(),
					MethodDeclaration.class);
			definedInDesignatedTypes.addAll(methods);
		});
		return this.testCases
			.stream()
			.filter(method -> !definedInDesignatedTypes.contains(method))
			.collect(Collectors.toSet());
	}

	public boolean isMainTopLevelTypeDesignated() {
		for (AbstractTypeDeclaration type : this.topLevelTypeReferences) {
			SimpleName name = type.getName();
			if (mainClassName.equals(name.getIdentifier())) {
				return true;
			}
		}
		return false;
	}

	public Set<AbstractTypeDeclaration> getTypesWithReferencesToUnusedType() {
		Set<AbstractTypeDeclaration> types = new HashSet<>();
		topLevelTypeReferences.forEach(types::add);
		for (AbstractTypeDeclaration nestedType : nestedTypeReferences) {
			if (!isIncludedInTopLevelClasses(nestedType)) {
				types.add(nestedType);
			}
		}

		return types;
	}

	private boolean isIncludedInTopLevelClasses(AbstractTypeDeclaration nestedType) {
		AbstractTypeDeclaration enclosingType = ASTNodeUtil.getSpecificAncestor(nestedType,
				AbstractTypeDeclaration.class);
		AbstractTypeDeclaration topLevelClass = enclosingType;
		while (enclosingType != null) {
			topLevelClass = enclosingType;
			enclosingType = ASTNodeUtil.getSpecificAncestor(enclosingType, AbstractTypeDeclaration.class);
		}
		return topLevelTypeReferences.contains(topLevelClass);
	}
}
