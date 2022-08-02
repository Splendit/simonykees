package eu.jsparrow.core.visitor.logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.exception.visitor.UnresolvedTypeBindingException;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ReplaceWrongClassForLoggerAnalyzer {

	private static final String GET_LOGGER = "getLogger"; //$NON-NLS-1$
	private static final String GET_NAME = "getName"; //$NON-NLS-1$

	private static final String ORG_SLF4J_LOGGER_FACTORY = "org.slf4j.LoggerFactory"; //$NON-NLS-1$
	private static final String ORG_APACHE_LOGGING_LOG4J_LOG_MANAGER = "org.apache.logging.log4j.LogManager"; //$NON-NLS-1$
	private static final String ORG_APACHE_LOG4J_LOG_MANAGER = "org.apache.log4j.LogManager"; //$NON-NLS-1$
	private static final String JAVA_UTIL_LOGGING_LOGGER = "java.util.logging.Logger"; //$NON-NLS-1$

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

	static boolean isClassLiteralToReplace(TypeLiteral typeLiteral,
			AbstractTypeDeclaration surroundingTypeDeclaration, CompilationUnit compilationUnit) throws UnresolvedTypeBindingException {

		MethodInvocation getLoggerInvocation = findGetLoggerInvocationForTypeLiteral(typeLiteral)
			.orElse(null);
		if (getLoggerInvocation == null) {
			return false;
		}
		if (ForeignTypeLiteral.isForeignTypeLiteral(typeLiteral, surroundingTypeDeclaration, compilationUnit)) {
			return analyzeGetLoggerInvocation(getLoggerInvocation);
		}
		return false;

	}

	private static Optional<MethodInvocation> findGetLoggerInvocationForTypeLiteral(TypeLiteral typeLiteral) {
		if (typeLiteral.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation assumedGetNameInvocation = (MethodInvocation) typeLiteral.getParent();
			if (assumedGetNameInvocation.getName()
				.getIdentifier()
				.equals(GET_NAME)) {
				return findGetLoggerInvocation(assumedGetNameInvocation);
			}
			return Optional.empty();
		}
		return findGetLoggerInvocation(typeLiteral);
	}

	private static Optional<MethodInvocation> findGetLoggerInvocation(Expression expression) {

		if (expression.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return Optional.empty();
		}
		MethodInvocation invocationToAnalyze = (MethodInvocation) expression.getParent();
		if (!invocationToAnalyze.getName()
			.getIdentifier()
			.equals(GET_LOGGER)) {
			return Optional.empty();
		}
		if(invocationToAnalyze.arguments().indexOf(expression) != 0) {
			return Optional.empty(); 
		}
		return Optional.of(invocationToAnalyze);
		
	}

	private static boolean analyzeGetLoggerInvocation(MethodInvocation getLoggerInvocation) throws UnresolvedTypeBindingException {
		IMethodBinding getLoggerMethodBinding = getLoggerInvocation.resolveMethodBinding();
		if (getLoggerMethodBinding == null) {
			throw new UnresolvedTypeBindingException(String.format("Cannot resolve method binding for getLogger invocation {%s}.", getLoggerInvocation.toString())); //$NON-NLS-1$
		}

		if (getLoggerInvocation.resolveTypeBinding() == null) {
			throw new UnresolvedTypeBindingException(String.format("Cannot resolve type binding for getLogger invocation {%s}.", getLoggerInvocation.toString())); //$NON-NLS-1$
		}

		ITypeBinding loggerFactoryClass = getLoggerMethodBinding.getDeclaringClass();
		if (ClassRelationUtil.isContentOfType(loggerFactoryClass, ORG_SLF4J_LOGGER_FACTORY) ||
				ClassRelationUtil.isContentOfType(loggerFactoryClass, ORG_APACHE_LOGGING_LOG4J_LOG_MANAGER)) {
			return true;
		}

		if (getLoggerInvocation.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
			return false;
		}

		MethodInvocation ecpectedLogInvocation = (MethodInvocation) getLoggerInvocation.getParent();
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

	private ReplaceWrongClassForLoggerAnalyzer() {
		// private default constructor hiding implicit public one
	}
}
