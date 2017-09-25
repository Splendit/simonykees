package eu.jsparrow.core.visitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import eu.jsparrow.core.util.ASTNodeUtil;

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

	protected AbstractAddImportASTVisitor() {
		super();
		this.addImports = new HashSet<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void endVisit(CompilationUnit node) {

		addImports.stream().filter((iterator) -> !StringUtils.startsWith(iterator, JAVA_LANG_PACKAGE)).forEach((iterator) -> {
			ImportDeclaration newImport = node.getAST().newImportDeclaration();
			newImport.setName(node.getAST().newName(iterator));
			if (node.imports().stream().noneMatch(importDeclaration -> (new ASTMatcher())
					.match((ImportDeclaration) importDeclaration, newImport))) {
				astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY).insertLast(newImport, null);
			}
		});
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
				 * inner class declared in the same compilation unit.
				 * Otherwise, the import points either to a type declared in an inner package
				 * or to an inner class which is not declared in the same compilation unit. 
				 */
				isInSamePackage = cuDeclaredTypes.stream().map(type -> type.getName().getIdentifier())
						.anyMatch(name -> name.equals(suffixComponents.get(0)));
			} else {
				isInSamePackage = true;
			}
		}

		return isInSamePackage;
	}
}
