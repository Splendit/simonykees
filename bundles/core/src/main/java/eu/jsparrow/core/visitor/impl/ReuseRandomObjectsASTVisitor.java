package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ReuseRandomObjectsASTVisitor extends AbstractASTRewriteASTVisitor {

	private final LiveVariableScope liveVariableScope = new LiveVariableScope();

	@Override
	public boolean visit(VariableDeclarationStatement statement) {
		Type type = statement.getType();
		ITypeBinding variableTypeBinding = type.resolveBinding();
		if (!ClassRelationUtil.isContentOfType(variableTypeBinding, java.util.Random.class.getName())) {
			return false;
		}

		List<VariableDeclarationFragment> fragments = extractFragmentsWithInitializer(statement);
		ASTNode scope = liveVariableScope.findEnclosingScope(statement)
			.orElse(null);
		if (scope == null) {
			return false;
		}

		liveVariableScope.lazyLoadScopeNames(scope);

		List<String> currentFieldNames = liveVariableScope.getFieldNames();
		List<String> localVariables = liveVariableScope.getLocalVariableNames(scope);
		/*
		 * For Each fragment: Check for name shadowing
		 * 
		 */

		List<VariableDeclarationFragment> reusableRandomDeclaration = new ArrayList<>();
		for (VariableDeclarationFragment fragment : fragments) {
			SimpleName fragmentName = fragment.getName();
			if (!currentFieldNames.contains(fragmentName.getIdentifier())) {
				long matchingVariableNamesCounter = localVariables.stream()
					.filter(name -> name.equals(fragmentName.getIdentifier()))
					.count();
				if (matchingVariableNamesCounter == 1) {
					// move fragment to a field
					reusableRandomDeclaration.add(fragment);
				}
			}
		}

		TypeDeclaration typeDeclaration = ASTNodeUtil.getSpecificAncestor(statement,
				TypeDeclaration.class);

		for (VariableDeclarationFragment fragment : reusableRandomDeclaration) {
			List<ModifierKeyword> requiredModifiers = findRequiredModifiers(statement);
			moveToFields(fragment, typeDeclaration, requiredModifiers);
		}

		int originalNumFragments = statement.fragments()
			.size();
		if (originalNumFragments == reusableRandomDeclaration.size()) {
			astRewrite.remove(statement, null);
		}

		return true;
	}

	private List<ModifierKeyword> findRequiredModifiers(VariableDeclarationStatement statement) {
		List<ModifierKeyword> modifiers = new ArrayList<>();
		modifiers.add(ModifierKeyword.PRIVATE_KEYWORD);
		ASTNodeUtil.convertToTypedList(statement.modifiers(), Modifier.class)
			.stream()
			.map(modifier -> modifier.getKeyword())
			.forEach(modifiers::add);
		BodyDeclaration enclosingBodyDeclaration = ASTNodeUtil.getSpecificAncestor(statement, BodyDeclaration.class);
		if (Modifier.isStatic(enclosingBodyDeclaration.getModifiers())) {
			modifiers.add(ModifierKeyword.STATIC_KEYWORD);
		}
		return modifiers;
	}

	private void moveToFields(VariableDeclarationFragment fragment, TypeDeclaration typeDeclaration,
			List<ModifierKeyword> modifiers) {
		BodyDeclaration nextNode = findSucceedingNode(typeDeclaration, fragment);
		AST ast = typeDeclaration.getAST();
		FieldDeclaration newField = ast
			.newFieldDeclaration((VariableDeclarationFragment) astRewrite.createMoveTarget(fragment));
		newField.setType(ast.newSimpleType(ast.newSimpleName(java.util.Random.class.getSimpleName())));
		ListRewrite fieldsModifersRewriter = astRewrite.getListRewrite(newField, FieldDeclaration.MODIFIERS2_PROPERTY);
		modifiers.forEach(modifier -> fieldsModifersRewriter.insertLast(ast.newModifier(modifier), null));
		ListRewrite listRewrite = astRewrite.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(newField, nextNode, null);
	}

	private BodyDeclaration findSucceedingNode(AbstractTypeDeclaration typeDeclaration,
			VariableDeclarationFragment fragment) {
		List<BodyDeclaration> bodyDeclarations = ASTNodeUtil.convertToTypedList(typeDeclaration.bodyDeclarations(),
				BodyDeclaration.class);

		BodyDeclaration enclosingBodyDeclaration = ASTNodeUtil.getSpecificAncestor(fragment, BodyDeclaration.class);
		BodyDeclaration firstMethodDeclaration = bodyDeclarations.stream()
			.filter(bodyDeclaration -> bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION)
			.findFirst()
			.orElse(null);
		if (enclosingBodyDeclaration == null) {
			return enclosingBodyDeclaration;
		}

		int firstMethodPosition = bodyDeclarations.indexOf(enclosingBodyDeclaration);
		int enclosingBodyDeclarationPosition = bodyDeclarations.indexOf(enclosingBodyDeclaration);
		if (enclosingBodyDeclarationPosition < firstMethodPosition) {
			return enclosingBodyDeclaration;
		}
		return firstMethodDeclaration;

	}

	private List<VariableDeclarationFragment> extractFragmentsWithInitializer(VariableDeclarationStatement statement) {
		return ASTNodeUtil.convertToTypedList(statement.fragments(), VariableDeclarationFragment.class)
			.stream()
			.filter(fragment -> fragment.getInitializer() != null)
			.collect(Collectors.toList());
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		liveVariableScope.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		liveVariableScope.clearFieldScope(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		liveVariableScope.clearLocalVariablesScope(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		liveVariableScope.clearLocalVariablesScope(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		liveVariableScope.clearLocalVariablesScope(initializer);
	}

}
