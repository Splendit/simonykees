package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;
import at.splendit.simonykees.core.i18n.ExceptionMessages;

/**
 * Extended {@link AbstractASTRewriteASTVisitor} where a list of java classes
 * can be injected by fully qualified name to enable a comparison.
 * 
 * @author Martin Huter
 *
 */
public abstract class AbstractCompilationUnitAstVisitor extends AbstractASTRewriteASTVisitor {

	protected Map<Integer, List<IType>> iTypeMap;
	protected Map<Integer, List<String>> fullyQuallifiedNameMap;

	protected AbstractCompilationUnitAstVisitor() {
		super();
		this.iTypeMap = new HashMap<>();
		this.fullyQuallifiedNameMap = new HashMap<>();
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
	
	protected List<String> generateFullyQuallifiedNameList(String... fullyQuallifiedName) {
		return Arrays.asList(fullyQuallifiedName);
	}
}
