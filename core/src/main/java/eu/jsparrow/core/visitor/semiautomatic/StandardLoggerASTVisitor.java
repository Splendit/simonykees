package eu.jsparrow.core.visitor.semiautomatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.core.visitor.sub.LocalVariableUsagesASTVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * Replaces the occurrences of {@code System.out/err.print/ln} and
 * {@code Throwable::printStackTrace()} with a logger method. The qualified name
 * of the logger and the replacing options must be provided as constructor
 * parameters, otherwise the visiting is interrupted.
 * 
 * <pre>
 * 
 * As an example, assuming that the <b>default</b> replacing options from
 * {@link StandardLoggerRule#getDefaultOptions()} are activated, the following
 * replacements are possible:
 * 
 * <ul>
 * <li>The occurrences of {@code System.out.println("Some message");} and
 * {@code System.out.print("Some message");}will be replaced with:
 * {@code logger.info("Some message")}</li>
 * <li>The occurrences of {@code System.err.println("Error message");} and
 * {@code System.err.print("Error message");} will be replaced with:
 * {@code logger.error("Error message")}</li>
 * <li>The occurrences of {@code e.printStackTrace()} will be replaced with:
 * {@code logger.error(e.getMessage(), e)}</li>
 * </ul>
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class StandardLoggerASTVisitor extends AbstractAddImportASTVisitor {

	private static final String JAVA_LANG_SYSTEM = java.lang.System.class.getName();
	private static final String JAVA_LANG_THROWABLE = java.lang.Throwable.class.getName();
	private static final String OUT = "out"; //$NON-NLS-1$
	private static final String PRINT = "print"; //$NON-NLS-1$
	private static final String PRINTF = "printf"; //$NON-NLS-1$
	private static final String PRINTLN = "println"; //$NON-NLS-1$
	private static final String ERR = "err"; //$NON-NLS-1$
	private static final String PRINT_STACK_TRACE = "printStackTrace"; //$NON-NLS-1$
	private static final String DEFAULT_LOGGER_NAME = "logger"; //$NON-NLS-1$
	private static final String SLF4J_LOGGER_GET_LOGGER = "getLogger"; //$NON-NLS-1$
	private static final String THROWABLE_GET_MESSAGE = "getMessage"; //$NON-NLS-1$
	private static final String LOGGER_CLASS_NAME = org.slf4j.Logger.class.getSimpleName();
	private static final String SLF4J_LOGGER_FACTORY = org.slf4j.LoggerFactory.class.getSimpleName();
	private static final String LOG4J_LOGGER_MANAGER = "LogManager"; //$NON-NLS-1$
	private static final String LOG4J_GET_LOGGER = "getLogger"; //$NON-NLS-1$
	private static final String SLF4J_LOGGER_FACTORY_QUALIFIED_NAME = org.slf4j.LoggerFactory.class.getName();
	private static final String VALUE_OF = "valueOf"; //$NON-NLS-1$
	private static final String FORMAT = "format"; //$NON-NLS-1$
	/**
	 * log4j is not within the class path
	 */
	private static final String LOG4J_LOGGER_FACTORY_QUALIFIED_NAME = "org.apache.logging.log4j.LogManager"; //$NON-NLS-1$
	private static final String SEPARATOR = "->"; //$NON-NLS-1$

	private static final List<String> exceptionQualifiedName = Collections
			.singletonList(java.lang.Exception.class.getName());

	private boolean importsNeeded = false;

	private Map<String, String> replacingOptions;
	private String loggerQualifiedName;
	private CompilationUnit compilationUnit;
	private AbstractTypeDeclaration typeDeclaration;
	private AbstractTypeDeclaration rootType;
	private int nestedTypeDeclarationLevel = 0;
	private Map<String, List<String>> newImports;
	private Map<String, String> loggerNames;

	public StandardLoggerASTVisitor(String loggerQualifiedName, Map<String, String> replacingOptions) {
		this.replacingOptions = replacingOptions;
		this.loggerQualifiedName = loggerQualifiedName;
		this.loggerNames = new HashMap<>();
		this.newImports = new HashMap<>();

		List<String> slf4jImports = new ArrayList<>();
		slf4jImports.add(StandardLoggerConstants.SLF4J_LOGGER);
		slf4jImports.add(SLF4J_LOGGER_FACTORY_QUALIFIED_NAME);
		newImports.put(StandardLoggerConstants.SLF4J_LOGGER, slf4jImports);
		List<String> log4jImports = new ArrayList<>();
		log4jImports.add(StandardLoggerConstants.LOG4J_LOGGER);
		log4jImports.add(LOG4J_LOGGER_FACTORY_QUALIFIED_NAME);
		newImports.put(StandardLoggerConstants.LOG4J_LOGGER, log4jImports);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		// rule precondition: all options must be set
		return loggerQualifiedName != null && replacingOptions != null
				&& replacingOptions.containsKey(StandardLoggerConstants.PRINT_STACKTRACE_KEY)
				&& replacingOptions.containsKey(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY)
				&& replacingOptions.containsKey(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY)
				&& replacingOptions.containsKey(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY)
				&& replacingOptions.containsKey(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY)
				&& replacingOptions.containsKey(StandardLoggerConstants.MISSING_LOG_KEY);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		importsNeeded = false;
		this.compilationUnit = compilationUnit;

		ClashingLoggerTypesASTVisitor clashingTypesVisitor = new ClashingLoggerTypesASTVisitor();
		compilationUnit.accept(clashingTypesVisitor);
		boolean noClashingTypes = clashingTypesVisitor.isLoggerFree();

		// checking whether there is a logger imported!!!
		boolean exisitngLoggerImported = ASTNodeUtil
				.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class).stream()
				.filter(importDecl -> !importDecl.isOnDemand()).map(ImportDeclaration::getName)
				.filter(Name::isQualifiedName).map(name -> ((QualifiedName) name).getName())
				.map(SimpleName::getIdentifier).anyMatch(LOGGER_CLASS_NAME::equals);

		return noClashingTypes && !exisitngLoggerImported && super.visit(compilationUnit);
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		if (importsNeeded) {
			super.addImports.addAll(newImports.get(loggerQualifiedName));
		}
		super.endVisit(compilationUnit);
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		visitNewTypeDeclaration(typeDeclaration);
		return true;
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		endVisitNewTypeDeclaration(typeDeclaration);
	}

	@Override
	public boolean visit(EnumDeclaration enumDeclaration) {
		visitNewTypeDeclaration(enumDeclaration);
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration enumDeclaration) {
		return false;
	}

	@Override
	public void endVisit(EnumDeclaration enumDeclaration) {
		endVisitNewTypeDeclaration(enumDeclaration);
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		/*
		 * Since it is not possible to have a static field in a nested class,
		 * the introduced logger has to be an instance field too. Therefore, it
		 * cannot be used in a static method.
		 */
		return !(nestedTypeDeclarationLevel > 1
				&& ASTNodeUtil.hasModifier(methodDeclaration.modifiers(), Modifier::isStatic));
	}

	@Override
	public boolean visit(CatchClause catchClause) {
		String replaceOption = replacingOptions.get(StandardLoggerConstants.MISSING_LOG_KEY);
		if (replaceOption == null || StringUtils.isEmpty(replaceOption)) {
			return true;
		}
		SingleVariableDeclaration exception = catchClause.getException();
		SimpleName exceptionName = exception.getName();

		Block catchBody = catchClause.getBody();
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(exceptionName);
		catchBody.accept(visitor);
		List<SimpleName> exceptionUsages = visitor.getUsages();
		if (!exceptionUsages.isEmpty()) {
			return true;
		}

		if (getLoggerName() == null) {
			addLogger();
		}

		ExpressionStatement loggingStatement = prepareLoggingStatement(exceptionName, replaceOption, getLoggerName());
		ListRewrite statemetnRewrite = astRewrite.getListRewrite(catchBody, Block.STATEMENTS_PROPERTY);
		statemetnRewrite.insertFirst(loggingStatement, null);

		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String methodIdentifier = methodName.getIdentifier();
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		// if the method invocation name is print, printf or println
		if (isPrintMethod(methodIdentifier) && !arguments.isEmpty()) {
			/*
			 * Looking for System.out/err.print/ln where System.out/err is a
			 * qualified name expression of the print/ln method invocation.
			 */
			Expression expression = methodInvocation.getExpression();
			// ... and if the argument of the method invocation is a string
			if (expression == null || ASTNode.QUALIFIED_NAME != expression.getNodeType()) {
				return false;
			}

			QualifiedName expressionQualifier = (QualifiedName) expression;
			Name qualifier = expressionQualifier.getQualifier();
			if (!ClassRelationUtil.isContentOfTypes(qualifier.resolveTypeBinding(),
					Collections.singletonList(JAVA_LANG_SYSTEM))) {
				return false;
			}

			List<Expression> logArguments = calcLogArgument(arguments, methodIdentifier);
			SimpleName qualiferName = expressionQualifier.getName();
			calcReplacingOtion(arguments, qualiferName)
					.ifPresent(replacingOption -> replaceMethod(methodInvocation, replacingOption, logArguments));

		} else if (PRINT_STACK_TRACE.equals(methodIdentifier)
				&& !StringUtils.isEmpty(replacingOptions.get(StandardLoggerConstants.PRINT_STACKTRACE_KEY))) {
			/*
			 * Looking for e.printStackTrace() where 'e' is a throwable object.
			 */
			Expression expression = methodInvocation.getExpression();
			if (expression == null || ASTNode.SIMPLE_NAME != expression.getNodeType()) {
				return true;
			}

			SimpleName simpleName = (SimpleName) expression;
			ITypeBinding iTypeBinding = simpleName.resolveTypeBinding();
			if (ClassRelationUtil.isInheritingContentOfTypes(iTypeBinding,
					Collections.singletonList(JAVA_LANG_THROWABLE))) {
				// replace printStackTrace with the logger method.
				String replacingMethod = replacingOptions.get(StandardLoggerConstants.PRINT_STACKTRACE_KEY);
				replaceMethod(methodInvocation, simpleName, replacingMethod);
			}
		}
		return true;
	}

	/**
	 * Creates a new unparented {@link ExpressionStatement} for logging an
	 * exception. The structure of the expression is:
	 * 
	 * <pre>
	 * {@code [loggerName].[logLevelName]([exceptionName].getMessage(), [exceptionName]);}
	 * 
	 * @param exceptionName
	 *            the name of the exception to be logged.
	 * @param logLevelName
	 *            the name of the logging level to be used
	 * @param loggerName
	 *            the name of the logger object.
	 * @return the newly created statement as described above.
	 */
	private ExpressionStatement prepareLoggingStatement(SimpleName exceptionName, String logLevelName,
			String loggerName) {
		AST ast = astRewrite.getAST();
		MethodInvocation loggingMethodInocation = ast.newMethodInvocation();
		loggingMethodInocation.setName(ast.newSimpleName(logLevelName));
		loggingMethodInocation.setExpression(ast.newSimpleName(loggerName));

		MethodInvocation loggingMessage = ast.newMethodInvocation();
		loggingMessage.setName(ast.newSimpleName(THROWABLE_GET_MESSAGE));
		loggingMessage.setExpression(ast.newSimpleName(exceptionName.getIdentifier()));

		ListRewrite argRewrite = astRewrite.getListRewrite(loggingMethodInocation, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewrite.insertFirst(loggingMessage, null);
		argRewrite.insertLast(astRewrite.createCopyTarget(exceptionName), null);

		return ast.newExpressionStatement(loggingMethodInocation);
	}

	/**
	 * Keeps track of the possibly nested types (classes or enums) declared
	 * inside the compilation unit.
	 * 
	 * @param abstractType
	 *            node representing a type declaration.
	 */
	private void visitNewTypeDeclaration(AbstractTypeDeclaration abstractType) {
		if (nestedTypeDeclarationLevel == 0) {
			this.rootType = abstractType;
		}
		this.typeDeclaration = abstractType;
		this.nestedTypeDeclarationLevel++;
	}

	/**
	 * Discard stored information related to the type after its corresponding
	 * node is visited.
	 * 
	 * @param typeDeclaration2
	 *            end visit node
	 */
	private void endVisitNewTypeDeclaration(AbstractTypeDeclaration typeDeclaration2) {
		nestedTypeDeclarationLevel--;
		this.typeDeclaration = ASTNodeUtil.getSpecificAncestor(typeDeclaration2, AbstractTypeDeclaration.class);
		if (nestedTypeDeclarationLevel == 0) {
			loggerNames.clear();
		} else {
			loggerNames.remove(generateUniqueTypeId(typeDeclaration2));
		}
	}

	private boolean isPrintMethod(String methodIdentifier) {
		return PRINT.equals(methodIdentifier) || PRINTLN.equals(methodIdentifier) || PRINTF.equals(methodIdentifier);
	}

	/**
	 * Computes the argument(s) to be used in a logger statements. Adapts the
	 * parameters of {@link System.out#print}, {@link System.out#printf} and
	 * {@link System.out#println} for both slf4j and log4j.
	 * 
	 * @param arguments
	 *            arguments used in the print statement
	 * @param methodIdentifier
	 *            the name of the method used for printing to standard outputs.
	 * @return the list of the arguments adapted for the
	 *         {@value #loggerQualifiedName}
	 */
	private List<Expression> calcLogArgument(List<Expression> arguments, String methodIdentifier) {
		List<Expression> logArguments = new ArrayList<>();
		List<String> stringQualifiedName = Collections.singletonList(java.lang.String.class.getName());
		Expression firstArgument = arguments.get(0);
		ITypeBinding stArgTypeBinding = firstArgument.resolveTypeBinding();
		if (PRINTF.equals(methodIdentifier)) {
			if (ClassRelationUtil.isContentOfTypes(stArgTypeBinding,
					Collections.singletonList(java.util.Locale.class.getName()))) {
				/*
				 * No corresponding log statements exists for this case. The
				 * arguments shall be wrapped in a String::format
				 */
				AST ast = astRewrite.getAST();
				MethodInvocation stringFormat = ast.newMethodInvocation();
				stringFormat.setName(ast.newSimpleName(FORMAT));
				stringFormat.setExpression(ast.newSimpleName(String.class.getSimpleName()));
				ListRewrite argRewrite = astRewrite.getListRewrite(stringFormat, MethodInvocation.ARGUMENTS_PROPERTY);
				arguments.forEach(arg -> argRewrite.insertLast(arg, null));
				logArguments.add(stringFormat);
			} else {
				/*
				 * Both slf4j and log4j are able accept formatting parameters
				 */
				logArguments.addAll(arguments);
			}

		} else {
			if (ClassRelationUtil.isContentOfTypes(stArgTypeBinding, stringQualifiedName)
					|| ClassRelationUtil.isInheritingContentOfTypes(stArgTypeBinding, stringQualifiedName)
					|| StandardLoggerConstants.LOG4J_LOGGER.equals(loggerQualifiedName)) {
				/*
				 * log4j is able to accept an object as input
				 */
				logArguments.add((Expression) astRewrite.createCopyTarget(firstArgument));
			} else {
				/*
				 * slf4j does NOT accept an Object. Therefore, the argument has
				 * to be wrapped in a String.valueOf
				 */
				AST ast = astRewrite.getAST();
				MethodInvocation stringValueOf = ast.newMethodInvocation();
				stringValueOf.setName(ast.newSimpleName(VALUE_OF));
				stringValueOf.setExpression(ast.newSimpleName(String.class.getSimpleName()));
				ListRewrite argRewrite = astRewrite.getListRewrite(stringValueOf, MethodInvocation.ARGUMENTS_PROPERTY);
				argRewrite.insertFirst((Expression) astRewrite.createCopyTarget(firstArgument), null);
				logArguments.add(stringValueOf);
			}
		}

		return logArguments;
	}

	/**
	 * Computes the logging level to be used, based on the
	 * {@link #replacingOptions}, the method used for printing to standard
	 * output and whether an exception occurs in the parameters used in the
	 * print method.
	 * 
	 * @param arguments
	 *            the list of the arguments occurring in the print method
	 * @param qualiferName
	 *            the qualifier of the print method
	 * @return the optional of the identifier of the logging level to be used,
	 *         or an empty optional if the qualifier name does not match with
	 *         the qualifiers of the standard output of if the replacement
	 *         option is not set in {@link #replacingOptions}.
	 */
	private Optional<String> calcReplacingOtion(List<Expression> arguments, SimpleName qualiferName) {
		ExceptionsASTVisitor visitor = new ExceptionsASTVisitor();
		arguments.forEach(argument -> argument.accept(visitor));
		boolean logsException = !visitor.getExceptions().isEmpty();
		String option = ""; //$NON-NLS-1$
		if (logsException && OUT.equals(qualiferName.getIdentifier())) {
			option = replacingOptions.get(StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY);
		} else if (logsException && ERR.equals(qualiferName.getIdentifier())) {
			option = replacingOptions.get(StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY);
		} else if (OUT.equals(qualiferName.getIdentifier())) {
			option = replacingOptions.get(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY);
		} else if (ERR.equals(qualiferName.getIdentifier())) {
			option = replacingOptions.get(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY);
		}

		return Optional.of(option).filter(s -> !s.isEmpty());
	}

	/**
	 * Replaces the method invocation with a logger method having one string as
	 * a parameter
	 * 
	 * @param methodInvocation
	 *            to be replaced
	 * @param replacingMethod
	 *            name of the replacing method
	 * @param logArgument
	 *            the expression being logged
	 */
	private void replaceMethod(MethodInvocation methodInvocation, String replacingMethod,
			List<Expression> logArguments) {
		if (getLoggerName() == null) {
			addLogger();
		}
		String loggerNameIdentifier = getLoggerName();
		AST ast = methodInvocation.getAST();
		SimpleName loggerMethodName = ast.newSimpleName(replacingMethod);
		SimpleName loggerName = ast.newSimpleName(loggerNameIdentifier);
		astRewrite.replace(methodInvocation.getName(), loggerMethodName, null);
		astRewrite.replace(methodInvocation.getExpression(), loggerName, null);
		ListRewrite argRewrite = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
				.forEach(arg -> argRewrite.remove(arg, null));
		logArguments.forEach(logArgument -> argRewrite.insertLast(logArgument, null));
	}

	/**
	 * Replaces the given method invocation with a logger method having the
	 * error message and the throwable object as parameters. For example:
	 * 
	 * {@code e.printStackTrace();}
	 * 
	 * is replaced with:
	 * 
	 * {@code logger.error(e.getMessage(), e);}
	 * 
	 * @param methodInvocation
	 *            the method invocation to be replaced
	 * @param throwableName
	 *            the name of the throwable object
	 * @param replacingMethod
	 *            the replacing method name of the logger
	 */
	private void replaceMethod(MethodInvocation methodInvocation, SimpleName throwableName, String replacingMethod) {
		if (getLoggerName() == null) {
			addLogger();
		}
		String loggerNameIdentifier = getLoggerName();
		AST ast = methodInvocation.getAST();
		SimpleName loggerMethodName = ast.newSimpleName(replacingMethod);
		SimpleName loggerName = ast.newSimpleName(loggerNameIdentifier);
		MethodInvocation newLoggerMethod = ast.newMethodInvocation();
		newLoggerMethod.setName(loggerMethodName);
		newLoggerMethod.setExpression(loggerName);

		MethodInvocation eGetMessage = ast.newMethodInvocation();
		eGetMessage.setName(ast.newSimpleName(THROWABLE_GET_MESSAGE));
		eGetMessage.setExpression((SimpleName) astRewrite.createCopyTarget(throwableName));

		ListRewrite miListRewrite = astRewrite.getListRewrite(newLoggerMethod, MethodInvocation.ARGUMENTS_PROPERTY);
		miListRewrite.insertLast(eGetMessage, null);
		miListRewrite.insertLast(astRewrite.createCopyTarget(throwableName), null);

		astRewrite.replace(methodInvocation, newLoggerMethod, null);
	}

	/**
	 * Creates a logger object as a final field and initializes it using a
	 * proper factory. The field is inserted at the beginning of the class body.
	 */
	private void addLogger() {
		importsNeeded = true;
		String loggerName = generateLoggerName();
		setCurrentLoggerName(loggerName);
		AST ast = compilationUnit.getAST();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		FieldDeclaration loggerDeclaration = ast.newFieldDeclaration(fragment);
		fragment.setName(ast.newSimpleName(loggerName));
		Expression loggerInitializer = generateLoggerInitializer(loggerDeclaration);
		fragment.setInitializer(loggerInitializer);
		Type loggerType = ast.newSimpleType(ast.newName(LOGGER_CLASS_NAME));
		loggerDeclaration.setType(loggerType);
		ListRewrite loggerListRewirte = astRewrite.getListRewrite(loggerDeclaration,
				FieldDeclaration.MODIFIERS2_PROPERTY);
		if (!isInterface(typeDeclaration)) {
			Modifier privateModifier = ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD);
			loggerListRewirte.insertLast(privateModifier, null);
		}
		Modifier finalModifier = ast.newModifier(ModifierKeyword.FINAL_KEYWORD);
		if (nestedTypeDeclarationLevel == 1) {
			Modifier staticModifier = ast.newModifier(ModifierKeyword.STATIC_KEYWORD);
			loggerListRewirte.insertLast(staticModifier, null);
		}
		loggerListRewirte.insertLast(finalModifier, null);

		ListRewrite listRewrite = astRewrite.getListRewrite(typeDeclaration, getBodyDeclarationProperty());
		listRewrite.insertFirst(loggerDeclaration, null);
	}

	private boolean isInterface(AbstractTypeDeclaration typeDeclaration2) {
		return typeDeclaration2 instanceof TypeDeclaration && ((TypeDeclaration) typeDeclaration2).isInterface();
	}

	private ChildListPropertyDescriptor getBodyDeclarationProperty() {

		ChildListPropertyDescriptor typeDeclarationProperty;
		if (this.typeDeclaration instanceof TypeDeclaration) {
			typeDeclarationProperty = TypeDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else if (this.typeDeclaration instanceof EnumDeclaration) {
			typeDeclarationProperty = EnumDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else {
			typeDeclarationProperty = AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY;
		}
		return typeDeclarationProperty;
	}

	/**
	 * Stores the name of the logger for the current type declaration which is
	 * being visited. Generates a unique identification for it.
	 * 
	 * @param loggerName
	 *            name to be stored.
	 */
	private void setCurrentLoggerName(String loggerName) {
		loggerNames.put(generateUniqueTypeId(this.typeDeclaration), loggerName);
	}

	/**
	 * Generates an initializer expression for the logger based on the qualified
	 * name of the logger ({@value #typeDeclaration}). The initializer generated
	 * for {@value StandardLoggerConstants#SLF4J_LOGGER} is:
	 * 
	 * {@code LoggerFactory.getLogger()}
	 * 
	 * whereas for {@value StandardLoggerConstants#LOG4J_LOGGER} is:
	 * 
	 * @param loggerDeclaration
	 *            Field declaration representing the logger declaration.
	 * 
	 * @return the generated initializer expression
	 */
	private Expression generateLoggerInitializer(FieldDeclaration loggerDeclaration) {
		Expression initializer;
		AST ast = loggerDeclaration.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		ListRewrite miListRewrite = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		TypeLiteral typeLiteral = ast.newTypeLiteral();
		typeLiteral.setType(ast.newSimpleType(ast.newName(typeDeclaration.getName().getIdentifier())));

		switch (this.loggerQualifiedName) {
		case StandardLoggerConstants.SLF4J_LOGGER:

			methodInvocation.setName(ast.newSimpleName(SLF4J_LOGGER_GET_LOGGER));
			miListRewrite.insertFirst(typeLiteral, null);
			methodInvocation.setExpression(ast.newSimpleName(SLF4J_LOGGER_FACTORY));
			initializer = methodInvocation;
			break;
		case StandardLoggerConstants.LOG4J_LOGGER:
			methodInvocation.setName(ast.newSimpleName(LOG4J_GET_LOGGER));
			miListRewrite.insertFirst(typeLiteral, null);
			methodInvocation.setExpression(ast.newSimpleName(LOG4J_LOGGER_MANAGER));
			initializer = methodInvocation;
			break;
		default:
			initializer = null;
			break;
		}
		return initializer;
	}

	/**
	 * Generates a name for the logger object. Avoids clashes with the rest of
	 * the fields in the current class or in the outer classes in case the
	 * logger is being introduced in a nested class. The default logger name is
	 * {@value #DEFAULT_LOGGER_NAME}. A number is added as a suffix if the
	 * default name is already taken by some other object within the scope.
	 * 
	 * @return a string representing the logger name.
	 */
	private String generateLoggerName() {

		VariableDeclarationsVisitor declVisitor = new VariableDeclarationsVisitor();
		rootType.accept(declVisitor);
		List<String> declaredNames = declVisitor.getVariableDeclarationNames().stream().map(SimpleName::getIdentifier)
				.distinct().collect(Collectors.toList());
		String suffix = ""; //$NON-NLS-1$
		int count = 0;
		while (declaredNames.contains(DEFAULT_LOGGER_NAME + suffix)
				|| loggerNames.containsValue(DEFAULT_LOGGER_NAME + suffix)) {
			count++;
			suffix = Integer.toString(count);
		}

		return DEFAULT_LOGGER_NAME + suffix;
	}

	private String getLoggerName() {
		return loggerNames.get(generateUniqueTypeId(this.typeDeclaration));
	}

	/**
	 * Generates a unique id for the logger name declared in the body of a type.
	 * 
	 * @param typeDeclaration
	 *            a node representing a type declaration.
	 * 
	 * @return a mixture of the type name, its starting position in the
	 *         compilation unit and its length.
	 */
	private String generateUniqueTypeId(AbstractTypeDeclaration typeDeclaration) {
		return typeDeclaration.getName().getIdentifier() + SEPARATOR + typeDeclaration.getStartPosition() + SEPARATOR
				+ typeDeclaration.getLength();
	}

	/**
	 * Checks for occurrences of {@link SimpleType}s with name
	 * {@value #LOGGER_CLASS_NAME}, {@value #SLF4J_LOGGER_FACTORY} or
	 * {@value #SLF4J_LOGGER_FACTORY}.
	 * 
	 * @author Ardit Ymeri
	 * @since 1.2
	 *
	 */
	private class ClashingLoggerTypesASTVisitor extends ASTVisitor {

		boolean clashingFound = false;

		@Override
		public boolean preVisit2(ASTNode node) {
			return !clashingFound;
		}

		@Override
		public boolean visit(TypeDeclaration typeDeclaration) {
			String typeIdentifier = typeDeclaration.getName().getIdentifier();
			if (isClashingLoggerName(typeIdentifier)) {
				clashingFound = true;
			}
			return true;
		}

		@Override
		public boolean visit(SimpleType simpleType) {
			Name typeName = simpleType.getName();
			if (typeName.isSimpleName() && isClashingLoggerName(((SimpleName) typeName).getIdentifier())) {
				clashingFound = true;
			}
			return true;
		}

		private boolean isClashingLoggerName(String typeIdentifier) {
			return LOGGER_CLASS_NAME.equals(typeIdentifier) || LOG4J_LOGGER_MANAGER.equals(typeIdentifier)
					|| SLF4J_LOGGER_FACTORY.equals(typeIdentifier);
		}

		public boolean isLoggerFree() {
			return !clashingFound;
		}
	}

	class ExceptionsASTVisitor extends ASTVisitor {
		private List<ASTNode> foundExceptions = new ArrayList<>();

		@Override
		public boolean visit(SimpleName simpleName) {
			IBinding binding = simpleName.resolveBinding();
			if (binding != null && IBinding.VARIABLE == binding.getKind()) {
				ITypeBinding typeBinding = simpleName.resolveTypeBinding();
				storeIfExceptionType(typeBinding, simpleName);
			}
			return true;
		}

		@Override
		public boolean visit(ClassInstanceCreation classCreation) {
			ITypeBinding typeBinding = classCreation.resolveTypeBinding();
			storeIfExceptionType(typeBinding, classCreation);
			return true;
		}

		private void storeIfExceptionType(ITypeBinding typeBinding, ASTNode node) {
			if (typeBinding != null && (ClassRelationUtil.isContentOfTypes(typeBinding, exceptionQualifiedName)
					|| ClassRelationUtil.isInheritingContentOfTypes(typeBinding, exceptionQualifiedName))) {
				foundExceptions.add(node);
			}
		}

		public List<ASTNode> getExceptions() {
			return this.foundExceptions;
		}
	}
}
