package eu.jsparrow.core.visitor.functionalinterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds unqualified references to class fields which may be problematic in
 * connection with the transformation of anonymous classes to lambda
 * expressions.
 * 
 * @since 3.16.0
 */
class UnqualifiedFieldNamesVisitor extends ASTVisitor {

	private final List<SimpleName> simpleNames = new ArrayList<>();

	private final List<ThisExpression> thisExpressionsOfStaticFields = new ArrayList<>();

	private final ITypeBinding anonymousClassTypeBinding;

	UnqualifiedFieldNamesVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	@Override
	public boolean visit(SimpleName simpleName) {

		ASTNode simpleNameParent = simpleName.getParent();
		if (simpleNameParent == null) {
			return true;
		}

		if (simpleNameParent.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) simpleNameParent;
			if (simpleName == qualifiedName.getName()) {
				return true;
			}
		}

		if (simpleNameParent.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) simpleNameParent;

			Expression fieldAccessExpression = fieldAccess.getExpression();
			if (fieldAccessExpression.getNodeType() == ASTNode.THIS_EXPRESSION) {
				thisExpressionsOfStaticFields.add((ThisExpression) fieldAccessExpression);
			}
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
		
		String declaringClassErasureName = declaringClass.getQualifiedName();		
		if (declaringClass.isParameterizedType()) {
			declaringClassErasureName = declaringClass.getErasure().getQualifiedName();
		}
		
		if (ClassRelationUtil.isInheritingContentOfTypes(
				anonymousClassTypeBinding,
				Collections.singletonList(declaringClassErasureName))) {
			simpleNames.add(simpleName);
		}

		return true;
	}

	public Stream<SimpleName> getSimpleNames() {
		return simpleNames.stream();
	}

	public Stream<ThisExpression> getThisExpressionsOfStaticFields() {
		return thisExpressionsOfStaticFields.stream();
	}

}