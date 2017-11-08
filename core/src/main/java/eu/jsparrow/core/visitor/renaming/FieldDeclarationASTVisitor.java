package eu.jsparrow.core.visitor.renaming;

import static eu.jsparrow.core.util.ASTNodeUtil.hasModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.FileWithCompilationErrorException;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.RefactoringUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;

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

	private static final Logger logger = LoggerFactory.getLogger(FieldDeclarationASTVisitor.class);

	private static final String RENAME_PUBLIC_FIELDS = "public"; //$NON-NLS-1$
	private static final String RENAME_PRIVATE_FIELDS = "private"; //$NON-NLS-1$
	private static final String RENAME_PROTECTED_FIELDS = "protected"; //$NON-NLS-1$
	private static final String RENAME_PACKAGE_PROTECTED_FIELDS = "package-protected"; //$NON-NLS-1$
	private static final String UPPERCASE_FOLLOWING_DOLLAR_SIGN = "uppercase-after-dollar"; //$NON-NLS-1$
	private static final String UPPERCASE_FOLLOWING_UNDERSCORE = "uppercase-after-underscore"; //$NON-NLS-1$
	private static final String ADD_COMMENT = "add-todo"; //$NON-NLS-1$
	private static final String FILE_WITH_COMPILATION_ERROR_EXCEPTION_MESSAGE = "A reference was found in a CompilationUnit with compilation errors."; //$NON-NLS-1$
	
	private Map<String, Boolean> modifierOptions = new HashMap<>();
	
	private CompilationUnit compilationUnit;
	private List<FieldMetadata> fieldsMetaData = new ArrayList<>();
	private Map<ASTNode, List<SimpleName>> declaredNamesPerNode = new HashMap<>();
	private List<String> newNamesPerType = new ArrayList<>();
	private Set<IJavaElement> targetIJavaElements = new HashSet<>();
	private IJavaProject iJavaProject;
	private IJavaElement[] searchScope;
	private List<FieldMetadata> unmodifiableFields = new ArrayList<>();

	public FieldDeclarationASTVisitor(IJavaElement[] scope) {
		this.searchScope = scope;
		activateDefaultOptions();
	}

	/**
	 * Activates the following options for the renaming:
	 * <ul>
	 * <li>{@link #RENAME_PUBLIC_FIELDS} = {@code true}</li>
	 * <li>{@link #RENAME_PACKAGE_PROTECTED_FIELDS} = {@code true}</li>
	 * <li>{@link #RENAME_PROTECTED_FIELDS} = {@code true}</li>
	 * <li>{@link #RENAME_PRIVATE_FIELDS} = {@code false}</li>
	 * <li>{@link #UPPERCASE_FOLLOWING_DOLLAR_SIGN} = {@code true}</li>
	 * <li>{@link #UPPERCASE_FOLLOWING_UNDERSCORE} = {@code true}</li>
	 * <li>{@link #ADD_COMMENT} = {@code false}</li>
	 * </ul>
	 */
	public void activateDefaultOptions() {
		modifierOptions.clear();
		modifierOptions.put(RENAME_PUBLIC_FIELDS, true);
		modifierOptions.put(RENAME_PACKAGE_PROTECTED_FIELDS, true);
		modifierOptions.put(RENAME_PROTECTED_FIELDS, true);
		modifierOptions.put(RENAME_PRIVATE_FIELDS, false);
		modifierOptions.put(UPPERCASE_FOLLOWING_DOLLAR_SIGN, true);
		modifierOptions.put(UPPERCASE_FOLLOWING_UNDERSCORE, true);
		modifierOptions.put(ADD_COMMENT, false);
		
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		if (iJavaProject == null) {
			iJavaProject = compilationUnit.getJavaElement().getJavaProject();
		}
		return true;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		declaredNamesPerNode.clear();
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
	public boolean visit(FieldDeclaration fieldDeclaration) {

		if(hasToBeSkippedModifier(fieldDeclaration)) {
			return true;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		for (VariableDeclarationFragment fragment : fragments) {
			SimpleName fragmentName = fragment.getName();
			if (!NamingConventionUtil.isComplyingWithConventions(fragmentName.getIdentifier())) {
				boolean upperCaseAfterDollar = getUppercaseAfterDollar();
				boolean upperCaseAfterUnderscore = getUppercaseAfterUnderscore();
				Optional<String> optNewIdentifier = NamingConventionUtil.generateNewIdetifier(
						fragmentName.getIdentifier(), upperCaseAfterDollar, upperCaseAfterUnderscore);
				if (optNewIdentifier.isPresent()
						&& !isConflictingIdentifier(optNewIdentifier.get(), fieldDeclaration)) {
					String newIdentifier = optNewIdentifier.get();
					storeIJavaElement(compilationUnit.getJavaElement());
					findFieldReferences(fragment).ifPresent(references -> {
						fieldsMetaData.add(new FieldMetadata(compilationUnit, references, fragment, newIdentifier));
						newNamesPerType.add(newIdentifier);
					});

				} else if (getAddTodo()) {
					FieldMetadata unmodifiableFieldDdata = new FieldMetadata(compilationUnit, Collections.emptyList(),
							fragment, fragment.getName().getIdentifier());
					unmodifiableFields.add(unmodifiableFieldDdata);
				}

			}
		}

		return true;
	}

	/**
	 * 
	 * @param fieldDeclaration
	 * @return
	 */
	private boolean hasToBeSkippedModifier(FieldDeclaration fieldDeclaration) {
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(fieldDeclaration.modifiers(), Modifier.class);
		return (modifiers.isEmpty() && !getRenamePackageProtectedField())
				|| (hasModifier(modifiers, Modifier::isPublic) && !getRenamePublicField())
				|| (hasModifier(modifiers, Modifier::isProtected) && !getRenameProtectedField())
				|| (hasModifier(modifiers, Modifier::isPrivate) && !getRenamePrivateField())
				|| (hasModifier(modifiers, Modifier::isStatic)
						&& hasModifier(modifiers, Modifier::isFinal));
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
		ASTNode parent = fieldDeclaration.getParent();

		/*
		 * The new name does not conflict with another newly introduced name.
		 */
		if (newNamesPerType.contains(newIdentifier)) {
			return true;
		}

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
		return imports.stream().filter(ImportDeclaration::isStatic)
				.map(ImportDeclaration::getName).filter(Name::isSimpleName)
				.anyMatch(name -> ((SimpleName) name).getIdentifier().equals(newIdentifier));
	}

	/**
	 * Checks for a match between any of the identifiers of the simple names in the given list and 
	 * the given new identifier. 
	 * 
	 * @param declaredNames a list of simple names to be checked.
	 * @param newIdentifier a string representing a new identifier.
	 * @return {@code true} if the match is found or {@code false} otherwise.
	 */
	private boolean matchesIdentifier(List<SimpleName> declaredNames, String newIdentifier) {
		return declaredNames.stream().map(SimpleName::getIdentifier).anyMatch(newIdentifier::equals);
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

	/**
	 * Makes use of {@link SearchEngine} for finding the references of a field
	 * which is declared in the given declaration fragment. Uses
	 * {@link #searchScope} for as the scope of the search. Discards the whole
	 * search if an error occurs during the search process.
	 * 
	 * @param fragment
	 *            a declaration fragment belonging to a field declaration.
	 * @return an optional of the list of {@link ReferenceSearchMatch}s or an
	 *         empty optional if a {@link CoreException} is thrown during the
	 *         search.
	 */
	private Optional<List<ReferenceSearchMatch>> findFieldReferences(VariableDeclarationFragment fragment) {
		IJavaElement iVariableBinding = fragment.resolveBinding().getJavaElement();

		/*
		 * Create a pattern that searches for references of a field.
		 */
		IField iField = (IField) iVariableBinding;
		SearchPattern searchPattern = SearchPattern.createPattern(iField, IJavaSearchConstants.REFERENCES);
		/*
		 * Create the search scope based on the provided scope.
		 */
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(searchScope);

		/*
		 * A list to store the references resulting from the search process.
		 */
		List<ReferenceSearchMatch> references = new ArrayList<>();
		String fragmentIdentifier = fragment.getName().getIdentifier();

		/*
		 * The object that stores the search result.
		 */
		SearchRequestor requestor = new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) {
				IJavaElement iJavaElement = (IJavaElement) match.getElement();
				IMember iMember = (IMember)iJavaElement;
				ICompilationUnit icu = iMember.getCompilationUnit();
				ReferenceSearchMatch reference = new ReferenceSearchMatch(match, fragmentIdentifier, icu);
				references.add(reference);
				if(RefactoringUtil.checkForSyntaxErrors(icu)) {
					references.clear();
					throw new FileWithCompilationErrorException(FILE_WITH_COMPILATION_ERROR_EXCEPTION_MESSAGE);
				}
				storeIJavaElement(icu);
			}
		};

		/*
		 * Finally, the search engine which performs the actual search based on
		 * the prepared pattern, scope and the requestor.
		 */
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
					scope, requestor, null);
		} catch (CoreException | FileWithCompilationErrorException e) {
			logger.error(e.getMessage());
			return Optional.empty();
		}

		return Optional.of(references);
	}
	
	private void storeIJavaElement(IJavaElement iJavaElement) {
		this.targetIJavaElements.add(iJavaElement);
	}
	

	/**
	 * 
	 * @return the set of the {@link IJavaElement}s containing a reference to a 
	 * field being renamed.
	 */
	public Set<IJavaElement> getTargetIJavaElements() {
		return this.targetIJavaElements;
	}

	/**
	 * Computes the list of all compilation units that are affected by the 
	 * renaming. Makes use of the {@link #fieldsMetaData} to collect distinct
	 * compilation units of all search results. 
	 * 
	 * @return
	 */
	public List<ICompilationUnit> computeAllTargetCompilationUnits() {
		return this.fieldsMetaData.stream().flatMap(metaData -> metaData.getTargetICompilationUnits().stream())
				.distinct().collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @return the list of the {@link FieldMetadata} corresponding to 
	 * the fields to be that are found from the search process. 
	 */
	public List<FieldMetadata> getFieldMetadata() {
		return this.fieldsMetaData;
	}
	
	/**
	 * 
	 * @return the list of the {@link FieldMetadata} corresponding to 
	 * the fields that cannot be renamed due to unfeasibility of 
	 * automatic generation of a new legal identifier.
	 */
	public List<FieldMetadata> getUnmodifiableFieldMetadata() {
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
	
	public void setUppercaseAfterDollar(boolean value) {
		this.modifierOptions.put(UPPERCASE_FOLLOWING_DOLLAR_SIGN, value);
	}
	
	public void setUppercaseAfterUnderscore(boolean value) {
		this.modifierOptions.put(UPPERCASE_FOLLOWING_UNDERSCORE, value);
	}
	
	public void setAddTodo(boolean value) {
		this.modifierOptions.put(ADD_COMMENT, value);
	}
	
	private boolean getRenamePublicField() {
		return modifierOptions.containsKey(RENAME_PUBLIC_FIELDS) && modifierOptions.get(RENAME_PUBLIC_FIELDS);
	}
	
	private boolean getRenamePackageProtectedField() {
		return modifierOptions.containsKey(RENAME_PACKAGE_PROTECTED_FIELDS) && modifierOptions.get(RENAME_PACKAGE_PROTECTED_FIELDS);
	}
	
	private boolean getRenameProtectedField() {
		return modifierOptions.containsKey(RENAME_PROTECTED_FIELDS) && modifierOptions.get(RENAME_PROTECTED_FIELDS);
	}
	
	private boolean getRenamePrivateField() {
		return modifierOptions.containsKey(RENAME_PRIVATE_FIELDS) && modifierOptions.get(RENAME_PRIVATE_FIELDS);
	}
	
	private boolean getUppercaseAfterDollar() {
		return modifierOptions.containsKey(UPPERCASE_FOLLOWING_DOLLAR_SIGN) && modifierOptions.get(UPPERCASE_FOLLOWING_DOLLAR_SIGN);
	}
	
	private boolean getUppercaseAfterUnderscore() {
		return modifierOptions.containsKey(UPPERCASE_FOLLOWING_UNDERSCORE) && modifierOptions.get(UPPERCASE_FOLLOWING_UNDERSCORE);
	}
	
	private boolean getAddTodo() {
		return modifierOptions.containsKey(ADD_COMMENT) && modifierOptions.get(ADD_COMMENT);
	}
}
