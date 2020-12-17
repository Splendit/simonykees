package eu.jsparrow.core.visitor.factory.methods;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Analyzes collection initialization of this form:
 * 
 * <pre>
 * <code>
 * {@code
 * List<String> list = new ArrayList<>();
 * list.add("1");
 * list.add("2");
 * list = Collections.unmodifiableList("1", "2");}
 * </code>
 * </pre>
 * 
 * Verifies the precondition for transforming this pattern to an initialization
 * using factory methods for collections. Saves the inserted elements.
 * 
 * @since 3.6.0
 *
 */
public class SimpleNameArgumentAnalyser extends ArgumentAnalyser<SimpleName> {

	private VariableDeclarationFragment nameDeclarationFragment = null;
	private List<ExpressionStatement> replacedStatements = new ArrayList<>();
	private boolean requiresNewDeclaration = false;

	@Override
	public void analyzeArgument(SimpleName name) {
		ITypeBinding type = name.resolveTypeBinding();
		if (!ClassRelationUtil.isInheritingContentOfTypes(type.getErasure(), collectionTypes)
				&& !ClassRelationUtil.isContentOfTypes(type, collectionTypes)) {
			return;
		}
		Statement unmodifiableCollectionStatement = ASTNodeUtil.getSpecificAncestor(name, Statement.class);
		if (unmodifiableCollectionStatement == null) {
			return;
		}

		ASTNode wrapper = unmodifiableCollectionStatement.getParent();
		if (wrapper.getNodeType() != ASTNode.BLOCK) {
			return;
		}

		Block block = (Block) wrapper;
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		int index = statements.indexOf(unmodifiableCollectionStatement);
		if (index == -1) {
			return;
		}

		if (uses(statements.subList(index + 1, statements.size()), name)) {
			return;
		}

		VariableDeclarationFragment fragment = findDeclarationFragment(statements.subList(0, index), name);
		if (fragment == null) {
			return;
		}
		nameDeclarationFragment = fragment;

		String methodName = findInsertMethodName(type);
		int arity = methodName.equals(PUT) ? 2 : 1;
		List<SimpleName> nonRemovable = findNonRemovableUsages(statements.subList(0, index), name, methodName, arity);
		if (!nonRemovable.isEmpty()) {
			return;
		}

		elements = findInsertedElements(statements.subList(0, index), name, methodName, arity);

		updateRequiresDeclarationFlag(unmodifiableCollectionStatement, name);
	}

	private void updateRequiresDeclarationFlag(Statement unmodifiableCollectionStatement, SimpleName name) {
		if (unmodifiableCollectionStatement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return;
		}

		ExpressionStatement expressionStatement = (ExpressionStatement) unmodifiableCollectionStatement;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.ASSIGNMENT) {
			return;
		}

		Assignment assignment = (Assignment) expression;
		Expression left = assignment.getLeftHandSide();
		if (left.getNodeType() != ASTNode.SIMPLE_NAME) {
			return;
		}

