package eu.jsparrow.rules.java10.factory.methods;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class CollectionsFactoryMethodsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String UNMODIFIABLE_LIST = "unmodifiableList"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_SET = "unmodifiableSet"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_MAP = "unmodifiableMap"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (!isUnmodifiableCollection(methodInvocation)) {
			return true;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return true;
		}
		Expression argument = arguments.get(0);

		ArgumentAnalyser<?> analyser = null;
		if (argument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation anonymousClass = (ClassInstanceCreation) argument;
			AnonymousClassArgumentAnalyser anonymousClassAnalyser = new AnonymousClassArgumentAnalyser();
			anonymousClassAnalyser.analyzeArgument(anonymousClass);
			analyser = anonymousClassAnalyser;

		} else if (argument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation argumentMethod = (MethodInvocation) argument;
			MethodInvocationArgumentAnalyser methodInvocationanalyser = new MethodInvocationArgumentAnalyser();
			methodInvocationanalyser.analyzeArgument(argumentMethod);
			analyser = methodInvocationanalyser;

		} else if (argument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleNameArgumentAnalyser simpleNameAnalyser = new SimpleNameArgumentAnalyser();
			SimpleName name = (SimpleName) argument;
			simpleNameAnalyser.analyzeArgument(name);
			analyser = simpleNameAnalyser;
		}

		if (analyser == null) {
			return true;
		}

		List<Expression> elements = analyser.getElements();
		if (elements == null) {
			return true;
		}

		if (!isNullSafe(elements)) {
			return true;
		}

		String expressionTypeName = findExpressionTypeName(methodInvocation);
		if (expressionTypeName.isEmpty()) {
			return true;
		}

		List<Expression> newArguments = elements.stream()
			.map(element -> (Expression) astRewrite.createCopyTarget(element))
			.collect(Collectors.toList());

		Expression factoryMethod = createCollectionFactoryMethod(expressionTypeName, newArguments);
		astRewrite.replace(methodInvocation, factoryMethod, null);
		analyser.getReplacedStatements()
			.forEach(statement -> astRewrite.remove(statement, null));
		analyser.getNameDeclaration()
			.forEach(this::removeFragment);

		onRewrite();

		return true;
	}

	private void removeFragment(VariableDeclarationFragment nameDeclaration) {
		VariableDeclarationStatement statement = (VariableDeclarationStatement) nameDeclaration.getParent();
		int fragmentsSize = statement.fragments()
			.size();
		if (fragmentsSize > 1) {
			astRewrite.remove(nameDeclaration, null);
		} else {
			astRewrite.remove(statement, null);
		}
	}

	private boolean isNullSafe(List<Expression> elements) {
		return elements.stream()
			.allMatch(element -> ASTNode.NULL_LITERAL != element.getNodeType());
	}

	private String findExpressionTypeName(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		String identifier = name.getIdentifier();
		switch (identifier) {
		case UNMODIFIABLE_LIST:
			return java.util.List.class.getSimpleName();
		case UNMODIFIABLE_SET:
			return java.util.Set.class.getSimpleName();
		case UNMODIFIABLE_MAP:
			return java.util.Map.class.getSimpleName();
		default:
			return ""; //$NON-NLS-1$
		}
	}

	private boolean isUnmodifiableCollection(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		String identifier = name.getIdentifier();

		if (!(identifier.equals(UNMODIFIABLE_LIST) || identifier.equals(UNMODIFIABLE_SET)
				|| identifier.equals(UNMODIFIABLE_MAP))) {
			return false;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, java.util.Collections.class.getName());
	}

	@SuppressWarnings("unchecked")
	private Expression createCollectionFactoryMethod(String type, List<Expression> arguments) {
		AST ast = getASTRewrite().getAST();
		SimpleName invocationExpression = ast.newSimpleName(type);
		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setName(ast.newSimpleName("of")); //$NON-NLS-1$
		invocation.setExpression(invocationExpression);
		invocation.arguments()
			.addAll(arguments);
		return invocation;

	}
}
