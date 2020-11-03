package eu.jsparrow.core.visitor.renaming;

import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.ADD_COMMENT;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PACKAGE_PROTECTED_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PRIVATE_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PROTECTED_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PUBLIC_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_DOLLAR_SIGN;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_UNDERSCORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.SearchEngine;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.VariableDeclarationsVisitor;
/**
 * A visitor that searches for fields that do not comply with the naming
 * conventions. Makes use of {@link SearchEngine} for finding references of a
 * field within the provided scope. Computes if possible a new legal name for
 * the field and checks if it clashes with other variable names in the same
 * scope.
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class FieldDeclarationASTVisitor extends AbstractASTRewriteASTVisitor {

	private Map<String, Boolean> modifierOptions = new HashMap<>();

	private CompilationUnit compilationUnit;
	private List<FieldMetaData> fieldsMetaData = new ArrayList<>();
	private Map<ASTNode, List<SimpleName>> declaredNamesPerNode = new HashMap<>();
	private List<String> newNamesPerType = new ArrayList<>();
	private Set<ICompilationUnit> targetIJavaElements = new HashSet<>();
	private IJavaProject iJavaProject;
	private List<FieldMetaData> unmodifiableFields = new ArrayList<>();
	private FieldReferencesSearch searchEngine;

	public FieldDeclarationASTVisitor(IJavaElement[] scope) {
		this.searchEngine = new FieldReferencesSearch(scope);
		activateDefaultOptions();
	}

	/**
	 * Activates the following options for the renaming:
	 * <ul>
	 * <li>{@link #RENAME_PUBLIC_FIELDS} = {@code true}</li>
	 * <li>{@link #RENAME_PACKAGE_PROTECTED_FIELDS} = {@code true}</li>
	 * <li>{@link #RENAME_PROTECTED_FIELDS} = {@code true}</li>
	 * <li>{@link #RENAME_PRIVATE_FIELDS} = {@code false}</li>
	 * <li>{@link #UPPER_CASE_FOLLOWING_DOLLAR_SIGN} = {@code true}</li>
	 * <li>{@link #UPPER_CASE_FOLLOWING_UNDERSCORE} = {@code true}</li>
	 * <li>{@link #ADD_COMMENT} = {@code false}</li>
	 * </ul>
	 */
	public void activateDefaultOptions() {
		modifierOptions.clear();
		modifierOptions.put(RENAME_PUBLIC_FIELDS, true);
		modifierOptions.put(RENAME_PACKAGE_PROTECTED_FIELDS, true);
		modifierOptions.put(RENAME_PROTECTED_FIELDS, true);
		modifierOptions.put(RENAME_PRIVATE_FIELDS, false);
		modifierOptions.put(UPPER_CASE_FOLLOWING_DOLLAR_SIGN, true);
		modifierOptions.put(UPPER_CASE_FOLLOWING_UNDERSCORE, true);
		modifierOptions.put(ADD_COMMENT, false);
	}

	public void updateOptions(Map<String, Boolean> options) {
		modifierOptions.putAll(options);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		if (iJavaProject == null) {
			iJavaProject = compilationUnit.getJavaElement()
				.getJavaProject();
		}
		super.visit(compilationUnit);
		return true;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		declaredNamesPerNode.clear();
		super.endVisit(compilationUnit);
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		return !typeDeclaration.isInterface();
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		if (!typeDeclaration.isMemberTypeDeclaration()) {
			newNamesPerType.clear();
		}
	}

	@Override
	public boolean visit(AnonymousClassDeclaration anonymousClass) {
		/*
		 * Fields declared in the body of an anonymous class are ignored because
		 * the search engine does not find their references correctly. see
		 * SIM-934
		 */
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {

		if (hasSkippedModifier(fieldDeclaration)) {
			return true;
		}

		if (!hasSafeTypeName(fieldDeclaration)) {
			return true;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		fragments.forEach(fragment -> {
			SimpleName fragmentName = fragment.getName();
			if (!NamingConventionUtil.isComplyingWithConventions(fragmentName.getIdentifier())) {
				boolean upperCaseAfterDollar = getUpperCaseAfterDollar();
				boolean upperCaseAfterUnderscore = getUpperCaseAfterUnderscore();
				Optional<String> optNewIdentifier = NamingConventionUtil.generateNewIdentifier(
						fragmentName.getIdentifier(), upperCaseAfterDollar, upperCaseAfterUnderscore);
				if (optNewIdentifier.isPresent()
						&& !isConflictingIdentifier(optNewIdentifier.get(), fieldDeclaration)) {
					String newIdentifier = optNewIdentifier.get();
					storeIJavaElement((ICompilationUnit) compilationUnit.getJavaElement());

					searchEngine.findFieldReferences(fragment)
						.ifPresent(references -> {
							storeIJavaElement(searchEngine.getTargetIJavaElements());
							fieldsMetaData.add(new FieldMetaData(compilationUnit, references, fragment, newIdentifier));
							newNamesPerType.add(newIdentifier);
						});

				} else if (getAddTodo()) {
					FieldMetaData unmodifiableFieldDdata = new FieldMetaData(compilationUnit, Collections.emptyList(),
							fragment, fragment.getName()
								.getIdentifier());
					unmodifiableFields.add(unmodifiableFieldDdata);
				}
			}
		});
		return true;
	}

	private boolean hasSafeTypeName(FieldDeclaration fieldDeclaration) {
		Type type = fieldDeclaration.getType();
		ITypeBinding binding = type.resolveBinding();
		if (binding == null) {
			return false;
		}
		String typeName = binding.getName();
		return !StringUtils.contains(typeName, "$"); //$NON-NLS-1$

	}

	/**
	 * 
	 * @param fieldDeclaration
	 * @return
	 */
	private boolean hasSkippedModifier(FieldDeclaration fieldDeclaration) {
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(fieldDeclaration.modifiers(), Modifier.class);
		return (ASTNodeUtil.isPackageProtected(modifiers) && !getRenamePackageProtectedField())
				|| (ASTNodeUtil.hasModifier(modifiers, Modifier::isPublic) && !getRenamePublicField())
				|| (ASTNodeUtil.hasModifier(modifiers, Modifier::isProtected) && !getRenameProtectedField())
				|| (ASTNodeUtil.hasModifier(modifiers, Modifier::isPrivate) && !getRenamePrivateField())
				|| (ASTNodeUtil.hasModifier(modifiers, Modifier::isStatic) && ASTNodeUtil.hasModifier(modifiers, Modifier::isFinal));
	}

	/**
	 * Checks whether the given new identifier causes a naming conflict on the
	 * scope where the given field declaration falls into.
	 * 
	 * @param newIdentifier
	 *            the new identifier to be checked.
	 * @param fieldDeclaration
	 *            a field declaration to get the scope from
	 * 
	 * @return {@code true} if the new identifier causes a naming conflict or
	 *         {@code false} otherwise.
	 */
	private boolean isConflictingIdentifier(String newIdentifier, FieldDeclaration fieldDeclaration) {

		/*
		 * The new name does not conflict with another newly introduced name.
		 */
		if (newNamesPerType.contains(newIdentifier)) {
			return true;
		}

		ASTNode parent = fieldDeclaration.getParent();
		/*
		 * The new name should not shadow another local variable.
		 */
		List<SimpleName> declaredNames = findDeclaredNames(parent);
		if (matchesIdentifier(declaredNames, newIdentifier)) {
			return true;
		}

		/*
		 * the new name should not shadow a field from the outer class.
		 */
		if (parent.getNodeType() == ASTNode.TYPE_DECLARATION) {
			TypeDeclaration type = (TypeDeclaration) parent;
			if (type.isMemberTypeDeclaration()) {
				ASTNode outerType = type.getParent();
				if (ASTNode.TYPE_DECLARATION == outerType.getNodeType()
						&& NamingConventionUtil.hasField((TypeDeclaration) outerType, newIdentifier)) {
					return true;
				}
			}
		}

		/*
		 * The new name should not conflict with a statically imported variable
		 */
		List<ImportDeclaration> imports = ASTNodeUtil.returnTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		return imports.stream()
			.filter(ImportDeclaration::isStatic)
			.map(ImportDeclaration::getName)
			.filter(Name::isSimpleName)
			.anyMatch(name -> ((SimpleName) name).getIdentifier()
				.equals(newIdentifier));
	}

	/**
	 * Checks for a match between any of the identifiers of the simple names in
	 * the given list and the given new identifier.
	 * 
	 * @param declaredNames
	 *            a list of simple names to be checked.
	 * @param newIdentifier
	 *            a string representing a new identifier.
	 * @return {@code true} if the match is found or {@code false} otherwise.
	 */
	private boolean matchesIdentifier(List<SimpleName> declaredNames, String newIdentifier) {
		return declaredNames.stream()
			.map(SimpleName::getIdentifier)
			.anyMatch(newIdentifier::equals);
	}

	/**
	 * Makes use of the {@link VariableDeclarationsVisitor} for finding and
	 * storing the names of the declared variables in the code represented by
	 * the given node.
	 * 
	 * @param node
	 *            a node representing a code snippet.
	 * @return the list of the names of the variables declared in the given
	 *         node.
	 */
	private List<SimpleName> findDeclaredNames(ASTNode node) {
		if (declaredNamesPerNode.containsKey(node)) {
			return declaredNamesPerNode.get(node);
		} else {
			VariableDeclarationsVisitor declVisitor = new VariableDeclarationsVisitor();
			node.accept(declVisitor);
			List<SimpleName> declaredNames = declVisitor.getVariableDeclarationNames();
			declaredNamesPerNode.put(node, declaredNames);

			return declaredNames;
		}
	}

	private void storeIJavaElement(ICompilationUnit iJavaElement) {
		this.targetIJavaElements.add(iJavaElement);
	}

	private void storeIJavaElement(Set<ICompilationUnit> targetIJavaElements2) {
		targetIJavaElements2.forEach(this::storeIJavaElement);
	}

	/**
	 * 
	 * @return the set of the {@link IJavaElement}s containing a reference to a
	 *         field being renamed.
	 */
	public Set<ICompilationUnit> getTargetIJavaElements() {
		return this.targetIJavaElements;
	}

	/**
	 * 
	 * @return the list of the {@link FieldMetaData} corresponding to the fields
	 *         to be that are found from the search process.
	 */
	public List<FieldMetaData> getFieldMetaData() {
		return this.fieldsMetaData;
	}

	/**
	 * 
	 * @return the list of the {@link FieldMetaData} corresponding to the fields
	 *         that cannot be renamed due to unfeasibility of automatic
	 *         generation of a new legal identifier.
	 */
	public List<FieldMetaData> getUnmodifiableFieldMetaData() {
		return this.unmodifiableFields;
	}

	/*
	 * Getters and setters for renaming options.
	 */

	public void setRenamePublicField(boolean value) {
		this.modifierOptions.put(RENAME_PUBLIC_FIELDS, value);
	}

	public void setRenameProtectedField(boolean value) {
		this.modifierOptions.put(RENAME_PROTECTED_FIELDS, value);
	}

	public void setRenamePackageProtectedField(boolean value) {
		this.modifierOptions.put(RENAME_PACKAGE_PROTECTED_FIELDS, value);
	}

	public void setRenamePrivateField(boolean value) {
		this.modifierOptions.put(RENAME_PRIVATE_FIELDS, value);
	}

	public void setUpperCaseAfterDollar(boolean value) {
		this.modifierOptions.put(UPPER_CASE_FOLLOWING_DOLLAR_SIGN, value);
	}

	public void setUpperCaseAfterUnderscore(boolean value) {
		this.modifierOptions.put(UPPER_CASE_FOLLOWING_UNDERSCORE, value);
	}

	public void setAddTodo(boolean value) {
		this.modifierOptions.put(ADD_COMMENT, value);
	}

	private boolean getRenamePublicField() {
		return modifierOptions.containsKey(RENAME_PUBLIC_FIELDS) && modifierOptions.get(RENAME_PUBLIC_FIELDS);
	}

	private boolean getRenamePackageProtectedField() {
		return modifierOptions.containsKey(RENAME_PACKAGE_PROTECTED_FIELDS)
				&& modifierOptions.get(RENAME_PACKAGE_PROTECTED_FIELDS);
	}

	private boolean getRenameProtectedField() {
		return modifierOptions.containsKey(RENAME_PROTECTED_FIELDS) && modifierOptions.get(RENAME_PROTECTED_FIELDS);
	}

	private boolean getRenamePrivateField() {
		return modifierOptions.containsKey(RENAME_PRIVATE_FIELDS) && modifierOptions.get(RENAME_PRIVATE_FIELDS);
	}

	private boolean getUpperCaseAfterDollar() {
		return modifierOptions.containsKey(UPPER_CASE_FOLLOWING_DOLLAR_SIGN)
				&& modifierOptions.get(UPPER_CASE_FOLLOWING_DOLLAR_SIGN);
	}

	private boolean getUpperCaseAfterUnderscore() {
		return modifierOptions.containsKey(UPPER_CASE_FOLLOWING_UNDERSCORE)
				&& modifierOptions.get(UPPER_CASE_FOLLOWING_UNDERSCORE);
	}

	private boolean getAddTodo() {
		return modifierOptions.containsKey(ADD_COMMENT) && modifierOptions.get(ADD_COMMENT);
	}
}
