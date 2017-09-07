package at.splendit.simonykees.core.visitor.renaming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
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

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * A visitor that searches for fields that do not comply with the naming
 * conventions. Makes use of {@link SearchEngine} for finding references of a
 * field within the provided scope. Computes if possible a new legal name for
 * the field and checks if it clashes with other variable names in the same
 * scope.
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class FieldDeclarationASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(FieldDeclarationASTVisitor.class);

	private CompilationUnit compilationUnit;
	private List<FieldMetadata> fieldsMetaData = new ArrayList<>();
	private Map<ASTNode, List<SimpleName>> declaredNamesPerNode = new HashMap<>();
	private List<String> newNamesPerType = new ArrayList<>();
	private Set<IPath> targetResources = new HashSet<>();
	private Set<IJavaElement> targetIJavaElements = new HashSet<>();
	private IJavaProject iJavaProject;
	private IJavaElement[] searchScope;

	public FieldDeclarationASTVisitor(IJavaElement[] scope) {
		this.searchScope = scope;
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
	public void endVisit(TypeDeclaration typeDeclaration) {
		if (!typeDeclaration.isMemberTypeDeclaration()) {
			newNamesPerType.clear();
		}
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {

		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(fieldDeclaration.modifiers(), Modifier.class);
		if (ASTNodeUtil.hasModifier(modifiers, Modifier::isStatic)
				&& ASTNodeUtil.hasModifier(modifiers, Modifier::isFinal)) {
			/*
			 * This visitor is only concerned on non static final fields
			 */
			return true;
		}

		if (ASTNodeUtil.hasModifier(modifiers, Modifier::isPrivate)) {
			/**
			 * private fields are handled in
			 * {@link FieldNameConventionASTVisitor}.
			 */
			return true;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		for (VariableDeclarationFragment fragment : fragments) {
			SimpleName fragmentName = fragment.getName();
			if (!NamingConventionUtil.isComplyingWithConventions(fragmentName.getIdentifier())) {
				NamingConventionUtil.generateNewIdetifier(fragmentName.getIdentifier())
						.filter(newIdentifier -> !isConflictingIdentifier(newIdentifier, fieldDeclaration))
						.ifPresent(newIdentifier -> {
							findFieldReferences(fragment).ifPresent(references -> {
								fieldsMetaData.add(new FieldMetadata(compilationUnit, references, fragment, newIdentifier));
								newNamesPerType.add(newIdentifier);
							});

						});
			}
		}

		return true;
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
		boolean collidesWithstaticImports = imports.stream().filter(ImportDeclaration::isStatic)
				.map(ImportDeclaration::getName).filter(Name::isSimpleName)
				.anyMatch(name -> ((SimpleName) name).getIdentifier().equals(newIdentifier));
		if (collidesWithstaticImports) {
			return true;
		}

		/*
		 * Otherwise, the name does not cause any clashing.
		 */
		return false;
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
				ReferenceSearchMatch reference = new ReferenceSearchMatch(match, fragmentIdentifier);
				references.add(reference);
				IPath path = match.getResource().getFullPath();
				storeIJavaElement((IJavaElement) match.getElement());
				storePath(path);
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
		} catch (CoreException e) {
			logger.error(e.getMessage());
			return Optional.empty();
		}

		return Optional.of(references);
	}

	/**
	 * Collects the {@link IPath}s to the {@link #targetResources}. 
	 * 
	 * @param path a path to be collected.
	 */
	private void storePath(IPath path) {
		this.targetResources.add(path);
	}
	
	private void storeIJavaElement(IJavaElement iJavaElement) {
		this.targetIJavaElements.add(iJavaElement);
	}

	/**
	 * 
	 * @return the set of the paths of the compilation units containing a reference to a 
	 * field being renamed. 
	 */
	public Set<IPath> getTargetCompilationUnitPaths() {
		return this.targetResources;
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
	 * @return the list of the {@link FieldMetadata} resulting from the search process. 
	 */
	public List<FieldMetadata> getFieldMetadata() {
		return this.fieldsMetaData;
	}
}
