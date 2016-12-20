package at.splendit.simonykees.core.visitor;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.constants.ReservedNames;

/**
 * Primitives should not use the constructor for construction of new Variables.
 * Instead the .valueOf(..) should be used
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer STRING_KEY = 1;
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public InefficientConstructorASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {

		/*
		 * checks if method invocation is toString. the invocation need to have
		 * zero arguments the expressions type where the toString is used on
		 * needs to be a String or a StringLiteral
		 */
		if (StringUtils.equals(ReservedNames.MI_TO_STRING, node.getName().getFullyQualifiedName())) {

			/*
			 * First case: Integer.valueOf(myInt).toString()
			 */
			if (node.getExpression() == null) {
				return true;
			}

			Expression refactorCandidateExpression = null;
			SimpleName refactorPrimitiveType = null;
			ITypeBinding refactorCandidateTypeBinding = null;

			if (ASTNode.METHOD_INVOCATION == node.getExpression().getNodeType()) {
				MethodInvocation expetedValueOf = (MethodInvocation) node.getExpression();
				if (StringUtils.equals(ReservedNames.MI_VALUE_OF, expetedValueOf.getName().getFullyQualifiedName())
						&& null != expetedValueOf.getExpression()
						&& ASTNode.SIMPLE_NAME == expetedValueOf.getExpression().getNodeType()
						&& 1 == expetedValueOf.arguments().size()) {
					refactorPrimitiveType = (SimpleName) expetedValueOf.getExpression();
					refactorCandidateExpression = (Expression) expetedValueOf.arguments().get(0);
					refactorCandidateTypeBinding = refactorCandidateExpression.resolveTypeBinding();
				}
			}

			/*
			 * Second case: new Integer(myInt).toString()
			 */
			else if (ASTNode.CLASS_INSTANCE_CREATION == node.getExpression().getNodeType()) {
				ClassInstanceCreation expectedPrimitiveNumberClass = (ClassInstanceCreation) node.getExpression();
				if (ASTNode.SIMPLE_TYPE == expectedPrimitiveNumberClass.getType().getNodeType()
						&& ASTNode.SIMPLE_NAME == ((SimpleType) expectedPrimitiveNumberClass.getType()).getName()
								.getNodeType()
						&& 1 == expectedPrimitiveNumberClass.arguments().size()) {
					refactorPrimitiveType = (SimpleName) ((SimpleType) expectedPrimitiveNumberClass.getType())
							.getName();
					refactorCandidateExpression = (Expression) expectedPrimitiveNumberClass.arguments().get(0);
					refactorCandidateTypeBinding = refactorCandidateExpression.resolveTypeBinding();

					/*
					 * new Float(4D).toString() is not transformable to
					 * Float.toString(4D) because toString only allows
					 * primitives that are implicit cast-able to float. doubles
					 * do not have this property
					 */
					Predicate<ITypeBinding> isDoubleVariable = (
							binding) -> (binding != null && (binding.getName().contains(ReservedNames.DOUBLE_PRIMITIVE)
									|| (binding.getName().contains(ReservedNames.DOUBLE))));

					if (ReservedNames.FLOAT.equals(refactorPrimitiveType.getIdentifier())
							&& isDoubleVariable.test(refactorCandidateTypeBinding)) {
						refactorPrimitiveType = null;
						refactorCandidateExpression = null;
						refactorCandidateTypeBinding = null;
					}
				}
			}
			if (refactorCandidateTypeBinding != null
					&& isPrimitiveNumberClass(refactorCandidateTypeBinding.getName())) {
				Expression moveTarget = (Expression) astRewrite.createMoveTarget(refactorCandidateExpression);
				astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(moveTarget, null);
				astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, refactorPrimitiveType, null);
			}

		}

		return true;
	}

	private boolean isPrimitiveNumberClass(String simpleName) {
		switch (simpleName) {
		case ReservedNames.INTEGER:
		case ReservedNames.FLOAT:
		case ReservedNames.DOUBLE:
		case ReservedNames.LONG:
		case ReservedNames.INTEGER_PRIMITIVE:
		case ReservedNames.FLOAT_PRIMITIVE:
		case ReservedNames.DOUBLE_PRIMITIVE:
		case ReservedNames.LONG_PRIMITIVE:
			return true;
		default:
			return false;
		}
	}
	
	private boolean isBooleanClass(String simpleName) {
		switch (simpleName) {
		case ReservedNames.BOOLEAN:
		case ReservedNames.BOOLEAN_PRIMITIVE:
			return true;
		default:
			return false;
		}
	}
}
