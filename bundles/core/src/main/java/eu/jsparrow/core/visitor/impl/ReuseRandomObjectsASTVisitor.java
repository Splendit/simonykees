package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
	private List<String> introducedFields = new ArrayList<>();
	private List<String> existingRandomFields = new ArrayList<>();
	private List<String> remainingFieldNames = new ArrayList<>();

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isMemberTypeDeclaration() || typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}

		existingRandomFields = Arrays.stream(typeDeclaration.getFields())
			.filter(field -> isJavaUtilRandomType(field.getType()))
			.flatMap(field -> ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class)
				.stream())
			// otherwise we don't know if the field is ever initialized
			.filter(fragment -> fragment.getInitializer() != null)
			.map(VariableDeclarationFragment::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		remainingFieldNames = Arrays.stream(typeDeclaration.getFields())
			.flatMap(field -> ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class)
				.stream())
			.map(VariableDeclarationFragment::getName)
			.map(SimpleName::getIdentifier)
			.filter(name -> !existingRandomFields.contains(name))
			.collect(Collectors.toList());

		return true;
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		existingRandomFields.clear();
		remainingFieldNames.clear();
		super.endVisit(typeDeclaration);
	}

	@Override
	public boolean visit(VariableDeclarationStatement statement) {

		if (!isJavaUtilRandomType(statement.getType())) {
			return false;
		}

		List<VariableDeclarationFragment> fragments = extractFragmentsWithInitializer(statement);
		ASTNode scope = liveVariableScope.findEnclosingScope(statement)
			.orElse(null);
		if (scope == null) {
			return false;
		}

		List<VariableDeclarationFragment> reusableRandomDeclaration = new ArrayList<>();
		List<VariableDeclarationFragment> alreadyExtracted = new ArrayList<>();
		for (VariableDeclarationFragment fragment : fragments) {
			SimpleName fragmentName = fragment.getName();
			if (!remainingFieldNames.contains(fragmentName.getIdentifier())) {
				if (introducedFields.contains(fragmentName.getIdentifier())
						|| existingRandomFields.contains(fragmentName.getIdentifier())) {
					alreadyExtracted.add(fragment);
				} else {
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
			introducedFields.add(fragment.getName()
				.getIdentifier());
		}

		for (VariableDeclarationFragment fragment : alreadyExtracted) {
			astRewrite.remove(fragment, null);
		}

		int originalNumFragments = statement.fragments()
			.size();
		if (originalNumFragments == reusableRandomDeclaration.size() + alreadyExtracted.size()) {
			astRewrite.remove(statement, null);
		}

		return true;
	}

	private boolean isJavaUtilRandomType(Type type) {
		ITypeBinding typeBinding = type.resolveBinding();
		if (typeBinding == null) {
			return false;
		}
		return ClassRelationUtil.isContentOfType(typeBinding, java.util.Random.class.getName());
	}

	private List<ModifierKeyword> findRequiredModifiers(VariableDeclarationStatement statement) {
		List<ModifierKeyword> modifiers = new ArrayList<>();
		modifiers.add(ModifierKeyword.PRIVATE_KEYWORD);
		ASTNodeUtil.convertToTypedList(statement.modifiers(), Modifier.class)
			.stream()
			.map(Modifier::getKeyword)
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
}
