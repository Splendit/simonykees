package at.splendit.simonykees.core.visitor;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.constants.ReservedNames;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Primitives should not use the constructor for construction of new Variables.
 * Instead the .valueOf(..) should be used
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer STRING_KEY = 1;
	private static final String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public InefficientConstructorASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		/*
		 * Boolean.valueOf(true); -> true, Boolean.valueOf("true"); -> true
		 * Boolean.valueOf(false); -> false, Boolean.valueOf("false"); -> false
		 * Boolean.valueOf("anyOtherString"); -> false Boolean/boolean b = ...;
		 * Boolean.valueOf(b); -> b String s = ...; Boolean.valueOf(s); ->
		 * ignore
		 */
		if (node.getExpression() == null) {
			return true;
		}

		if (StringUtils.equals(ReservedNames.MI_VALUE_OF, node.getName().getFullyQualifiedName())
				&& null != node.getExpression() && ASTNode.SIMPLE_NAME == node.getExpression().getNodeType()
				&& 1 == node.arguments().size()) {
			SimpleName refactorPrimitiveType = (SimpleName) node.getExpression();
			ITypeBinding refactorPrimitiveTypeBinding = refactorPrimitiveType.resolveTypeBinding();
			Expression refactorCandidateParameter = (Expression) node.arguments().get(0);
			ITypeBinding refactorCandidateTypeBinding = refactorCandidateParameter.resolveTypeBinding();
			if (null != refactorPrimitiveTypeBinding && isBooleanClass(refactorPrimitiveTypeBinding.getName())) {
				if (ASTNode.STRING_LITERAL == refactorCandidateParameter.getNodeType()) {
					StringLiteral stringParameter = (StringLiteral) refactorCandidateParameter;
					if (ReservedNames.BOOLEAN_TRUE.equals(stringParameter.getLiteralValue())) {
						refactorCandidateParameter = node.getAST().newBooleanLiteral(true);
					} else {
						refactorCandidateParameter = node.getAST().newBooleanLiteral(false);
					}
				} else if (ClassRelationUtil.isContentOfRegistertITypes(refactorCandidateTypeBinding,
						iTypeMap.get(STRING_KEY))) {
					return true;
				} else {
					refactorCandidateParameter = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
				}
				astRewrite.replace(node, refactorCandidateParameter, null);
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		/*
		 * Second case: new Integer(myInt).toString()
		 */
		Expression refactorCandidateParameter = null;
		SimpleName refactorPrimitiveType = null;
		ITypeBinding refactorCandidateTypeBinding = null;
		ITypeBinding refactorPrimitiveTypeBinding = null;

		if (ASTNode.SIMPLE_TYPE == node.getType().getNodeType()
				&& ASTNode.SIMPLE_NAME == ((SimpleType) node.getType()).getName().getNodeType()
				&& 1 == node.arguments().size()) {
			refactorPrimitiveType = (SimpleName) ((SimpleType) node.getType()).getName();
			refactorPrimitiveTypeBinding = refactorPrimitiveType.resolveTypeBinding();
			refactorCandidateParameter = (Expression) node.arguments().get(0);
			refactorCandidateTypeBinding = refactorCandidateParameter.resolveTypeBinding();

			/*
			 * new Float(4D).toString() is not transformable to
			 * Float.toString(4D) because toString only allows primitives that
			 * are implicit cast-able to float. doubles do not have this
			 * property
			 */
			Predicate<ITypeBinding> isDoubleVariable = (
					binding) -> (binding != null && (binding.getName().contains(ReservedNames.DOUBLE_PRIMITIVE)
							|| (binding.getName().contains(ReservedNames.DOUBLE))));

			if (ReservedNames.FLOAT.equals(refactorPrimitiveType.getIdentifier())
					&& isDoubleVariable.test(refactorCandidateTypeBinding)) {
				return true;
			}

			if (null != refactorPrimitiveTypeBinding && isBooleanClass(refactorPrimitiveTypeBinding.getName())) {
				if (ASTNode.STRING_LITERAL == refactorCandidateParameter.getNodeType()) {
					StringLiteral stringParameter = (StringLiteral) refactorCandidateParameter;
					if (ReservedNames.BOOLEAN_TRUE.equals(stringParameter.getLiteralValue())) {
						refactorCandidateParameter = node.getAST().newBooleanLiteral(true);
					} else {
						refactorCandidateParameter = node.getAST().newBooleanLiteral(false);
					}
				} else if (ClassRelationUtil.isContentOfRegistertITypes(refactorCandidateTypeBinding,
						iTypeMap.get(STRING_KEY))) {
					refactorPrimitiveType = (SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType);
					refactorCandidateParameter = (SimpleName) astRewrite.createMoveTarget(refactorCandidateParameter);
					SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
					refactorCandidateParameter = NodeBuilder.newMethodInvocation(node.getAST(), refactorPrimitiveType,
							valueOfInvocation, refactorCandidateParameter);
				} else {
					refactorCandidateParameter = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
				}
				astRewrite.replace(node, refactorCandidateParameter, null);
			}
			else if (refactorCandidateTypeBinding != null && isPrimitiveNumberClass(refactorCandidateTypeBinding.getName())) {
				refactorCandidateParameter = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
				astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(refactorCandidateParameter, null);
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
