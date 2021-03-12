package eu.jsparrow.core.visitor.functionalinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.sub.MethodInvocationsVisitor;
import eu.jsparrow.core.visitor.sub.VariableDefinitionASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Finds anonymous classes and converts them to lambdas, if they are functional
 * interfaces.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9
 *
 */
public class FunctionalInterfaceASTVisitor extends AbstractASTRewriteASTVisitor {

	Logger log = LoggerFactory.getLogger(FunctionalInterfaceASTVisitor.class);

	private Map<String, Integer> renamings = new HashMap<>();
	private CompilationUnit compilationUnit;
	private List<SimpleName> safeToUseFields = new ArrayList<>();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		super.visit(compilationUnit);
		return true;
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		boolean isFinal = ASTNodeUtil.hasModifier(fieldDeclaration.modifiers(), Modifier::isFinal);
		safeToUseFields
			.addAll(ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class)
				.stream()
				.filter(fragment -> !isFinal || fragment.getInitializer() != null)
				.map(VariableDeclarationFragment::getName)
				.collect(Collectors.toList()));
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		ITypeBinding binding = typeDeclaration.resolveBinding();
		if (binding != null && binding.isTopLevel()) {
			safeToUseFields.clear();
		}
	}

	private Name extractName(Type type) {

		if (type.isParameterizedType()) {
			return extractName(((ParameterizedType) type).getType());
		}
		if (type.isSimpleType()) {
			return (Name) astRewrite.createCopyTarget(((SimpleType) type).getName());
		}
		if (type.isNameQualifiedType()) {
			AST ast = astRewrite.getAST();
			NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
			Name qualifierClone = (Name) astRewrite.createCopyTarget(nameQualifiedType.getQualifier());
			SimpleName simpleNameClone = ast.newSimpleName(nameQualifiedType.getName()
				.getIdentifier());
			return ast.newQualifiedName(qualifierClone, simpleNameClone);
		}
		return null;
	}

	private boolean canExtractName(Type type) {

		if (type.isParameterizedType()) {
			return canExtractName(((ParameterizedType) type).getType());
		}
		return type.isSimpleType() || type.isNameQualifiedType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (ASTNode.CLASS_INSTANCE_CREATION == node.getParent()
			.getNodeType()) {
			ClassInstanceCreation parentNode = (ClassInstanceCreation) node.getParent();
			Type classType = parentNode.getType();

			if (ASTNode.PARAMETERIZED_TYPE == classType.getNodeType()) {
				ASTNode gParent = parentNode.getParent();
				if (gParent != null && ASTNode.METHOD_INVOCATION == gParent.getNodeType()) {
					MethodInvocation methodInvocation = (MethodInvocation) gParent;
					IMethodBinding miBinding = methodInvocation.resolveMethodBinding();
					if (miBinding == null || Modifier.isStatic(miBinding.getModifiers())) {
						/*
						 * If the anonymous class is parameterized and is
						 * occurring as a parameter of a static method
						 * invocation then, then it is not safe to replace it
						 * with a lambda expression.
						 */
						return true;
					}
				}
			}

			/*
			 * Check if the consuming part is the same type (assignment to the
			 * same type, method parameter is the same type)
			 */
			boolean allowedType = ClassRelationUtil
				.compareITypeBinding(ASTNodeUtil.getTypeBindingOfNodeUsage(parentNode), classType.resolveBinding());

			if (allowedType) {
				ITypeBinding parentNodeTypeBinding = parentNode.getType()
					.resolveBinding();
				if (parentNodeTypeBinding != null) {

					/*
					 * Get the Body of the functional interface method
					 * implementation
					 */
					MethodDeclaration onlyFunctionalInterfaceMethod = getOnlyFunctionalInterfaceMethodImpl(node,
							parentNodeTypeBinding);
					if (onlyFunctionalInterfaceMethod == null) {
						return false;
					}
					if (hasInvocationsOfInstanceMethods(node, onlyFunctionalInterfaceMethod)) {
						return true;
					}
					Block onlyFunctionalInterfaceMethodImplBody = onlyFunctionalInterfaceMethod.getBody();
					if (hasOccurrencesOfThisKeyWord(onlyFunctionalInterfaceMethod)) {
						return true;
					}
					// find parent scope and variable declarations in it
					List<ASTNode> relevantBlocks = new ArrayList<>();
					ASTNode scope = findScope(node, relevantBlocks);
					if (scope == null) {
						return true;
					}

					LambdaExpression newInitializer = node.getAST()
						.newLambdaExpression();
					MethodBlockASTVisitor methodBlockASTVisitor = new MethodBlockASTVisitor();
					methodBlockASTVisitor.setASTRewrite(astRewrite);
					node.accept(methodBlockASTVisitor);
					Block moveBlock = methodBlockASTVisitor.getMethodBlock();

					if (moveBlock == null || ASTNodeUtil.containsWildCards(moveBlock)) {
						return true;
					}

					if (moveBlock != null && isCommentFree(node, moveBlock)) {

						// find variable declarations inside the method block
						BlockVariableDeclarationsVisitor varDeclarationVisitor = new BlockVariableDeclarationsVisitor();
						moveBlock.accept(varDeclarationVisitor);
						List<SimpleName> blockLocalVarNames = varDeclarationVisitor.getBlockVariableDeclarations();

						int scopeNodeType = scope.getNodeType();

						/*
						 * check if the scope is static for methods and
						 * initializer
						 * 
						 * lambdas are static resolved, anonymous classes not.
						 * this changes the behavior of use-able methods within
						 * static functions
						 */

						@SuppressWarnings("rawtypes")
						List modifiers = null;
						if (ASTNode.INITIALIZER == scopeNodeType) {
							modifiers = ((Initializer) scope).modifiers();
						} else if (ASTNode.METHOD_DECLARATION == scopeNodeType) {
							modifiers = ((MethodDeclaration) scope).modifiers();
						}

						if (modifiers != null && ASTNodeUtil.hasModifier(modifiers, Modifier::isStatic)) {
							CheckNativeMethodInvocationVisitor visitor = new CheckNativeMethodInvocationVisitor();
							node.accept(visitor);
							if (visitor.objectMethodDeclarationInvocated()) {
								return true;
							}
						}

						if (ASTNode.TYPE_DECLARATION == scopeNodeType || ASTNode.ENUM_DECLARATION == scopeNodeType) {
							/*
							 * the anonymous class is occurring as an
							 * initializer of a field
							 */

							PublicVariableReferencesVisitor fieldReferencesVisitor = new PublicVariableReferencesVisitor();
							node.accept(fieldReferencesVisitor);
							List<SimpleName> unAssignedReferences = fieldReferencesVisitor
								.getUnassignedVariableReferences();

							boolean isNotSafeReference = unAssignedReferences.stream()
								.anyMatch(fieldReference -> !isSafeFieldReference(fieldReference));
							if (isNotSafeReference) {
								return true;
							}
						} else if (ASTNode.METHOD_DECLARATION == scopeNodeType) {
							MethodDeclaration methodDeclaration = (MethodDeclaration) scope;
							/*
							 * Check if the anonymous class is occurring in the
							 * body of a constructor.
							 */
							if (methodDeclaration.isConstructor()) {

								/*
								 * List of variables that are assigned before
								 * the occurrence of the anonymous class.
								 */
								List<SimpleName> assignedVariables = findAssignedVariablesTillNodeOccurrence(
										methodDeclaration, relevantBlocks, node);
								PublicVariableReferencesVisitor fieldReferencesVisitor = new PublicVariableReferencesVisitor();
								node.accept(fieldReferencesVisitor);
								/*
								 * List of public variables in the body of the
								 * anonymous class that are used before being
								 * assigned.
								 */
								List<SimpleName> unnassignedReferences = fieldReferencesVisitor
									.getUnassignedVariableReferences();
								/*
								 * not safe if any of the unassigned references
								 * doesn't match
								 */
								boolean unsafe = unnassignedReferences.stream()
									.map(SimpleName::getIdentifier)
									.anyMatch(name -> assignedVariables.stream()
										.map(SimpleName::getIdentifier)
										.noneMatch(name::equals));
								if (unsafe) {
									return true;
								}

							}
						}

						if (!canExtractName(classType)) {
							return true;
						}

						qualifyUnqualifiedConstants(node, classType, onlyFunctionalInterfaceMethodImplBody);

						VariableDefinitionASTVisitor varVisistor = new VariableDefinitionASTVisitor(node,
								relevantBlocks);
						scope.accept(varVisistor);
						List<SimpleName> scopeNames = varVisistor.getScopeVariableNames();

						List<SimpleName> redeclaredNames = checkForClashingLocalVariables(scopeNames,
								blockLocalVarNames);
						renameLocalVariables(node, scopeNames, redeclaredNames);

						List<SingleVariableDeclaration> parameteres = methodBlockASTVisitor.getParameters();

						if (parameteres != null) {
							if (ASTNode.TYPE_DECLARATION != scope.getNodeType()) {
								/*
								 * if the scope is the whole class, no need to
								 * do any renaming...
								 */

								List<SimpleName> conflictingNames = findConflictingNames(parameteres, scopeNames);

								// renaming the clashing variable names
								if (!conflictingNames.isEmpty()) {
									renameLocalVariables(node, scopeNames, conflictingNames);
								}
							}

							parameteres.forEach(s -> newInitializer.parameters()
								.add(astRewrite.createMoveTarget(s)));
						}

						newInitializer.setBody(astRewrite.createMoveTarget(moveBlock));
						getASTRewrite().replace(parentNode, newInitializer, null);
						onRewrite();
					}
				}
			}
		}
		return true;

	}

	private boolean hasOccurrencesOfThisKeyWord(MethodDeclaration methodDeclaration) {
		ThisExpressionVisitor visitor = new ThisExpressionVisitor();
		methodDeclaration.accept(visitor);
		return visitor.hasThisExpression();
	}

	private boolean hasInvocationsOfInstanceMethods(AnonymousClassDeclaration node,
			MethodDeclaration onlyFunctionalInterfaceMethod) {
		Block onlyFunctionalInterfaceMethodImplBody = onlyFunctionalInterfaceMethod.getBody();
		DefaultMethodInvocationVisitor defaultMethodInvocationASTVisitor = new DefaultMethodInvocationVisitor(
				node);
		onlyFunctionalInterfaceMethodImplBody.accept(defaultMethodInvocationASTVisitor);
		if (defaultMethodInvocationASTVisitor.isFlagCancelTransformation()) {
			return true;
		}

		return hasRecursiveCalls(onlyFunctionalInterfaceMethod);
	}

	private boolean hasRecursiveCalls(MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		Block body = methodDeclaration.getBody();
		MethodInvocationsVisitor visitor = new MethodInvocationsVisitor(methodBinding);
		body.accept(visitor);
		boolean noRecursiveCalls = visitor.getMethodInvocations()
			.isEmpty();
		return !noRecursiveCalls;
	}

	private void qualifyUnqualifiedConstants(AnonymousClassDeclaration node, Type type,
			Block onlyFunctionalInterfaceMethodImplBody) {
		UnqualifiedFieldNamesVisitor unqualifiedConstantNamesVisitor = new UnqualifiedFieldNamesVisitor(node);
		onlyFunctionalInterfaceMethodImplBody.accept(unqualifiedConstantNamesVisitor);

		AST ast = node.getAST();

		unqualifiedConstantNamesVisitor.getSimpleNames()
			.forEach(simpleName -> {
				Name qualifierClone = extractName(type);
				SimpleName simpleNameClone = ast.newSimpleName(simpleName.getIdentifier());
				QualifiedName qualifiedName = ast.newQualifiedName(qualifierClone, simpleNameClone);
				astRewrite.replace(simpleName, qualifiedName, null);
			});

		unqualifiedConstantNamesVisitor.getThisExpressionsOfStaticFields()
			.forEach(thisExpression -> {
				Name qualifierClone = extractName(type);
				astRewrite.replace(thisExpression, qualifierClone, null);
			});
	}

	/**
	 * Finds the list of the variables that are safe to use in the body of a
	 * lambda expression before the occurrence of the given anonymous class. In
	 * particular, it collects:
	 * <ul>
	 * <li>the list of fields which are either non final or are initialized</li>
	 * <li>list of field that are initialized in the body of the method
	 * declaration before the occurrence of the anonymous class</li>
	 * <li>list of the parameters of the given method declaration</li>
	 * </ul>
	 * 
	 * @param methodDeclaration
	 *            a method declaration containing the occurrence of the
	 *            anonymous class
	 * @param node
	 *            a node representing an anonymous class
	 * @return list of safe-to-use variables if the anonymous class would be
	 *         converted to a lambda expression.
	 */
	private List<SimpleName> findAssignedVariablesTillNodeOccurrence(MethodDeclaration methodDeclaration,
			List<ASTNode> relevantBlocks, AnonymousClassDeclaration node) {

		List<FieldDeclaration> fields = new ArrayList<>();
		ASTNode parent = methodDeclaration.getParent();
		if (parent.getNodeType() == ASTNode.TYPE_DECLARATION) {
			fields = Arrays.asList(((TypeDeclaration) parent).getFields());
		} else if (parent.getNodeType() == ASTNode.ENUM_DECLARATION) {
			fields = ASTNodeUtil.convertToTypedList(((EnumDeclaration) parent).bodyDeclarations(),
					FieldDeclaration.class);
		}

		List<SimpleName> initializedFields = new ArrayList<>();

		fields.forEach(field -> {
			boolean isFinal = ASTNodeUtil.hasModifier(field.modifiers(), Modifier::isFinal);
			initializedFields
				.addAll(ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class)
					.stream()
					.filter(fragment -> !isFinal || fragment.getInitializer() != null)
					.map(VariableDeclarationFragment::getName)
					.collect(Collectors.toList()));
		});

		initializedFields
			.addAll(ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(), SingleVariableDeclaration.class)
				.stream()
				.map(SingleVariableDeclaration::getName)
				.collect(Collectors.toList()));

		List<Statement> statements = ASTNodeUtil.convertToTypedList(relevantBlocks, Block.class)
			.stream()
			.flatMap(block -> ASTNodeUtil.convertToTypedList(block.statements(), Statement.class)
				.stream())
			.collect(Collectors.toList());
		for (Statement statement : statements) {
			AnonymousClassNodeWrapperVisitor visitor = new AnonymousClassNodeWrapperVisitor(node);
			statement.accept(visitor);
			if (visitor.isAncestor()) {
				break;
			}

			if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
				ExpressionStatement expressionStatement = (ExpressionStatement) statement;
				Expression expression = expressionStatement.getExpression();
				if (ASTNode.ASSIGNMENT == expression.getNodeType()
						&& ASTNode.SIMPLE_NAME == ((Assignment) expression).getLeftHandSide()
							.getNodeType()) {
					SimpleName simpleName = (SimpleName) ((Assignment) expression).getLeftHandSide();
					initializedFields.add(simpleName);
				}
			} else if (ASTNode.VARIABLE_DECLARATION_STATEMENT == statement.getNodeType()) {
				initializedFields.addAll(ASTNodeUtil
					.convertToTypedList(((VariableDeclarationStatement) statement).fragments(),
							VariableDeclarationFragment.class)
					.stream()
					.filter(fragment -> fragment.getInitializer() != null)
					.map(VariableDeclarationFragment::getName)
					.collect(Collectors.toList()));

			}

		}

		return initializedFields;
	}

	/**
	 * Makes use of {@link #safeToUseFields} to check if reference represented
	 * by the given simple name, can be used in the body of a lambda expression
	 * which is used as an initializer in a field declaration.
	 * 
	 * @param fieldReference
	 *            a {@link SimpleName} representing a reference of a field.
	 * 
	 * @return {@code true} if the reference can be used in the body of a lambda
	 *         expression serving as initializer in a field declaration or
	 *         {@code false} otherwise.
	 */
	private boolean isSafeFieldReference(SimpleName fieldReference) {
		String referenceIdentifier = fieldReference.getIdentifier();
		return safeToUseFields.stream()
			.map(SimpleName::getIdentifier)
			.anyMatch(referenceIdentifier::equals);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getParent() != null && ASTNode.TYPE_DECLARATION == node.getParent()
			.getNodeType()) {
			renamings.clear();
		}
		// FIXME SIM-335: it is better to detected the uninitialized fields that
		// are referenced in the body.
		// boolean isConstructor = node.isConstructor()

		return true;
	}

	private List<SimpleName> checkForClashingLocalVariables(List<SimpleName> scopeNames,
			List<SimpleName> blockLocalVarNames) {

		List<String> parentScopeNames = scopeNames.stream()
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		return blockLocalVarNames.stream()
			.filter(simpleName -> parentScopeNames.contains(simpleName.getIdentifier()))
			.collect(Collectors.toList());
	}

	private MethodDeclaration getOnlyFunctionalInterfaceMethodImpl(AnonymousClassDeclaration node,
			ITypeBinding parentNodeTypeBinding) {
		if (node == null) {
			return null;
		}

		if (parentNodeTypeBinding == null) {
			return null;
		}

		IMethodBinding functionalInterfaceMethod = parentNodeTypeBinding.getFunctionalInterfaceMethod();
		if (functionalInterfaceMethod == null) {
			return null;
		}

		if (functionalInterfaceMethod.isGenericMethod()) {
			return null;
		}

		if (node.bodyDeclarations() == null) {
			return null;
		}

		if (node.bodyDeclarations()
			.size() != 1) {
			return null;
		}

		if (node.bodyDeclarations()
			.get(0) instanceof MethodDeclaration) {

			MethodDeclaration methodDeclaration = (MethodDeclaration) node.bodyDeclarations()
				.get(0);
			String functionalInterfaceMethodName = functionalInterfaceMethod.getName();

			if (StringUtils.equals(functionalInterfaceMethodName, methodDeclaration.getName()
				.getIdentifier())) {
				return methodDeclaration;
			}
		}

		return null;
	}

	/**
	 * Renames all occurrences of variables with conflicting names in the given
	 * anonymous class node, so that they will not clash with the existing local
	 * variables within the scope.
	 * 
	 * @param node
	 *            node representing the anonymous class declaration.
	 * @param scopeNames
	 *            variables names in the scope of the declaration the anonymous
	 *            class.
	 * @param conflictingNames
	 *            list of variable names conflicting with the existing local
	 *            variables.
	 */
	private void renameLocalVariables(AnonymousClassDeclaration node, List<SimpleName> scopeNames,
			List<SimpleName> conflictingNames) {
		LocalVariableUsagesVisitor visitor;
		for (SimpleName conflictingName : conflictingNames) {
			visitor = new LocalVariableUsagesVisitor(conflictingName);
			node.accept(visitor);
			String newName = calcNewName(scopeNames, conflictingName);
			List<SimpleName> usages = visitor.getUsages();
			usages.forEach(usage -> astRewrite.set(usage, SimpleName.IDENTIFIER_PROPERTY, newName, null));
		}
	}

	/**
	 * Computes a new variable name by appending a suffix to the existing name,
	 * so that it will not clash with any of the variable within the scope.
	 * 
	 * @param scopeVariableNames
	 *            names of the local variables within the scope.
	 * @param simpleName
	 *            current name to be changed.
	 * 
	 * @return new safe name for the variable.
	 */
	private String calcNewName(List<SimpleName> scopeVariableNames, SimpleName simpleName) {
		int suffix = 1;
		List<String> identifiers = scopeVariableNames.stream()
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		String newName;
		String currentName = simpleName.getIdentifier();
		if (renamings.containsKey(currentName)) {
			suffix = renamings.get(currentName) + 1;
		}

		boolean inalidNewName = true;
		do {
			newName = currentName + Integer.toString(suffix);
			if (!identifiers.contains(newName)) {
				inalidNewName = false;
				// keep track of the introduced new names
				renamings.put(currentName, suffix);
			}
			suffix++;
		} while (inalidNewName);

		return newName;
	}

	/**
	 * Finds the scope where the anonymous class declaration occurs. In addition
	 * it adds all its parent blocks to the {@code relevantBlocks} list.
	 */
	private ASTNode findScope(AnonymousClassDeclaration node, List<ASTNode> relevantBlocks) {
		ASTNode scope = null;
		if (node != null && relevantBlocks != null) {
			relevantBlocks.add(node);
			scope = node;
			boolean stopCondition = true;
			do {
				scope = scope.getParent();
				int scopeNodeType = scope.getNodeType();
				if (ASTNode.BLOCK == scopeNodeType || ASTNode.FOR_STATEMENT == scopeNodeType
						|| ASTNode.METHOD_DECLARATION == scopeNodeType || ASTNode.LAMBDA_EXPRESSION == scopeNodeType) {
					relevantBlocks.add(scope);
				}

				stopCondition = scope == null
						|| (scope.getNodeType() == ASTNode.METHOD_DECLARATION && scope.getParent() != null
								&& ASTNode.ANONYMOUS_CLASS_DECLARATION != scope.getParent()
									.getNodeType())
						|| scope.getNodeType() == ASTNode.TYPE_DECLARATION
						|| scope.getNodeType() == ASTNode.INITIALIZER;
			} while (!stopCondition);
		}
		return scope;
	}

	/**
	 * Filters the names of the given {@code scopeVariableNames} that occur also
	 * in the given {@code parameters}
	 */
	private List<SimpleName> findConflictingNames(List<SingleVariableDeclaration> parameters,
			List<SimpleName> scopeVariableNames) {

		List<String> varNames = scopeVariableNames.stream()
			.map(SimpleName::getIdentifier)
			.distinct()
			.collect(Collectors.toList());

		return parameters.stream()
			.map(SingleVariableDeclaration::getName)
			.filter(parameter -> varNames.contains(parameter.getIdentifier()))
			.collect(Collectors.toList());
	}

	private boolean isCommentFree(AnonymousClassDeclaration node, Block moveBlock) {
		boolean commentFree = false;
		if (compilationUnit != null) {
			int nodeStartPos = node.getStartPosition();
			int nodeLength = node.getLength();
			int nodeLastPos = nodeStartPos + nodeLength;
			int blockStartPos = moveBlock.getStartPosition();
			int blockEndPos = moveBlock.getStartPosition() + moveBlock.getLength();

			List<Comment> allComments = ASTNodeUtil.returnTypedList(compilationUnit.getCommentList(), Comment.class);

			boolean hasComments = allComments.stream()
				.anyMatch(comment -> {
					int commentStartPos = comment.getStartPosition();
					int commentLastPOs = commentStartPos + comment.getLength();
					return (commentStartPos > nodeStartPos && commentLastPOs < blockStartPos)
							|| (commentStartPos > blockEndPos && commentLastPOs < nodeLastPos);
				});

			commentFree = !hasComments;
		}

		return commentFree;
	}
}
