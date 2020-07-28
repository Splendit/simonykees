package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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

import eu.jsparrow.core.visitor.impl.trycatch.ReferencedVariablesASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Creating a new Random object each time a random value is needed is
 * inefficient and may produce numbers which are not random. This visitor
 * extracts reusable {@link java.util.Random} objects, from local variables to
 * class or instance fields. For example, the following code:
 * 
 * <pre>
 * <code>
 * 	private void sampleMethod(String value) {
 *		Random r = new Random();
 *		int i = r.nextInt();
 *	}
 * </code>
 * </pre>
 * 
 * will be transformed into
 * 
 * <pre>
 * <code> 
 * 	Random r = new Random();
 * 	private void sampleMethod(String value) {
 *		int i = r.nextInt();
 *	}
 * </code>
 * </pre>
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

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return false;
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
			if (hasReusableField(fragment, requiredModifiers)) {
				alreadyExtracted.add(fragment);
			} else if (isExtractableLocalVariable(fragment)) {
				reusableRandomDeclaration.add(fragment);
			}
		}

		TypeDeclaration typeDeclaration = ASTNodeUtil.getSpecificAncestor(statement,
				TypeDeclaration.class);

		for (VariableDeclarationFragment fragment : reusableRandomDeclaration) {
			moveToFields(fragment, typeDeclaration, requiredModifiers, statement.getType());
			onRewrite();
			introducedFields.add(new FieldProperties(fragment.getName()
				.getIdentifier(), requiredModifiers, fragment.getInitializer()));
		}

		for (VariableDeclarationFragment fragment : alreadyExtracted) {
			astRewrite.remove(fragment, null);
			onRewrite();
		}

		int originalNumFragments = statement.fragments()
			.size();
		if (originalNumFragments == reusableRandomDeclaration.size() + alreadyExtracted.size()) {
			astRewrite.remove(statement, null);
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
				existingRandomFields
					.add(new FieldProperties(name.getIdentifier(), modifiers, fragment.getInitializer()));
			} else {
				remainingFieldNames.add(fragment.getName()
					.getIdentifier());
			}
		}
	}

	private boolean hasReusableField(List<FieldProperties> fieldProperties, VariableDeclarationFragment fragment,
			List<ModifierKeyword> requiredModifiers) {
		SimpleName name = fragment.getName();
		String identifier = name.getIdentifier();
		if (!FieldProperties.contains(fieldProperties, identifier)) {
			return false;
		}

		FieldProperties matchingField = fieldProperties.stream()
			.filter(field -> identifier.equals(field.getName()))
			.findFirst()
			.orElse(null);
		if (matchingField == null) {
			return false;
		}

		Expression fieldInitializer = matchingField.getInitializer();
		Expression fragmentInitializer = fragment.getInitializer();

		ASTMatcher matcher = new ASTMatcher();
		if (!matcher.safeSubtreeMatch(fieldInitializer, fragmentInitializer)) {
			return false;
		}

		boolean isStaticRequired = requiredModifiers.contains(ModifierKeyword.STATIC_KEYWORD);
		if (!isStaticRequired) {
			return true;
		}

		return matchingField.getModifiers()
			.contains(ModifierKeyword.STATIC_KEYWORD);
	}

	private boolean hasReusableField(VariableDeclarationFragment fragment,
			List<ModifierKeyword> requiredModifiers) {

		String identifier = fragment.getName()
			.getIdentifier();
		if (remainingFieldNames.contains(identifier)) {
			return false;
		}

		return hasReusableField(introducedFields, fragment, requiredModifiers)
				|| hasReusableField(existingRandomFields, fragment, requiredModifiers);
	}

	private boolean isExtractableLocalVariable(VariableDeclarationFragment fragment) {
		SimpleName name = fragment.getName();
		String identifier = name.getIdentifier();
		if (remainingFieldNames.contains(identifier)) {
			return false;
		}

		Expression initializer = fragment.getInitializer();

		ReferencedVariablesASTVisitor variableReferencesVisitor = new ReferencedVariablesASTVisitor();
		initializer.accept(variableReferencesVisitor);
		List<SimpleName> referencedVariables = variableReferencesVisitor.getReferencedVariables();
		if (!referencedVariables.isEmpty()) {
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
			List<ModifierKeyword> modifiers, Type type) {
		BodyDeclaration nextNode = findSucceedingNode(typeDeclaration, fragment);
		AST ast = typeDeclaration.getAST();
		FieldDeclaration newField = ast
			.newFieldDeclaration((VariableDeclarationFragment) astRewrite.createMoveTarget(fragment));
		newField.setType((Type) astRewrite.createCopyTarget(type));
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
		Expression initializer;

		public FieldProperties(String name, List<ModifierKeyword> modifiers, Expression initializer) {
			this.name = name;
			this.modifiers = modifiers;
			this.initializer = initializer;
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

		public Expression getInitializer() {
			return initializer;
		}
	}
}
