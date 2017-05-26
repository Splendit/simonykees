package at.splendit.simonykees.core.visitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachCollectASTVisitor extends AbstractAddImportASTVisitor {

	private static final String FOR_EACH_METHOD_NAME = "forEach"; //$NON-NLS-1$
	private static final String ADD_METHOD_NAME = "add"; //$NON-NLS-1$
	private static final String JAVA_UTIL_STREAM = java.util.stream.Stream.class.getName();
	private static final String JAVA_UTIL_STREAM_COLLECTORS = java.util.stream.Collectors.class.getName();
	private static final String JAVA_UTIL_STREAM_COLLECTORS_SIMPLE_NAME = java.util.stream.Collectors.class
			.getSimpleName();
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String JAVA_UTIL_ARRAY_LIST = java.util.ArrayList.class.getName();
	private static final String JAVA_UTIL_HASHSET = java.util.HashSet.class.getName();
	private static final String TO_LIST = "toList"; //$NON-NLS-1$
	private static final String COLLECT = "collect"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		// TODO: check if there is another Collectors class imported
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		SimpleName methodName = methodInvocation.getName();
		if (FOR_EACH_METHOD_NAME.equals(methodName.getIdentifier())
				&& ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding != null && ClassRelationUtil.isContentOfTypes(methodBinding.getDeclaringClass(), Collections.singletonList(JAVA_UTIL_STREAM))) {
				List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				if (arguments.size() == 1 && ASTNode.LAMBDA_EXPRESSION == arguments.get(0).getNodeType()) {

					LambdaExpression lambdaExpression = (LambdaExpression) arguments.get(0);
					SimpleName parameter = extractSingleParameter(lambdaExpression);
					MethodInvocation bodyExpression = extractSingleBodyExpression(lambdaExpression);

					if (parameter != null && bodyExpression != null && isListAddInvocation(bodyExpression)) {
						Expression collectionExpression = bodyExpression.getExpression();
						if (ASTNode.SIMPLE_NAME == collectionExpression.getNodeType()) {
							SimpleName collection = (SimpleName) collectionExpression;
							Block outerBlock = ASTNodeUtil.getSpecificAncestor(methodInvocation, Block.class);
							if (outerBlock != null) {
								VariableDeclarationFragment collectionDeclarationFragment = findEmptyCollectionInitialization(
										collection, outerBlock);

								if (collectionDeclarationFragment != null) {

									VariableDeclarationStatement targetDecl = createTargetVariableDeclaration(
											methodInvocation, collection, collectionDeclarationFragment);

									astRewrite.replace(methodInvocation.getParent(), targetDecl, null);

									removeFragmentFromAst(collectionDeclarationFragment);
								}
							}
						}
					}

				}
			}
		}

		return true;
	}

	private VariableDeclarationStatement createTargetVariableDeclaration(MethodInvocation methodInvocation,
			SimpleName collection, VariableDeclarationFragment collectionDeclarationFragment) {

		AST ast = methodInvocation.getAST();
		MethodInvocation collect = ast.newMethodInvocation();
		collect.setName(ast.newSimpleName(COLLECT));

		ListRewrite listRewirte = astRewrite.getListRewrite(collect, MethodInvocation.ARGUMENTS_PROPERTY);
		MethodInvocation collectorsToList = collect.getAST().newMethodInvocation();
		collectorsToList.setName(collect.getAST().newSimpleName(TO_LIST));
		collectorsToList.setExpression(collect.getAST().newSimpleName(JAVA_UTIL_STREAM_COLLECTORS_SIMPLE_NAME));
		listRewirte.insertFirst(collectorsToList, null);
		this.addImports.add(JAVA_UTIL_STREAM_COLLECTORS);

		collect.setExpression((Expression) astRewrite.createCopyTarget(methodInvocation.getExpression()));

		VariableDeclarationStatement targetDecl = (VariableDeclarationStatement) ASTNode.copySubtree(
				collectionDeclarationFragment.getAST(),
				(VariableDeclarationStatement) collectionDeclarationFragment.getParent());
		targetDecl.setType((Type) astRewrite.createCopyTarget(
				((VariableDeclarationStatement) collectionDeclarationFragment.getParent()).getType()));
		ASTNodeUtil.returnTypedList(targetDecl.fragments(), VariableDeclarationFragment.class).stream()
				.forEach(fragment -> {
					if (fragment.getName().getIdentifier()
							.equals(collectionDeclarationFragment.getName().getIdentifier())) {
						fragment.setInitializer(collect);
					} else {
						astRewrite.remove(fragment, null);
					}
				});

		return targetDecl;
	}

	private void removeFragmentFromAst(VariableDeclarationFragment collectionDeclarationFragment) {
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == collectionDeclarationFragment.getParent().getNodeType()) {
			VariableDeclarationStatement statement = (VariableDeclarationStatement) collectionDeclarationFragment
					.getParent();
			if (statement.fragments().size() == 1) {
				astRewrite.remove(statement, null);
			} else {
				astRewrite.remove(collectionDeclarationFragment, null);
			}
		}

	}

	private VariableDeclarationFragment findEmptyCollectionInitialization(SimpleName collection, Block block) {
		EmptyListInitializationVisitor visitor = new EmptyListInitializationVisitor(collection);
		block.accept(visitor);
		return visitor.getInitializationFragment();
	}

	private boolean isListAddInvocation(MethodInvocation bodyExpression) {
		boolean isAddInvocation = false;
		SimpleName name = bodyExpression.getName();
		if (ADD_METHOD_NAME.equals(name.getIdentifier())) {
			Expression expression = bodyExpression.getExpression();
			if (ClassRelationUtil.isContentOfTypes(expression.resolveTypeBinding(),
					Collections.singletonList(JAVA_UTIL_LIST))) {
				isAddInvocation = true;
			}
		}
		return isAddInvocation;
	}

	private MethodInvocation extractSingleBodyExpression(LambdaExpression lambdaExpression) {
		MethodInvocation methodInvocation = null;
		ASTNode body = lambdaExpression.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			Block block = (Block) body;
			List<ExpressionStatement> statements = ASTNodeUtil.returnTypedList(block.statements(),
					ExpressionStatement.class);
			if (statements.size() == 1) {
				ExpressionStatement statement = statements.get(0);
				Expression expression = statement.getExpression();
				if (ASTNode.METHOD_INVOCATION == expression.getNodeType()) {
					methodInvocation = (MethodInvocation) expression;
				}

			}
		} else if (ASTNode.METHOD_INVOCATION == body.getNodeType()) {
			methodInvocation = (MethodInvocation) body;
		}

		return methodInvocation;
	}

	private SimpleName extractSingleParameter(LambdaExpression lambdaExpression) {
		SimpleName parameter = null;
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				VariableDeclarationFragment.class);
		if (fragments.size() == 1) {
			VariableDeclarationFragment fragment = fragments.get(0);
			parameter = fragment.getName();
		} else {
			List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
					SingleVariableDeclaration.class);
			if (declarations.size() == 1) {
				SingleVariableDeclaration declaration = declarations.get(0);
				parameter = declaration.getName();
			}
		}
		return parameter;
	}

	private class EmptyListInitializationVisitor extends ASTVisitor {
		private SimpleName listName;
		private VariableDeclarationFragment emptyListInitialization;
		private boolean referenced = false;
		private boolean terminate = false;

		public EmptyListInitializationVisitor(SimpleName listName) {
			this.listName = listName;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			return !terminate && !referenced;
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			if (simpleName == listName) {
				terminate = true;
				return true;
			}

			IBinding binding = simpleName.resolveBinding();
			if (IBinding.VARIABLE == binding.getKind() && simpleName.getIdentifier().equals(listName.getIdentifier())) {
				if (VariableDeclarationFragment.NAME_PROPERTY == simpleName.getLocationInParent()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) simpleName.getParent();
					Expression initializer = fragment.getInitializer();
					if (initializer != null && ASTNode.CLASS_INSTANCE_CREATION == initializer.getNodeType()) {
						ClassInstanceCreation instanceCreation = (ClassInstanceCreation) initializer;
						IMethodBinding ctorBinding = instanceCreation.resolveConstructorBinding();
						ITypeBinding declClass = ctorBinding.getDeclaringClass();
						if (instanceCreation.arguments().isEmpty() && ClassRelationUtil.isContentOfTypes(
								declClass.getErasure(), Arrays.asList(JAVA_UTIL_ARRAY_LIST, JAVA_UTIL_HASHSET))) {
							this.emptyListInitialization = fragment;

						}
					}
				} else {
					referenced = true;
					emptyListInitialization = null;
				}
			}

			return true;
		}

		public VariableDeclarationFragment getInitializationFragment() {
			return this.emptyListInitialization;
		}
	}
}
