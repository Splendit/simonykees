package eu.jsparrow.core.visitor.functionalinterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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

	private final List<SimpleName> simpleNames = new ArrayList<>();

	private final ITypeBinding anonymousClassTypeBinding;

	UnqualifiedFieldNamesVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		ASTNode simpleNameParent = simpleName.getParent();
		if (simpleNameParent instanceof QualifiedName) {
			return true;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return true;
		}

		if (binding.getKind() != IBinding.VARIABLE) {
			return true;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (!variableBinding.isField()) {
			return true;
		}

		ITypeBinding declaringClass = variableBinding.getDeclaringClass();
		if (ClassRelationUtil.isInheritingContentOfTypes(
				anonymousClassTypeBinding,
				Collections.singletonList(declaringClass.getQualifiedName()))) {
			simpleNames.add(simpleName);
		}

		return true;
	}

	public Stream<SimpleName> getSimpleNames() {
		return simpleNames.stream();
	}

	public boolean hasSimpleNamesToQualify() {
		return !this.simpleNames.isEmpty();
	}

}