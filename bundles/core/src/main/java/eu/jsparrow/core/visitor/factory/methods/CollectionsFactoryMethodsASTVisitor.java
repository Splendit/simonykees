package eu.jsparrow.core.visitor.factory.methods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.CollectionsFactoryMethodsEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * A visitor for converting
 * {@link java.util.Collections#unmodifiableList(List)},
 * {@link java.util.Collections#unmodifiableSet(Set)} and
 * {@link java.util.Collections#unmodifiableMap(Map)} with factory methods for
 * collections introduced in {@code Java 9}, respectively {@code List.of},
 * {@code Set.of} and {@code Map.ofEntries}.
 * 
 * @since 3.6.0
 *
 */
public class CollectionsFactoryMethodsASTVisitor extends AbstractASTRewriteASTVisitor implements CollectionsFactoryMethodsEvent {

	private static final String UNMODIFIABLE_LIST = "unmodifiableList"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_SET = "unmodifiableSet"; //$NON-NLS-1$
	private static final String UNMODIFIABLE_MAP = "unmodifiableMap"; //$NON-NLS-1$

	private Set<String> staticImports = new HashSet<>();

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		staticImports.stream()
			.forEach(staticImport -> addStaticImport(compilationUnit, staticImport));
		staticImports.clear();
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (!isUnmodifiableCollection(methodInvocation)) {
			return true;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return true;
		}

		ArgumentAnalyser<?> analyser = analyzeArgument(arguments.get(0));
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
		String factoryMethodName = "of"; //$NON-NLS-1$
		if (expressionTypeName.equals(java.util.Map.class.getSimpleName()) && newArguments.size() > 2) {
			factoryMethodName = "ofEntries"; //$NON-NLS-1$
			newArguments = createMapOfEntriesArguments(newArguments);
			staticImports.add(java.util.Map.class.getName() + ".entry"); //$NON-NLS-1$
		}

		Expression factoryMethod = createCollectionFactoryMethod(expressionTypeName, factoryMethodName, newArguments);
		replaceWithFactoryMethod(methodInvocation, analyser, factoryMethod);
		addMarkerEvent(methodInvocation, expressionTypeName, factoryMethodName, elements);
		onRewrite();
		return true;
	}

	private void addStaticImport(CompilationUnit compilationUnit, String name) {
		ListRewrite importsRewriter = astRewrite.getListRewrite(compilationUnit, CompilationUnit.IMPORTS_PROPERTY);
		AST ast = compilationUnit.getAST();
		ImportDeclaration importDeclaration = ast.newImportDeclaration();
		importDeclaration.setStatic(true);
		importDeclaration.setName(ast.newName(name));
		importsRewriter.insertLast(importDeclaration, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Expression> createMapOfEntriesArguments(List<Expression> newArguments) {
		AST ast = astRewrite.getAST();
		List<Expression> entries = new ArrayList<>();
		for (int i = 0; i < newArguments.size(); i += 2) {
			Expression key = newArguments.get(i);
			Expression value = newArguments.get(i + 1);
			MethodInvocation entry = ast.newMethodInvocation();
			entry.setName(ast.newSimpleName("entry")); //$NON-NLS-1$
			List entryArguments = entry.arguments();
			entryArguments.add(key);
			entryArguments.add(value);
			entries.add(entry);
		}
		return entries;
	}

	private ArgumentAnalyser<?> analyzeArgument(Expression argument) {
		if (argument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation anonymousClass = (ClassInstanceCreation) argument;
			AnonymousClassArgumentAnalyser anonymousClassAnalyser = new AnonymousClassArgumentAnalyser();
			anonymousClassAnalyser.analyzeArgument(anonymousClass);
			return anonymousClassAnalyser;

		} else if (argument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation argumentMethod = (MethodInvocation) argument;
			MethodInvocationArgumentAnalyser methodInvocationanalyser = new MethodInvocationArgumentAnalyser();
			methodInvocationanalyser.analyzeArgument(argumentMethod);
			return methodInvocationanalyser;

		} else if (argument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleNameArgumentAnalyser simpleNameAnalyser = new SimpleNameArgumentAnalyser();
			SimpleName name = (SimpleName) argument;
			simpleNameAnalyser.analyzeArgument(name);
			return simpleNameAnalyser;
		}
		return null;
	}

	private void replaceWithFactoryMethod(MethodInvocation methodInvocation, ArgumentAnalyser<?> analyser,
			Expression factoryMethod) {
		if (analyser.requiresNewDeclaration()) {
			VariableDeclarationStatement newVariableDeclaration = createNewVariableDeclaration(analyser, factoryMethod);
			Statement statement = ASTNodeUtil.getSpecificAncestor(methodInvocation, Statement.class);
			astRewrite.replace(statement, newVariableDeclaration, null);
		} else {
			astRewrite.replace(methodInvocation, factoryMethod, null);
		}
		analyser.getReplacedStatements()
			.forEach(statement -> astRewrite.remove(statement, null));
		analyser.getNameDeclaration()
			.forEach(this::removeFragment);
	}

	private VariableDeclarationStatement createNewVariableDeclaration(ArgumentAnalyser<?> analyser,
			Expression factoryMethod) {
		VariableDeclarationFragment fragment = analyser.getNameDeclaration()
			.get(0);
		VariableDeclarationStatement oldDeclaration = (VariableDeclarationStatement) fragment.getParent();
		Type type = oldDeclaration.getType();
		SimpleName name = fragment.getName();
		AST ast = astRewrite.getAST();
		VariableDeclarationFragment newFragment = ast.newVariableDeclarationFragment();
		newFragment.setName((SimpleName) astRewrite.createCopyTarget(name));
		newFragment.setInitializer(factoryMethod);
		VariableDeclarationStatement newDeclaration = ast.newVariableDeclarationStatement(newFragment);
		newDeclaration.setType((Type) astRewrite.createCopyTarget(type));

		return newDeclaration;
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
	private Expression createCollectionFactoryMethod(String type, String factoryMethodName,
			List<Expression> arguments) {
		AST ast = getASTRewrite().getAST();
		SimpleName invocationExpression = ast.newSimpleName(type);
		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setName(ast.newSimpleName(factoryMethodName));
		invocation.setExpression(invocationExpression);
		invocation.arguments()
			.addAll(arguments);

		return invocation;

	}
}