		SimpleName leftName = (SimpleName) left;
		String leftIdentifier = leftName.getIdentifier();
		this.requiresNewDeclaration = leftIdentifier.equals(name.getIdentifier());
	}

	private List<SimpleName> findNonRemovableUsages(List<Statement> statements, SimpleName name, String methodName,
			int arity) {
		List<SimpleName> allNonRemovableUsages = new ArrayList<>();
		for (Statement statement : statements) {

			LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(name);
			statement.accept(visitor);
			List<SimpleName> usages = visitor.getUsages();
			if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
				List<SimpleName> nonRemovable = usages.stream()
					.filter(usage -> usage.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY)
					.collect(Collectors.toList());
				allNonRemovableUsages.addAll(nonRemovable);
				allNonRemovableUsages.addAll(usages.stream()
					.filter(usage -> usage.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY)
					.filter(usage -> !isInsertStatement(methodName, arity, statement, usage))
					.collect(Collectors.toList()));

			} else {
				List<SimpleName> nonRemovable = usages.stream()
					.filter(usage -> usage.getLocationInParent() != VariableDeclarationFragment.NAME_PROPERTY)
					.collect(Collectors.toList());
				allNonRemovableUsages.addAll(nonRemovable);

			}
		}
		return allNonRemovableUsages;
	}

	private List<Expression> findInsertedElements(List<Statement> statements, SimpleName name, String methodName,
			int arity) {
		List<Expression> insertedElements = new ArrayList<>();
		List<ExpressionStatement> insertStatements = new ArrayList<>();
		for (Statement statement : statements) {
			if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
				ExpressionStatement expressionStatement = (ExpressionStatement) statement;
				List<Expression> arguments = findMethodArguments(methodName, arity, expressionStatement, name);
				if (!arguments.isEmpty()) {
					insertStatements.add((ExpressionStatement) statement);
					insertedElements.addAll(arguments);
				}
			}
		}
		replacedStatements = insertStatements;
		return insertedElements;
	}

	private VariableDeclarationFragment findDeclarationFragment(List<Statement> statements, SimpleName name) {
		for (Statement statement : statements) {
			if (statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				VariableDeclarationStatement declaration = (VariableDeclarationStatement) statement;
				VariableDeclarationFragment fragment = findDeclarationFragment(declaration, name);
				if (fragment != null) {
					if (hasEmptyInitialization(fragment)) {
						return fragment;
					} else {
						return null;
					}
				} else if (uses(statement, name)) {
					return null;
				}
			}
		}
		return null;
	}

	private List<Expression> findMethodArguments(String expectedMethodname, int arity,
			ExpressionStatement expressionStatement, SimpleName methodInvocationExpression) {
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Collections.emptyList();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null || methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Collections.emptyList();
		}
		SimpleName methodExpressionName = (SimpleName) methodExpression;
		if (!methodExpressionName.getIdentifier()
			.equals(methodInvocationExpression.getIdentifier())) {
			return Collections.emptyList();
		}
		List<Expression> methodArgments = convertToTypedList(methodInvocation.arguments(), Expression.class);
		int methodArity = methodArgments.size();
		if (!expectedMethodname.equals(methodName.getIdentifier()) || arity != methodArity) {
			return Collections.emptyList();
		}
		return methodArgments;
	}

	private boolean isInsertStatement(String expectedNamename, int arity, Statement statement,
			SimpleName methodInvocationExpression) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return false;
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null || methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName methodExpressionName = (SimpleName) methodExpression;
		if (!methodExpressionName.getIdentifier()
			.equals(methodInvocationExpression.getIdentifier())) {
			return false;
		}
		List<Expression> methodArgments = convertToTypedList(methodInvocation.arguments(), Expression.class);
		int methodArity = methodArgments.size();
		return expectedNamename.equals(methodName.getIdentifier()) && arity == methodArity;
	}

	private VariableDeclarationFragment findDeclarationFragment(VariableDeclarationStatement declaration,
			SimpleName name) {
		String identifier = name.getIdentifier();
		return convertToTypedList(declaration.fragments(), VariableDeclarationFragment.class).stream()
			.filter(fragment -> identifier.equals(fragment.getName()
				.getIdentifier()))
			.findFirst()
			.orElse(null);
	}

	private boolean hasEmptyInitialization(VariableDeclarationFragment fragment) {
		Expression expression = fragment.getInitializer();
		if (expression.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		boolean emptyArgumetns = classInstanceCreation.arguments()
			.isEmpty();
		boolean emptyAnonymousClass = classInstanceCreation.getAnonymousClassDeclaration() == null;
		return emptyArgumetns && emptyAnonymousClass;
	}

	private boolean uses(List<Statement> statements, SimpleName name) {
		return statements.stream()
			.anyMatch(statement -> uses(statement, name));
	}

	private boolean uses(Statement statement, SimpleName name) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(name);
		statement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}

	@Override
	public List<VariableDeclarationFragment> getNameDeclaration() {
		return Collections.singletonList(nameDeclarationFragment);
	}

	@Override
	public List<ExpressionStatement> getReplacedStatements() {
		return this.replacedStatements;
	}

	@Override
	public boolean requiresNewDeclaration() {
		return this.requiresNewDeclaration;
	}

}
