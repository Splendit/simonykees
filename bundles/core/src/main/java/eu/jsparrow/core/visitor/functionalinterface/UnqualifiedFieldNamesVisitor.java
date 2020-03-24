package eu.jsparrow.core.visitor.functionalinterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds unqualified references to class fields which may be problematic in
 * connection with the transformation of anonymous classes to lambda
 * expressions.
 * 
 * @since 3.16
 */
class UnqualifiedFieldNamesVisitor extends ASTVisitor {

	private final List<ITypeBinding> ancestors;

	private final Map<SimpleName, QualifiedName> simpleNameReplacementsMap = new HashMap<>();

	UnqualifiedFieldNamesVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {

		ancestors = ClassRelationUtil.findAncestors(anonymousClassDeclaration.resolveBinding());
		// Sometimes during debugging and collecting ancestors
		// com.sun.jdi.ObjectCollectedException occurred while retrieving
		// value.
		ancestors.size();
	}

	private void addToReplacementsMap(ITypeBinding declaringType, SimpleName simpleNameToReplace) {
		AST astNode = simpleNameToReplace.getParent()
			.getAST();
		SimpleName qualifier = astNode.newSimpleName(declaringType.getName());
		SimpleName simpleNameClone = astNode.newSimpleName(simpleNameToReplace.getIdentifier());
		QualifiedName qualifiedName = astNode.newQualifiedName(qualifier, simpleNameClone);
		simpleNameReplacementsMap.put(simpleNameToReplace, qualifiedName);
	}

	@Override
	public boolean visit(SimpleName node) {
		ASTNode simpleNameParent = node.getParent();
		if (simpleNameParent instanceof QualifiedName) {
			return true;
		}

		IBinding binding = node.resolveBinding();
		if (!(binding instanceof IVariableBinding)) {
			return true;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (!variableBinding.isField()) {
			return true;
		}

		ITypeBinding declaringClass = variableBinding.getDeclaringClass();
		String declaringClassName = declaringClass.getQualifiedName();

		int i = 0;
		boolean declaringClassIsSupertype = false;
		while (i < ancestors.size() && !declaringClassIsSupertype) {
			ITypeBinding superType = ancestors.get(i);
			if (superType.getQualifiedName()
				.equals(declaringClassName)) {
				declaringClassIsSupertype = true;
			}
			i++;
		}

		if (declaringClassIsSupertype) {
			addToReplacementsMap(declaringClass, node);
		}

		return true;
	}

	public Stream<Entry<SimpleName, QualifiedName>> getSimpleNameReplacements() {
		return simpleNameReplacementsMap.entrySet()
			.stream();
	}

}