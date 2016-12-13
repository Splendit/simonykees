package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;
import at.splendit.simonykees.core.i18n.ExceptionMessages;

/**
 * Extended {@link AbstractASTRewriteASTVisitor} where a list of java classes
 * can be injected by fully qualified name to enable a comparison.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public abstract class AbstractCompilationUnitASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String JAVA_LANG_PACKAGE = "java.lang"; //$NON-NLS-1$

	protected Map<Integer, List<IType>> iTypeMap;
	protected Map<Integer, List<String>> fullyQuallifiedNameMap;
	protected Set<String> addImports;

	protected AbstractCompilationUnitASTVisitor() {
		super();
		this.iTypeMap = new HashMap<>();
		this.fullyQuallifiedNameMap = new HashMap<>();
		addImports = new HashSet<>();
	}

	/**
	 * Find the corresponding types of the {@link #relevantClasses()} in the
	 * java project of the {@link CompilationUnit} that accepts the ASTVisitor
	 */
	@Override
	public boolean visit(CompilationUnit node) {
		if (node.getJavaElement() == null && node.getJavaElement().getJavaProject() == null) {
			throw new ITypeNotFoundRuntimeException(
					ExceptionMessages.AbstractCompilationUnitAstVisitor_compilation_unit_no_context);
		}
		IJavaProject iJavaProject = node.getJavaElement().getJavaProject();
		try {
			for (Entry<Integer, List<String>> fullyQualifiedNameEntryp : fullyQuallifiedNameMap.entrySet()) {
				for (String fullyQuallifiedClassName : fullyQualifiedNameEntryp.getValue()) {
					IType classtype = iJavaProject.findType(fullyQuallifiedClassName);
					if (classtype != null) {
						List<IType> categoryTypeList = iTypeMap.get(fullyQualifiedNameEntryp.getKey());
						if (categoryTypeList == null) {
							categoryTypeList = new ArrayList<>();
							categoryTypeList.add(classtype);
							iTypeMap.put(fullyQualifiedNameEntryp.getKey(), categoryTypeList);
						} else {
							categoryTypeList.add(classtype);
						}
					} else {
						throw new ITypeNotFoundRuntimeException();
					}
				}
			}
		} catch (JavaModelException e) {
			throw new ITypeNotFoundRuntimeException(e);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void endVisit(CompilationUnit node) {

		/**
		 * Manages the addition of new Imports
		 */
		if (!addImports.isEmpty()) {
			for (String iterator : addImports) {
				/**
				 * java.lang doesn't need to be imported
				 */
				if (!StringUtils.startsWith(iterator, JAVA_LANG_PACKAGE)) {
					ImportDeclaration newImport = node.getAST().newImportDeclaration();
					newImport.setName(node.getAST().newName(iterator));
					if (node.imports().stream().noneMatch(importDeclaration -> (new ASTMatcher())
							.match((ImportDeclaration) importDeclaration, newImport))) {
						astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY).insertLast(newImport, null);
					}
				}
			}
		}
	}

	protected List<String> generateFullyQuallifiedNameList(String... fullyQuallifiedName) {
		return Arrays.asList(fullyQuallifiedName);
	}
}
