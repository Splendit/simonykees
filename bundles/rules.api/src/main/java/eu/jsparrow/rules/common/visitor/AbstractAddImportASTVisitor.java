package eu.jsparrow.rules.common.visitor;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.importsInnerTypeOnDemand;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.importsStaticMethodOnDemand;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.importsTypeOnDemand;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.DeclaredMethodNamesASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.DeclaredTypesASTVisitor;

/**
 * Extended {@link AbstractASTRewriteASTVisitor} where a list of java classes
 * can be injected by fully qualified name to enable a comparison.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public abstract class AbstractAddImportASTVisitor extends AbstractASTRewriteASTVisitor {

	protected static final String JAVA_LANG_PACKAGE = "java.lang"; //$NON-NLS-1$
	protected static final String DOT = "."; //$NON-NLS-1$
	protected static final String DOT_REGEX = "\\" + DOT; //$NON-NLS-1$

	private Set<String> addImports = new HashSet<>();
	private Set<String> staticImports = new HashSet<>();
	private Set<String> safeImports = new HashSet<>();
	private Set<String> typesImportedOnDemand = new HashSet<>();
	private Set<String> safeStaticMethodImports = new HashSet<>();
	private Set<String> staticMethodsImportedOnDemand = new HashSet<>();

	@Override
	public void endVisit(CompilationUnit node) {

		PackageDeclaration cuPackage = node.getPackage();
		String packageQualifiedName;
		if (cuPackage != null) {
			Name packageName = cuPackage.getName();
			packageQualifiedName = packageName.getFullyQualifiedName();
		} else {
			packageQualifiedName = ""; //$NON-NLS-1$
		}
		List<AbstractTypeDeclaration> cuDeclaredTypes = ASTNodeUtil.convertToTypedList(node.types(),
				AbstractTypeDeclaration.class);
		List<ImportDeclaration> existingImports = ASTNodeUtil.convertToTypedList(node.imports(),
				ImportDeclaration.class);

		addImports.stream()
			.filter(qualifiedName -> !JAVA_LANG_PACKAGE.equals(findQualifyingPrefix(qualifiedName)))
			.filter(qualifiedName -> !isInSamePackage(qualifiedName, packageQualifiedName, cuDeclaredTypes))
			.filter(qualifiedName -> !containsImport(existingImports, qualifiedName))
			.map(qualifiedName -> NodeBuilder.newImportDeclaration(node.getAST(), qualifiedName, false))
			.forEach(newImport -> astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY)
				.insertLast(newImport, null));
		staticImports.stream()
			.filter(qualifiedName -> !containsImport(existingImports, qualifiedName))
			.map(qualifiedName -> NodeBuilder.newImportDeclaration(node.getAST(), qualifiedName, true))
			.forEach(newImport -> astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY)
				.insertLast(newImport, null));
		safeImports.clear();
		typesImportedOnDemand.clear();
		safeStaticMethodImports.clear();
		staticMethodsImportedOnDemand.clear();
		super.endVisit(node);
	}

	protected void verifyImport(CompilationUnit compilationUnit, String qualifiedTypeName) {
		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		if (isSafeToAddImport(compilationUnit, importDeclarations, qualifiedTypeName)) {
			safeImports.add(qualifiedTypeName);
			if (matchesTypeImportOnDemand(importDeclarations, qualifiedTypeName)) {
				typesImportedOnDemand.add(qualifiedTypeName);
			}
		}
	}

	protected void verifyStaticMethodImport(CompilationUnit compilationUnit, String fullyQualifiedStaticMethodName) {
		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		if (isSafeToAddStaticMethodImport(compilationUnit, importDeclarations, fullyQualifiedStaticMethodName)) {
			safeStaticMethodImports.add(fullyQualifiedStaticMethodName);
			if (matchesStaticMethodImportOnDemand(importDeclarations, fullyQualifiedStaticMethodName)) {
				staticMethodsImportedOnDemand.add(fullyQualifiedStaticMethodName);
			}
		}
	}

	/**
	 * Checks whether the new import points to a class in the same package or in
	 * the same file as the compilation unit.
	 * 
	 * @param newImport
	 *            qualified name of the new import
	 * @param cuPackageQualifiedName
	 *            qualified name of the compilation unit's package
	 * @param cuDeclaredTypes
	 *            types declared in the compilation unit.
	 * @return true if the new import points to a type in the same package as
	 *         the compilation unit or to a type declared inside the compilation
	 *         unit.
	 */
	private boolean isInSamePackage(String newImport, String cuPackageQualifiedName,
			List<AbstractTypeDeclaration> cuDeclaredTypes) {
		boolean isInSamePackage = false;

		if (StringUtils.startsWith(newImport, cuPackageQualifiedName + DOT)) {
			int packageNameEndIndex = cuPackageQualifiedName.length() + 1;
			String suffix = StringUtils.substring(newImport, packageNameEndIndex);
			List<String> suffixComponents = Arrays.asList(suffix.split(DOT_REGEX));
			if (suffixComponents.size() > 1) {
				/*
				 * It can be the case that the new import candidate points to an
				 * inner class declared in the same compilation unit. Otherwise,
				 * the import points either to a type declared in an inner
				 * package or to an inner class which is not declared in the
				 * same compilation unit.
				 */
				isInSamePackage = cuDeclaredTypes.stream()
					.map(AbstractTypeDeclaration::getName)
					.map(SimpleName::getIdentifier)
					.anyMatch(name -> name.equals(suffixComponents.get(0)));
			} else {
				isInSamePackage = true;
			}
		}

		return isInSamePackage;
	}

	/**
	 * 
	 * @return true if a type with the given simple name is declared in the
	 *         given {@link CompilationUnit}.
	 */
	private boolean containsTypeDeclarationWithName(CompilationUnit compilationUnit, String simpleTypeName) {
		DeclaredTypesASTVisitor visitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(visitor);
		return visitor.getAllTypes()
			.stream()
			.map(ITypeBinding::getName)
			.anyMatch(simpleTypeName::equals);
	}

	/**
	 * 
	 * @return true if a given type is already imported into the given
	 *         {@link CompilationUnit}.
	 */
	private boolean containsImport(List<ImportDeclaration> importDeclarations, String qualifiedTypeName) {
		return importDeclarations
			.stream()
			.map(ImportDeclaration::getName)
			.map(Name::getFullyQualifiedName)
			.anyMatch(qualifiedTypeName::equals);
	}

	/**
	 * 
	 * @return true if the simple name of a given type will cause conflicts when
	 *         imported into the given {@link CompilationUnit}.
	 */
	private boolean isImportClashing(List<ImportDeclaration> importDeclarations, String simpleTypeName) {
		List<Name> importedNames = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.collect(Collectors.toList());
		boolean clashing = importedNames
			.stream()
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.anyMatch(simpleTypeName::equals);

		if (!clashing) {
			clashing = importedNames.stream()
				.filter(Name::isSimpleName)
				.map(name -> (SimpleName) name)
				.map(SimpleName::getIdentifier)
				.anyMatch(simpleTypeName::equals);
		}
		return clashing;
	}

	/**
	 * 
	 * @param name
	 *            expected to be the either a simple or a qualified class name.
	 * @return the simple name of the class corresponding the name given by the
	 *         parameter.
	 */
	private String getSimpleName(String name) {
		int lastIndexOfDot = name.lastIndexOf('.');
		if (lastIndexOfDot == -1) {
			return name;
		}
		return name.substring(lastIndexOfDot + 1);
	}

	/**
	 * 
	 * @param compilationUnit
	 *            where the import is intended to be carried out
	 * @param qualifiedTypeName
	 *            class to be imported
	 * @return true if the import can be carried out, otherwise false.
	 */
	private boolean isSafeToAddImport(CompilationUnit compilationUnit, List<ImportDeclaration> importDeclarations,
			String qualifiedTypeName) {

		String simpleTypeName = getSimpleName(qualifiedTypeName);

		if (containsTypeDeclarationWithName(compilationUnit, simpleTypeName)) {
			return false;
		}

		if (containsImport(importDeclarations, qualifiedTypeName)) {
			return true;
		}

		if (matchesTypeImportOnDemand(importDeclarations, qualifiedTypeName)) {
			return true;
		}

		if (isImportClashing(importDeclarations, simpleTypeName)) {
			return false;
		}
		return importDeclarations.stream()
			.noneMatch(importDeclaration -> importsTypeOnDemand(importDeclaration, simpleTypeName) ||
					importsInnerTypeOnDemand(importDeclaration, simpleTypeName));
	}

	/**
	 * 
	 * @return true if a method with the given simple name is declared in the
	 *         given {@link CompilationUnit}.
	 */
	private boolean containsMethodDeclarationWithName(CompilationUnit compilationUnit, String simpleMethod) {
		DeclaredMethodNamesASTVisitor visitor = new DeclaredMethodNamesASTVisitor();
		compilationUnit.accept(visitor);
		return visitor.getDeclaredMethodNames()
			.stream()
			.anyMatch(simpleMethod::equals);
	}

	private boolean matchesTypeImportOnDemand(List<ImportDeclaration> importDeclarations,
			String qualifiedTypeName) {
		String simpleTypeName = getSimpleName(qualifiedTypeName);
		List<ImportDeclaration> importsOnDemand = importDeclarations.stream()
			.filter(ImportDeclaration::isOnDemand)
			.collect(Collectors.toList());

		if (importsOnDemand.isEmpty()) {
			return false;
		}

		for (ImportDeclaration importOnDemand : importsOnDemand) {
			IBinding iBinding = importOnDemand.resolveBinding();
			String implicitTypeImport = iBinding.getName() + "." + simpleTypeName; //$NON-NLS-1$
			if (qualifiedTypeName.equals(implicitTypeImport)) {
				return true;
			}
		}
		return false;
	}

	private boolean matchesStaticMethodImportOnDemand(List<ImportDeclaration> importDeclarations,
			String qualifiedStaticMethodName) {
		String simpleMethodName = getSimpleName(qualifiedStaticMethodName);
		List<ImportDeclaration> importsOnDemand = importDeclarations.stream()
			.filter(importDeclaration -> importsStaticMethodOnDemand(importDeclaration, simpleMethodName))
			.collect(Collectors.toList());

		if (importsOnDemand.isEmpty()) {
			return false;
		}

		for (ImportDeclaration importOnDemand : importsOnDemand) {
			IBinding iBinding = importOnDemand.resolveBinding();
			if (iBinding.getKind() != IBinding.TYPE) {
				return false;
			}
			ITypeBinding typeBinding = (ITypeBinding) iBinding;
			String implicitStaticImport = typeBinding.getQualifiedName() + "." + simpleMethodName; //$NON-NLS-1$
			if (!qualifiedStaticMethodName.equals(implicitStaticImport)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param compilationUnit
	 *            where the import is intended to be carried out
	 * @param qualifiedStaticMethodName
	 *            static method to be imported
	 * @return true if the import can be carried out, otherwise false.
	 */
	private boolean isSafeToAddStaticMethodImport(CompilationUnit compilationUnit,
			List<ImportDeclaration> importDeclarations, String qualifiedStaticMethodName) {

		String simpleMethodName = getSimpleName(qualifiedStaticMethodName);

		if (containsMethodDeclarationWithName(compilationUnit, simpleMethodName)) {
			return false;
		}

		if (containsImport(importDeclarations, qualifiedStaticMethodName)) {
			return true;
		}

		if (matchesStaticMethodImportOnDemand(importDeclarations, qualifiedStaticMethodName)) {
			return true;
		}

		if (isImportClashing(importDeclarations, simpleMethodName)) {
			return false;
		}
		return importDeclarations.stream()
			.noneMatch(importDeclaration -> importsStaticMethodOnDemand(importDeclaration, simpleMethodName));
	}

	/**
	 * @param qualifiedName
	 *            the fully qualified name of a type.
	 * @return a {@link SimpleName} representing the simple type name if
	 *         corresponding import can be added safely added, otherwise a
	 *         {@link Name} representing the fully qualified type name.
	 */
	private Name findTypeName(String qualifiedName) {
		AST ast = astRewrite.getAST();
		if (safeImports.contains(qualifiedName)) {
			return ast.newSimpleName(getSimpleName(qualifiedName));
		}
		return ast.newName(qualifiedName);
	}

	/**
	 * Adds the qualified type name specified by the parameter to the type names
	 * which will be imported.
	 * 
	 * @param qualifiedName
	 */
	protected Name addImport(String qualifiedName) {
		if (safeImports.contains(qualifiedName) && !typesImportedOnDemand.contains(qualifiedName)) {
			addImports.add(qualifiedName);
		}
		return findTypeName(qualifiedName);
	}

	/**
	 * Adds the fully qualified static method name to the static method names
	 * which will be imported.
	 * 
	 * @param fullyQualifiedMethodName
	 * @return The qualifier to be used for the static method or an empty
	 *         {@link Optional} if no qualifier is needed.
	 */
	protected Optional<Name> addImportForStaticMethod(String fullyQualifiedMethodName) {
		if (safeStaticMethodImports.contains(fullyQualifiedMethodName)) {
			if(!staticMethodsImportedOnDemand.contains(fullyQualifiedMethodName)) {
				this.staticImports.add(fullyQualifiedMethodName);
			}
			return Optional.empty();
		} else {
			String qualifiedTypeName = findQualifyingPrefix(fullyQualifiedMethodName);
			addImport(qualifiedTypeName);
			return Optional.of(findTypeName(qualifiedTypeName));
		}
	}

	private String findQualifyingPrefix(String qualifiedName) {
		int lastIndexOfDot = qualifiedName.lastIndexOf('.');
		return qualifiedName.substring(0, lastIndexOfDot);
	}

	protected void addAlreadyVerifiedImports(Collection<String> newImports) {
		addImports.addAll(newImports);
	}
}
