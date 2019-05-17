package eu.jsparrow.rules.java10.factorymethods;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UnmodifiableArgumentAnalyser {
	private static final String ADD = "add"; //$NON-NLS-1$
	private static final String PUT = "put"; //$NON-NLS-1$
	private List<Expression> elements;
	private static final List<String> collectionTypes = unmodifiableList(Arrays.asList(java.util.List.class.getName(),
			java.util.Set.class.getName(), java.util.Map.class.getName()));

	public UnmodifiableArgumentAnalyser(Expression argument) {

		if (argument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation anonymousClass = (ClassInstanceCreation) argument;
			/*
			 * Check for: Any implementation of List/Set/Map having a static
			 * initializer consisting only of add() or put methods.
			 * 
			 * Extract a list of entries for the factory method
			 */
			analyzeAnonymousClassArgument(anonymousClass);
		} else if (argument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation argumentMethod = (MethodInvocation) argument;
			analyzeMethodInvocationArgument(argumentMethod);

		} else if (argument.getNodeType() == ASTNode.SIMPLE_NAME) {
			/*
			 * Check for: A fresh declaration of a collection followed by
			 * inserting elements. Keep track of all statements followed
			 * immediately by an assignment statement where this method
			 * invocation is the RHS
			 * 
			 * Extract the list of the inserted elements
			 */
			SimpleName name = (SimpleName) argument;
			analyzeSimpleNameArgument(name);
		}
	}

	private VariableDeclarationFragment nameDeclarationFragment = null;
	private List<ExpressionStatement> replacedStatements = new ArrayList<>();

	private void analyzeSimpleNameArgument(SimpleName name) {
		ITypeBinding type = name.resolveTypeBinding();
		if (!ClassRelationUtil.isInheritingContentOfTypes(type.getErasure(), collectionTypes) && !ClassRelationUtil.isContentOfTypes(type, collectionTypes)
				) {
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

		// find the method name to look for i.e. (add/put)
		String methodName = ClassRelationUtil.isInheritingContentOfTypes(type,
				Collections.singletonList(java.util.Map.class.getName())) || ClassRelationUtil.isContentOfTypes(type,
						Collections.singletonList(java.util.Map.class.getName())) ? PUT : ADD;
		int arity = methodName.equals(PUT) ? 2 : 1;
		boolean terminate = false;
		List<ExpressionStatement> insertStatements = new ArrayList<>();
		List<Expression> insertedElements = new ArrayList<>();
		for (int i = index - 1; i >= 0; i--) {
			Statement stm = statements.get(i);
			if (stm.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				VariableDeclarationStatement declaration = (VariableDeclarationStatement) stm;
				VariableDeclarationFragment fragment = findDeclarationFragment(declaration, name);
				if (fragment != null) {
					if (hasEmptyInitialization(fragment)) {
						nameDeclarationFragment = fragment;
						
					} else {
						terminate = true;
					}
				} else if (uses(stm, name)) {
					terminate = true;
				}
			}

			if (stm.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
				List<Expression> arguments = isInsertStatement(methodName, arity, stm, name);
				if (!arguments.isEmpty()) {
					insertStatements.add((ExpressionStatement) stm);
					insertedElements.addAll(0, arguments);
				} else if (uses(stm, name)) {

					terminate = true;
				}
			}

			if (terminate) {
				break;
			}
		}

		for (int i = index + 1; i < statements.size(); i++) {
			Statement stm = statements.get(i);
			if(uses(stm, name)) {
				terminate = true;
				break;
			}
		}
		
		if (terminate || nameDeclarationFragment == null) {
			return;
		}

		elements = insertedElements;
		replacedStatements = insertStatements;
	}

	private VariableDeclarationFragment findDeclarationFragment(VariableDeclarationStatement declaration,
			SimpleName name) {
		String identifier = name.getIdentifier();
		return convertToTypedList(declaration.fragments(), VariableDeclarationFragment.class).stream()
			.filter(f -> identifier.equals(f.getName()
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

	private boolean uses(Statement statement, SimpleName name) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(name);
		statement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}

	private void analyzeAnonymousClassArgument(ClassInstanceCreation classInstanceCreation) {
		Type type = classInstanceCreation.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		if (!ClassRelationUtil.isInheritingContentOfTypes(typeBinding, collectionTypes)) {
			return;
		}
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		List<BodyDeclaration> bodyDeclarations = convertToTypedList(anonymousClassDeclaration.bodyDeclarations(),
				BodyDeclaration.class);
		if (bodyDeclarations.size() != 1) {
			return;
		}
		BodyDeclaration declaration = bodyDeclarations.get(0);
		if (declaration.getNodeType() != ASTNode.INITIALIZER) {
			return;
		}

		Initializer initializer = (Initializer) declaration;
		Block body = initializer.getBody();
		List<Statement> bodyStatements = convertToTypedList(body.statements(), Statement.class);

		if (!collectInsertedElements(ADD, 1, bodyStatements)) {
			collectInsertedElements(PUT, 2, bodyStatements);
		}
	}

	private boolean collectInsertedElements(String name, int arity, List<Statement> bodyStatements) {
		List<Expression> collectedElements = new ArrayList<>();
		for (Statement statement : bodyStatements) {
			List<Expression> methodArgments = isInsertStatement(name, arity, statement);
			if (methodArgments.isEmpty()) {
				return false;
			}
			collectedElements.addAll(methodArgments);

		}
		elements = collectedElements;
		return true;
	}

	private List<Expression> isInsertStatement(String name, int arity, Statement statement) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Collections.emptyList();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Collections.emptyList();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		Expression methodExpression = methodInvocation.getExpression();
		List<Expression> methodArgments = convertToTypedList(methodInvocation.arguments(), Expression.class);
		int methodArity = methodArgments.size();
		if (methodExpression != null || !name.equals(methodName.getIdentifier()) || arity != methodArity) {
			return Collections.emptyList();
		}
		return methodArgments;
	}
	
	private List<Expression> isInsertStatement(String expectedNamename, int arity, Statement statement, SimpleName methodInvocationExpression) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Collections.emptyList();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Collections.emptyList();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		Expression methodExpression = methodInvocation.getExpression();
		if(methodExpression == null || methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Collections.emptyList();
		}
		SimpleName methodExpressionName = (SimpleName) methodExpression;
		if(!methodExpressionName.getIdentifier().equals(methodInvocationExpression.getIdentifier())) {
			return Collections.emptyList();
		}
		List<Expression> methodArgments = convertToTypedList(methodInvocation.arguments(), Expression.class);
		int methodArity = methodArgments.size();
		if (!expectedNamename.equals(methodName.getIdentifier()) || arity != methodArity) {
			return Collections.emptyList();
		}
		return methodArgments;
	}

	private void analyzeMethodInvocationArgument(MethodInvocation argumentMethod) {
		/*
		 * Check for: Arrays.asList(...)
		 * 
		 * Extract a list of entries for the factory method
		 */
		IMethodBinding methodBinding = argumentMethod.resolveMethodBinding();

		if ("asList".equals(methodBinding.getName()) && ClassRelationUtil //$NON-NLS-1$
			.isContentOfType(methodBinding.getDeclaringClass(), java.util.Arrays.class.getName())) {
			elements = convertToTypedList(argumentMethod.arguments(), Expression.class);
		}
	}

	public List<Expression> getElements() {
		return elements;
	}

	public VariableDeclarationFragment getNameDeclaration() {
		return this.nameDeclarationFragment;
	}

	public List<ExpressionStatement> getReplacedStatements() {
		return this.replacedStatements;
	}

}
