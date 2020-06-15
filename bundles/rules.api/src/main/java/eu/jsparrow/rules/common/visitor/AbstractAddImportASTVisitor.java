package eu.jsparrow.rules.common.visitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
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

	protected Set<String> addImports;
	private Set<String> staticImports;

	protected AbstractAddImportASTVisitor() {
		super();
		this.addImports = new HashSet<>();
		this.staticImports = new HashSet<>();
	}

	@Override
	public void endVisit(CompilationUnit node) {

		addImports.stream()
			.filter(qualifiedName -> !StringUtils.startsWith(qualifiedName, JAVA_LANG_PACKAGE))
			.map(qualifiedName -> NodeBuilder.newImportDeclaration(node.getAST(), qualifiedName, false))
			.filter(newImport -> isNotExistingImport(node, newImport))
			.forEach(newImport -> astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY)
				.insertLast(newImport, null));
		staticImports.stream()
			.map(qualifiedName -> NodeBuilder.newImportDeclaration(node.getAST(), qualifiedName, true))
			.filter(newImport -> isNotExistingImport(node, newImport))
			.forEach(newImport -> astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY)
				.insertLast(newImport, null));
		super.endVisit(node);
	}

	@SuppressWarnings("unchecked")
	private boolean isNotExistingImport(CompilationUnit node, ImportDeclaration newImport) {
		return node.imports()
			.stream()
			.noneMatch(importDeclaration -> (new ASTMatcher()).match((ImportDeclaration) importDeclaration, newImport));
	}

	/**
	 * from a given list of fully qualified class names, this method filters out
	 * all imports, which are in the same package or file as the given
	 * {@link CompilationUnit}.
	 * 
	 * @param cu
	 *            current compilation unit
	 * @param newImports
	 *            list of fully qualified names, which are about to be imported
	 * @return list of fully qualified names, where the names contained in the
	 *         current package are filtered out
	 */
	protected List<String> filterNewImportsByExcludingCurrentPackage(CompilationUnit cu, Set<String> newImports) {
		PackageDeclaration cuPackage = cu.getPackage();
		String packageQualifiedName;
		if (cuPackage != null) {
			Name packageName = cuPackage.getName();
			packageQualifiedName = packageName.getFullyQualifiedName();
		} else {
			packageQualifiedName = ""; //$NON-NLS-1$
		}
		List<AbstractTypeDeclaration> cuDeclaredTypes = ASTNodeUtil.convertToTypedList(cu.types(),
				AbstractTypeDeclaration.class);

		return newImports.stream()
			.filter(newImport -> !isInSamePackage(newImport, packageQualifiedName, cuDeclaredTypes))
			.collect(Collectors.toList());
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
	protected boolean isInSamePackage(String newImport, String cuPackageQualifiedName,
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
					.map(type -> type.getName()
						.getIdentifier())
					.anyMatch(name -> name.equals(suffixComponents.get(0)));
			} else {
				isInSamePackage = true;
			}
		}

		return isInSamePackage;
	}

	/**
	 * Records a static import to be inserted if it does not exist. Does not
	 * support on demand imports.
	 * 
	 * @param qualifiedName
	 *            the qualified name of the static import.
	 */
	protected void addStaticImport(String qualifiedName) {
		this.staticImports.add(qualifiedName);
	}

	/**
	 * 
	 * @return true if a type with the given simple name is declared in the
	 *         given {@link CompilationUnit}.
	 */
	protected boolean containsTypeDeclarationWithName(CompilationUnit compilationUnit, String simpleTypeName) {
		DeclaredTypesASTVisitor visitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(visitor);
		return visitor.getAllTypes()
			.stream()
			.map(ITypeBinding::getName)
			.anyMatch(name -> name.equals(simpleTypeName));
	}

	/**
	 * 
	 * @return true if a given type is already imported into the given
	 *         {@link CompilationUnit}.
	 */
	protected boolean containsImport(List<ImportDeclaration> importDeclarations, String qualifiedTypeName) {
		return importDeclarations
			.stream()
			.map(ImportDeclaration::getName)
			.map(Name::getFullyQualifiedName)
			.anyMatch(qualifiedName -> qualifiedName.equals(qualifiedTypeName));
	}

	/**
	 * 
	 * @return true if the simple name of a given type will cause conflicts when
	 *         imported into the given {@link CompilationUnit}.
	 */
	protected boolean isImportClashing(List<ImportDeclaration> importDeclarations, String simpleTypeName) {
		boolean clashing = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.anyMatch(simpleTypeName::equals);

		if (!clashing) {
			clashing = importDeclarations.stream()
				.map(ImportDeclaration::getName)
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
	protected String getSimpleName(String name) {
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
	protected boolean isSafeToAddImport(CompilationUnit compilationUnit, String qualifiedTypeName) {

		String simpleTypeName = getSimpleName(qualifiedTypeName);

		if (containsTypeDeclarationWithName(compilationUnit, simpleTypeName)) {
			return false;
		}
		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		if (containsImport(importDeclarations, qualifiedTypeName)) {
			return true;
		}
		if (isImportClashing(importDeclarations, simpleTypeName)) {
			return false;
		}
		return importDeclarations.stream()
			.noneMatch(
					importDeclaration -> ClassRelationUtil.importsTypeOnDemand(importDeclaration, qualifiedTypeName));
	}
}
