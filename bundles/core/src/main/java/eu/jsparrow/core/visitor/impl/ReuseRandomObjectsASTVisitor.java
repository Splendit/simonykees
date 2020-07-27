package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
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

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * 
 * @since 3.20.0
 *
 */
public class ReuseRandomObjectsASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<FieldProperties> introducedFields = new ArrayList<>();
	private List<FieldProperties> existingRandomFields = new ArrayList<>();
	private List<String> remainingFieldNames = new ArrayList<>();

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isMemberTypeDeclaration() || typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}

		for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
			List<VariableDeclarationFragment> fragments = ASTNodeUtil
				.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class);
			if (isJavaUtilRandomType(fieldDeclaration.getType())
					&& Modifier.isPrivate(fieldDeclaration.getModifiers())) {
				collectReusableRandomObjects(fieldDeclaration, fragments);
			} else {
				fragments.forEach(fragment -> remainingFieldNames.add(fragment.getName()
					.getIdentifier()));
			}
		}
		return true;
	}

	private void collectReusableRandomObjects(FieldDeclaration fieldDeclaration,
			List<VariableDeclarationFragment> fragments) {
		List<ModifierKeyword> modifiers = ASTNodeUtil
			.convertToTypedList(fieldDeclaration.modifiers(), Modifier.class)
			.stream()
			.map(Modifier::getKeyword)
			.collect(Collectors.toList());

		for (VariableDeclarationFragment fragment : fragments) {
			Expression initializer = fragment.getInitializer();
			if (initializer != null && initializer.getNodeType() != ASTNode.NULL_LITERAL) {
				SimpleName name = fragment.getName();
				existingRandomFields.add(new FieldProperties(name.getIdentifier(), modifiers));
			} else {
				remainingFieldNames.add(fragment.getName()
					.getIdentifier());
			}
		}
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		existingRandomFields.clear();
		remainingFieldNames.clear();
		introducedFields.clear();
		super.endVisit(typeDeclaration);
	}

	@Override
	public boolean visit(VariableDeclarationStatement statement) {

		if (!isJavaUtilRandomType(statement.getType())) {
			return false;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil
			.convertToTypedList(statement.fragments(), VariableDeclarationFragment.class)
			.stream()
			.filter(fragment -> fragment.getInitializer() != null && fragment.getInitializer()
				.getNodeType() != ASTNode.NULL_LITERAL)
			.collect(Collectors.toList());
		List<VariableDeclarationFragment> reusableRandomDeclaration = new ArrayList<>();
		List<VariableDeclarationFragment> alreadyExtracted = new ArrayList<>();
		List<ModifierKeyword> requiredModifiers = findRequiredModifiers(statement);
		for (VariableDeclarationFragment fragment : fragments) {
			SimpleName fragmentName = fragment.getName();
			String identifier = fragmentName.getIdentifier();
			if (hasReusableField(identifier, requiredModifiers)) {
				alreadyExtracted.add(fragment);
			} else if (isExtractableLocalVariable(identifier)) {
				reusableRandomDeclaration.add(fragment);
			}
		}

		TypeDeclaration typeDeclaration = ASTNodeUtil.getSpecificAncestor(statement,
				TypeDeclaration.class);

		for (VariableDeclarationFragment fragment : reusableRandomDeclaration) {
			moveToFields(fragment, typeDeclaration, requiredModifiers);
			introducedFields.add(new FieldProperties(fragment.getName()
				.getIdentifier(), requiredModifiers));
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

	private boolean hasReusableField(List<FieldProperties> fieldProperties, String identifier,
			List<ModifierKeyword> requiredModifiers) {
		if (!FieldProperties.contains(fieldProperties, identifier)) {
			return false;
		}
		boolean isStaticRequired = requiredModifiers.contains(ModifierKeyword.STATIC_KEYWORD);
		if (!isStaticRequired) {
			return true;
		}
		return existingRandomFields.stream()
			.filter(field -> identifier.equals(field.getName()))
			.findFirst()
			.map(FieldProperties::getModifiers)
			.filter(modifiers -> modifiers.contains(ModifierKeyword.STATIC_KEYWORD))
			.isPresent();
	}

	private boolean hasReusableField(String identifier,
			List<ModifierKeyword> requiredModifiers) {

		if (remainingFieldNames.contains(identifier)) {
			return false;
		}

		return hasReusableField(introducedFields, identifier, requiredModifiers)
				|| hasReusableField(existingRandomFields, identifier, requiredModifiers);
	}

	private boolean isExtractableLocalVariable(String identifier) {
		if (remainingFieldNames.contains(identifier)) {
			return false;
		}

		return !FieldProperties.contains(introducedFields, identifier)
				&& !FieldProperties.contains(existingRandomFields, identifier);
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
		if (firstMethodDeclaration == null) {
			return enclosingBodyDeclaration;
		}

		int firstMethodPosition = bodyDeclarations.indexOf(firstMethodDeclaration);
		int enclosingBodyDeclarationPosition = bodyDeclarations.indexOf(enclosingBodyDeclaration);
		if (enclosingBodyDeclarationPosition < firstMethodPosition) {
			return enclosingBodyDeclaration;
		}
		return firstMethodDeclaration;

	}

	static class FieldProperties {
		String name;
		List<ModifierKeyword> modifiers;

		public FieldProperties(String name, List<ModifierKeyword> modifiers) {
			this.name = name;
			this.modifiers = modifiers;
		}

		public static boolean contains(List<FieldProperties> fieldProperties, String name) {
			return fieldProperties.stream()
				.anyMatch(field -> name.equals(field.getName()));

		}

		public String getName() {
			return this.name;
		}

		public List<ModifierKeyword> getModifiers() {
			return modifiers;
		}
	}
}
