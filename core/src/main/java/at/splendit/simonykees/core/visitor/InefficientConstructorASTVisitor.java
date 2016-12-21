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

		if (ASTNode.METHOD_INVOCATION == node.getParent().getNodeType()) {
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
		if (ASTNode.SIMPLE_TYPE == node.getType().getNodeType()
				&& ASTNode.SIMPLE_NAME == ((SimpleType) node.getType()).getName().getNodeType()
				&& 1 == node.arguments().size()) {
			SimpleName refactorPrimitiveType = (SimpleName) ((SimpleType) node.getType()).getName();
			ITypeBinding refactorPrimitiveTypeBinding = refactorPrimitiveType.resolveTypeBinding();
			Expression refactorCandidateParameter = (Expression) node.arguments().get(0);
			ITypeBinding refactorCandidateTypeBinding = refactorCandidateParameter.resolveTypeBinding();
			Expression replacement = null;

			if (null == refactorCandidateTypeBinding || null == refactorPrimitiveTypeBinding) {
				return true;
			}

			/*
			 * boolean case
			 */
			if (isBooleanClass(refactorPrimitiveTypeBinding.getName())) {
				boolean wrapIfParentMethodInvocation = false;

				/*
				 * all string-literals transformed to its boolean counterpart
				 */
				if (ASTNode.STRING_LITERAL == refactorCandidateParameter.getNodeType()) {
					StringLiteral stringParameter = (StringLiteral) refactorCandidateParameter;
					wrapIfParentMethodInvocation = true;
					if (ReservedNames.BOOLEAN_TRUE.equals(stringParameter.getLiteralValue())) {
						replacement = node.getAST().newBooleanLiteral(true);
					} else {
						replacement = node.getAST().newBooleanLiteral(false);
					}
				}
				/* wrapping string variables into Boolean.valueOf(...) */
				else if (ClassRelationUtil.isContentOfRegistertITypes(refactorCandidateTypeBinding,
						iTypeMap.get(STRING_KEY))) {
					SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
					replacement = NodeBuilder.newMethodInvocation(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType), valueOfInvocation,
							(SimpleName) astRewrite.createMoveTarget(refactorCandidateParameter));
				}
				/* primitive booleans */
				else if (isBooleanClass(refactorCandidateTypeBinding.getName())) {
					replacement = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
					wrapIfParentMethodInvocation = refactorCandidateTypeBinding.isPrimitive();
				}

				/* wrap if object is needed */
				if (ASTNode.METHOD_INVOCATION == node.getParent().getNodeType() && wrapIfParentMethodInvocation) {
					SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
					replacement = NodeBuilder.newMethodInvocation(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType), valueOfInvocation,
							replacement);
				}
			}

			/*
			 * primitive types
			 */
			else if (isPrimitiveTypeClass(refactorPrimitiveTypeBinding.getName())) {
				/*
				 * new Float(4D) is not transformable to Float.valueOf(4D)
				 * because valueOf only allows primitives that are implicit
				 * cast-able to float. doubles do not have this property
				 */
				Predicate<ITypeBinding> isDoubleVariable = (
						binding) -> (binding != null && (binding.getName().contains(ReservedNames.DOUBLE_PRIMITIVE)
								|| (binding.getName().contains(ReservedNames.DOUBLE))));

				if (ReservedNames.FLOAT.equals(refactorPrimitiveType.getIdentifier())
						&& isDoubleVariable.test(refactorCandidateTypeBinding)) {
					return true;
				}
				
				/* wrapping string variables into PrimitiveType.valueOf(...) */
				if (ClassRelationUtil.isContentOfRegistertITypes(refactorCandidateTypeBinding,
						iTypeMap.get(STRING_KEY))) {
					SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
					replacement = NodeBuilder.newMethodInvocation(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType), valueOfInvocation,
							(SimpleName) astRewrite.createMoveTarget(refactorCandidateParameter));
				}

				/* primitive input parameters */
				else if (isPrimitiveTypeClass(refactorCandidateTypeBinding.getName())) {
					replacement = (Expression) astRewrite.createMoveTarget(refactorCandidateParameter);
					/*
					 * if the new object is part of a method invocation it needs to
					 * be wrapped into a object
					 */
					if (ASTNode.METHOD_INVOCATION == node.getParent().getNodeType()
							&& refactorCandidateTypeBinding.isPrimitive()) {
						SimpleName valueOfInvocation = NodeBuilder.newSimpleName(node.getAST(), ReservedNames.MI_VALUE_OF);
						replacement = NodeBuilder.newMethodInvocation(node.getAST(),
								(SimpleName) astRewrite.createMoveTarget(refactorPrimitiveType), valueOfInvocation,
								replacement);
					}
				}
			}
			if (replacement != null) {
				astRewrite.replace(node, replacement, null);
			}
		}
		return true;
	}

	private boolean isPrimitiveTypeClass(String simpleName) {
		switch (simpleName) {
		case ReservedNames.INTEGER:
		case ReservedNames.FLOAT:
		case ReservedNames.DOUBLE:
		case ReservedNames.LONG:
		case ReservedNames.SHORT:
		case ReservedNames.CHARACTER:
		case ReservedNames.BYTE:
		case ReservedNames.INTEGER_PRIMITIVE:
		case ReservedNames.FLOAT_PRIMITIVE:
		case ReservedNames.DOUBLE_PRIMITIVE:
		case ReservedNames.LONG_PRIMITIVE:
		case ReservedNames.SHORT_PRIMITIVE:
		case ReservedNames.CHARACTER_PRIMITIVE:
		case ReservedNames.BYTE_PRIMITIVE:
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
