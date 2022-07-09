package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.markers.common.ReplaceWrongClassForLoggerEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 4.13.0
 *
 */
public class ReplaceWrongClassForLoggerASTVisitor extends AbstractASTRewriteASTVisitor implements ReplaceWrongClassForLoggerEvent {

	private static final String GET_LOGGER = "getLogger"; //$NON-NLS-1$
	private static final String GET_NAME = "getName"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static final List<String> SUPPORTED_LOGGER_FACTORIES = Collections.unmodifiableList(
			Arrays.asList("java.util.logging.Logger",
					"org.slf4j.LoggerFactory",
					"org.apache.log4j.LogManager",
					"org.apache.logging.log4j.LogManager"));

	@Override
	public boolean visit(TypeLiteral node) {

		if (isUsedToInitializeLogger(node)) {
			AbstractTypeDeclaration surroundingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(node,
					AbstractTypeDeclaration.class);
			ITypeBinding typeLiteralBinding = node.getType()
				.resolveBinding();
			ITypeBinding surroundingTypeDeclarationBinding = surroundingTypeDeclaration.resolveBinding();

			if (!ClassRelationUtil.compareITypeBinding(typeLiteralBinding, surroundingTypeDeclarationBinding)) {
				TypeLiteral typeLiteralReplacement = createTypeLiteralReplacement(surroundingTypeDeclarationBinding);
				astRewrite.replace(node, typeLiteralReplacement, null);
				onRewrite();
				addMarkerEvent(node);
			}
		}
		return false;
	}

	private boolean isUsedToInitializeLogger(TypeLiteral typeLiteral) {

		if (typeLiteral.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return isGetLoggerMethod((MethodInvocation) typeLiteral.getParent());
		}

		if (typeLiteral.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return false;
		}

		MethodInvocation methodInvocation = (MethodInvocation) typeLiteral.getParent();
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(GET_NAME)) {
			return false;
		}

		if (methodInvocation.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return false;
		}
		return isGetLoggerMethod((MethodInvocation) methodInvocation.getParent());

	}

	boolean isGetLoggerMethod(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(GET_LOGGER)) {
			return false;
		}
		if (methodInvocation.resolveTypeBinding() == null) {
			return false;
		}
		IMethodBinding loggerMethodBinding = methodInvocation.resolveMethodBinding();
		if (loggerMethodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = loggerMethodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfTypes(declaringClass, SUPPORTED_LOGGER_FACTORIES);
	}

	private TypeLiteral createTypeLiteralReplacement(ITypeBinding surroundingTypeDeclarationBinding) {

		AST ast = getASTRewrite().getAST();
		SimpleName newSimpleName = ast.newSimpleName(surroundingTypeDeclarationBinding.getName());
		SimpleType newSimpleType = ast.newSimpleType(newSimpleName);
		TypeLiteral newTypeLiteral = ast.newTypeLiteral();
		newTypeLiteral.setType(newSimpleType);
		return newTypeLiteral;
	}

}
