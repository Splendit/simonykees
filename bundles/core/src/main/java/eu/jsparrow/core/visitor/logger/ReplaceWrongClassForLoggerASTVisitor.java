package eu.jsparrow.core.visitor.logger;

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
public class ReplaceWrongClassForLoggerASTVisitor extends AbstractASTRewriteASTVisitor
		implements ReplaceWrongClassForLoggerEvent {

	private static final String ORG_SLF4J_LOGGER_FACTORY = "org.slf4j.LoggerFactory"; //$NON-NLS-1$
	private static final String ORG_APACHE_LOGGING_LOG4J_LOG_MANAGER = "org.apache.logging.log4j.LogManager"; //$NON-NLS-1$
	private static final String ORG_APACHE_LOG4J_LOG_MANAGER = "org.apache.log4j.LogManager"; //$NON-NLS-1$
	private static final String JAVA_UTIL_LOGGING_LOGGER = "java.util.logging.Logger"; //$NON-NLS-1$

	private static final String GET_LOGGER = "getLogger"; //$NON-NLS-1$
	private static final String GET_NAME = "getName"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static final List<String> LOG4J_1_LOG_METHODS = Collections.unmodifiableList(
			Arrays.asList(
					"debug",
					"error",
					"fatal",
					"info",
					"log",
					"trace",
					"warn"));

	@SuppressWarnings("nls")
	private static final List<String> JAVA_UTIL_LOG_METHODS = Collections.unmodifiableList(
			Arrays.asList(
					"config",
					"entering",
					"exiting",
					"fine",
					"finer",
					"finest",
					"info",
					"log",
					"logp",
					"logrb",
					"severe",
					"throwing",
					"warning"));

	@Override
	public boolean visit(TypeLiteral node) {
		AbstractTypeDeclaration surroundingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(node,
				AbstractTypeDeclaration.class);
		ITypeBinding typeLiteralBinding = node.getType()
			.resolveBinding();
		ITypeBinding surroundingTypeDeclarationBinding = surroundingTypeDeclaration.resolveBinding();

		if (!ClassRelationUtil.compareITypeBinding(typeLiteralBinding, surroundingTypeDeclarationBinding)
				&& isUsedForSupportedGetLoggerInvocation(node)) {
			TypeLiteral typeLiteralReplacement = createTypeLiteralReplacement(surroundingTypeDeclarationBinding);
			astRewrite.replace(node, typeLiteralReplacement, null);
			onRewrite();
			addMarkerEvent(node);
		}
		return false;
	}

	private boolean isUsedForSupportedGetLoggerInvocation(TypeLiteral typeLiteral) {

		if (typeLiteral.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return isSupportedGetLoggerMethod((MethodInvocation) typeLiteral.getParent());
		}

		if (typeLiteral.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return false;
		}

		MethodInvocation methodInvocationOnTypeLiteral = (MethodInvocation) typeLiteral.getParent();
		if (!methodInvocationOnTypeLiteral.getName()
			.getIdentifier()
			.equals(GET_NAME)) {
			return false;
		}

		if (methodInvocationOnTypeLiteral.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return false;
		}
		return isSupportedGetLoggerMethod((MethodInvocation) methodInvocationOnTypeLiteral.getParent());

	}

	boolean isSupportedGetLoggerMethod(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(GET_LOGGER)) {
			return false;
		}
		IMethodBinding getLoggerMethodBinding = methodInvocation.resolveMethodBinding();
		if (getLoggerMethodBinding == null) {
			return false;
		}

		if (methodInvocation.resolveTypeBinding() == null) {
			return false;
		}

		ITypeBinding loggerFactoryClass = getLoggerMethodBinding.getDeclaringClass();
		if (ClassRelationUtil.isContentOfType(loggerFactoryClass, ORG_SLF4J_LOGGER_FACTORY) ||
				ClassRelationUtil.isContentOfType(loggerFactoryClass, ORG_APACHE_LOGGING_LOG4J_LOG_MANAGER)) {
			return true;
		}

		if (methodInvocation.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return false;
		}

		MethodInvocation ecpectedLogInvocation = (MethodInvocation) methodInvocation.getParent();
		String loggingMethodIdentifier = ecpectedLogInvocation.getName()
			.getIdentifier();

		if (ClassRelationUtil.isContentOfType(loggerFactoryClass, ORG_APACHE_LOG4J_LOG_MANAGER)) {
			return LOG4J_1_LOG_METHODS.contains(loggingMethodIdentifier);
		}

		if (ClassRelationUtil.isContentOfType(loggerFactoryClass, JAVA_UTIL_LOGGING_LOGGER)) {
			return JAVA_UTIL_LOG_METHODS.contains(loggingMethodIdentifier);
		}

		return false;
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
