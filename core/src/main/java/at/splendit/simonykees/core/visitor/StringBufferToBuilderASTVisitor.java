package at.splendit.simonykees.core.visitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String JAVA_LANG_STRINGBUFFER = java.lang.StringBuffer.class.getName();
	private static final String JAVA_LANG_STRINGBUILDER = java.lang.StringBuilder.class.getName();
	private static final String JAVA_LANG_STRINGBUILDER_SIMPLENAME = java.lang.StringBuilder.class.getSimpleName();

	private static final List<String> STRINGBUFFER_TYPE_LIST = Collections.singletonList(JAVA_LANG_STRINGBUFFER);
	private static final List<String> STRINGBUILDER_TYPE_LIST = Collections.singletonList(JAVA_LANG_STRINGBUILDER);

	private List<VariableDeclarationStatement> stringBufferDeclarations = new LinkedList<>();
	private List<Assignment> stringBufferAssignmetns = new LinkedList<>();
	private ReturnStatement stringBufferReturnStatement = null;

	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatementNode) {
		ITypeBinding declarationTypeBinding = variableDeclarationStatementNode.getType().resolveBinding();
		if (ClassRelationUtil.isContentOfTypes(declarationTypeBinding, STRINGBUFFER_TYPE_LIST)) {
			stringBufferDeclarations.add(variableDeclarationStatementNode);
		}

		return false;
	}

	@Override
	public boolean visit(Assignment assignmentNode) {
		if (Assignment.Operator.ASSIGN == assignmentNode.getOperator()) {
			Expression leftExpression = assignmentNode.getLeftHandSide();
			ITypeBinding leftExpressionTypeBinding = leftExpression.resolveTypeBinding();
			if (ClassRelationUtil.isContentOfTypes(leftExpressionTypeBinding, STRINGBUFFER_TYPE_LIST)) {
				stringBufferAssignmetns.add(assignmentNode);
			}
		}

		return false;
	}

	@Override
	public boolean visit(ReturnStatement returnStatementNode) {
		Expression expression = returnStatementNode.getExpression();
		if (expression != null) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, STRINGBUFFER_TYPE_LIST)) {
				stringBufferReturnStatement = returnStatementNode;
			}
		}

		return false;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		for (VariableDeclarationStatement declaration : stringBufferDeclarations) {
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(declaration.fragments(),
					VariableDeclarationFragment.class);
			List<String> declarationFragmentNames = fragments.stream()
					.map(fragment -> fragment.getName().getIdentifier()).collect(Collectors.toList());

			String returnStatementName = getReturnStatementExpressionSimpleName(stringBufferReturnStatement);
			if (!declarationFragmentNames.contains(returnStatementName)) {

				List<Assignment> validAssignments = getValidAssignments(declarationFragmentNames);

				if (validAssignments != null) {

					if (isFragmentsValid(fragments)) {

						for (Assignment assignment : validAssignments) {
							if (ASTNode.CLASS_INSTANCE_CREATION == assignment.getRightHandSide().getNodeType()) {
								ClassInstanceCreation creation = (ClassInstanceCreation) assignment.getRightHandSide();
								ClassInstanceCreation newCreation = createClassInstanceCreation(creation);
								astRewrite.replace(creation, newCreation, null);
							}
						}

						VariableDeclarationStatement newDeclaration = createVariableDeclarationStatement(fragments);

						astRewrite.replace(declaration, newDeclaration, null);
					}
				}
			}
		}

		stringBufferAssignmetns.clear();
		stringBufferDeclarations.clear();
		stringBufferReturnStatement = null;
	}

	private VariableDeclarationStatement createVariableDeclarationStatement(
			List<VariableDeclarationFragment> fragments) {
		VariableDeclarationStatement newDeclaration = null;
		VariableDeclarationFragment firstFragment = fragments.get(0);

		VariableDeclarationFragment firstFragmentCopy = createStringBuilderFragmentCopy(firstFragment);

		if (firstFragmentCopy != null) {
			newDeclaration = astRewrite.getAST().newVariableDeclarationStatement(firstFragmentCopy);

			SimpleName stringBuilderTypeName = astRewrite.getAST().newSimpleName(JAVA_LANG_STRINGBUILDER_SIMPLENAME);
			SimpleType stringBuilderType = astRewrite.getAST().newSimpleType(stringBuilderTypeName);
			newDeclaration.setType(stringBuilderType);

			ListRewrite newDeclarationFragments = astRewrite.getListRewrite(newDeclaration,
					VariableDeclarationStatement.FRAGMENTS_PROPERTY);
			for (int i = 1; i < fragments.size(); i++) {
				VariableDeclarationFragment fragment = fragments.get(i);
				
				VariableDeclarationFragment newFragmentCopy = createStringBuilderFragmentCopy(fragment);
				if(newFragmentCopy != null) {
					newDeclarationFragments.insertLast(newFragmentCopy, null);
				}
			}
		}
		
		return newDeclaration;
	}

	private VariableDeclarationFragment createStringBuilderFragmentCopy(VariableDeclarationFragment fragment) {
		VariableDeclarationFragment fragmentCopy = null;
		
		Expression initializer = fragment.getInitializer();
		if (initializer != null) {
			ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
			if (ASTNode.CLASS_INSTANCE_CREATION == initializer.getNodeType()) {
				ClassInstanceCreation creation = (ClassInstanceCreation) initializer;
				ClassInstanceCreation newCreation = createClassInstanceCreation(creation);
				astRewrite.replace(creation, newCreation, null);
				fragmentCopy = (VariableDeclarationFragment) astRewrite.createCopyTarget(fragment);
			} else if (ASTNode.NULL_LITERAL == initializer.getNodeType()
					|| ClassRelationUtil.isContentOfTypes(initializerTypeBinding, STRINGBUILDER_TYPE_LIST)) {
				fragmentCopy = (VariableDeclarationFragment) astRewrite.createCopyTarget(fragment);
			}
		} else {
			fragmentCopy = (VariableDeclarationFragment) astRewrite.createCopyTarget(fragment);
		}
		
		return fragmentCopy;
	}
	
	private String getReturnStatementExpressionSimpleName(ReturnStatement returnStatement) {
		if (returnStatement != null) {
			Expression expression = returnStatement.getExpression();

			if (expression != null) {
				if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
					SimpleName simpleName = (SimpleName) expression;
					return simpleName.getIdentifier();
				}
			}
		}

		return null;
	}

	private List<Assignment> getValidAssignments(List<String> declarationFragmentNames) {
		List<Assignment> validAssignments = new LinkedList<>();
		int totalAssignmentCount = 0;
		for (Assignment assignment : stringBufferAssignmetns) {
			if (ASTNode.SIMPLE_NAME == assignment.getLeftHandSide().getNodeType()) {
				if (declarationFragmentNames.contains(((SimpleName) assignment.getLeftHandSide()).getIdentifier())) {
					totalAssignmentCount++;
					if (ASTNode.CLASS_INSTANCE_CREATION == assignment.getRightHandSide().getNodeType()) {
						validAssignments.add(assignment);
					} else {
						ITypeBinding expressionTypeBinding = assignment.getRightHandSide().resolveTypeBinding();
						if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, STRINGBUILDER_TYPE_LIST)) {
							validAssignments.add(assignment);
						} else {
							break;
						}
					}
				}
			}
		}

		if (validAssignments.size() == totalAssignmentCount) {
			return validAssignments;
		}

		return null;
	}

	private boolean isFragmentsValid(List<VariableDeclarationFragment> fragments) {
		List<VariableDeclarationFragment> validFragments = new LinkedList<>();
		for (VariableDeclarationFragment fragment : fragments) {
			Expression initializer = fragment.getInitializer();
			if (initializer != null) {
				if (ASTNode.CLASS_INSTANCE_CREATION == initializer.getNodeType()
						|| ASTNode.NULL_LITERAL == initializer.getNodeType()) {
					validFragments.add(fragment);
				} else {
					ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
					if (ClassRelationUtil.isContentOfTypes(initializerTypeBinding, STRINGBUILDER_TYPE_LIST)) {
						validFragments.add(fragment);
					}
				}
			} else {
				validFragments.add(fragment);
			}
		}

		return validFragments.size() == fragments.size();
	}

	private ClassInstanceCreation createClassInstanceCreation(ClassInstanceCreation oldCreation) {
		SimpleName stringBuilderName = astRewrite.getAST().newSimpleName(JAVA_LANG_STRINGBUILDER_SIMPLENAME);
		SimpleType stringBuilderType = astRewrite.getAST().newSimpleType(stringBuilderName);

		ClassInstanceCreation newCreation = astRewrite.getAST().newClassInstanceCreation();
		newCreation.setType(stringBuilderType);

		if (oldCreation.arguments().size() > 0) {
			ListRewrite newCreationArguments = astRewrite.getListRewrite(newCreation,
					ClassInstanceCreation.ARGUMENTS_PROPERTY);
			for (Object argument : oldCreation.arguments()) {
				newCreationArguments.insertLast(astRewrite.createCopyTarget((ASTNode) argument), null);
			}
		}

		return newCreation;
	}
}
