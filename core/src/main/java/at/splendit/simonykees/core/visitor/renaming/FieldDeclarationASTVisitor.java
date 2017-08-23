package at.splendit.simonykees.core.visitor.renaming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.text.edits.TextEditGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * 
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
	private IJavaProject iJavaProject;
	private IJavaElement[] iPackageFragment;
	
	
	public FieldDeclarationASTVisitor(IJavaElement[] scope) {
		this.iPackageFragment = scope;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		if(iJavaProject == null) {
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
		if(!typeDeclaration.isMemberTypeDeclaration()) {			
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
							List<ReferenceSearchMatch> references = findFieldReferences(fragment);
							fieldsMetaData.add(new MetaData(compilationUnit, references, fragment, newIdentifier));
							newNamesPerType.add(newIdentifier);
						});
			}
		}

		return true;
	}

	/**
	 * 
	 * @param newIdentifier
	 * @param fieldDeclaration
	 * @return
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
	 * 
	 * @param declaredNames
	 * @param newIdentifier
	 * @return
	 */
	private boolean matchesIdentifier(List<SimpleName> declaredNames, String newIdentifier) {
		return declaredNames.stream().map(SimpleName::getIdentifier).anyMatch(newIdentifier::equals);
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	private List<SimpleName> findDeclaredNames(ASTNode parent) {
		if (declaredNamesPerNode.containsKey(parent)) {
			return declaredNamesPerNode.get(parent);
		} else {
			VariableDeclarationsVisitor declVisitor = new VariableDeclarationsVisitor();
			parent.accept(declVisitor);
			List<SimpleName> declaredNames = declVisitor.getVariableDeclarationNames();
			declaredNamesPerNode.put(parent, declaredNames);

			return declaredNames;
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<FieldMetadata> getFieldMetadata() {
		return this.fieldsMetaData;
	}

	/**
	 * 
	 * @param fragment
	 * @return
	 */
	private List<ReferenceSearchMatch> findFieldReferences(VariableDeclarationFragment fragment) {
		IJavaElement iVariableBinding = fragment.resolveBinding().getJavaElement();
		
		IField iField = (IField) iVariableBinding;
		SearchPattern searchPattern = SearchPattern.createPattern(iField, IJavaSearchConstants.REFERENCES);
		IJavaElement[] projectScope = iPackageFragment;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(projectScope);
		List<ReferenceSearchMatch> references = new ArrayList<>();
		String fragmentIdentifier = fragment.getName().getIdentifier();

		SearchRequestor requestor = new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) {
				ReferenceSearchMatch reference = new ReferenceSearchMatch(match, fragmentIdentifier);
				references.add(reference);
				IPath path = match.getResource().getFullPath();
				storePath(path);
			}
		};

		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
					scope, requestor, null);
		} catch (CoreException e) {
			logger.error(e.getMessage());
			references.clear();
		}

		return references;
	}
	
	private void storePath(IPath path) {
		this.targetResources.add(path);
	}
	
	public Set<IPath> getTargetCompilationUnitPaths() {
		return this.targetResources;
	}

	/**
	 * 
	 * @author Ardit Ymeri
	 * @since 2.1.0
	 *
	 */
	private class MetaData implements FieldMetadata {
		private CompilationUnit compilationUnit;
		private List<ReferenceSearchMatch> references;
		private VariableDeclarationFragment declarationFragment;
		private String newIdentifier;
		private TextEditGroup textEditGroup;

		public MetaData(CompilationUnit cu, List<ReferenceSearchMatch> references, VariableDeclarationFragment fragment,
				String newIdentifier) {
			this.compilationUnit = cu;
			this.references = references;
			this.declarationFragment = fragment;
			this.newIdentifier = newIdentifier;
			references.forEach(referece -> referece.setMetadata(this));
			textEditGroup = new TextEditGroup(newIdentifier);
		}

		@Override
		public CompilationUnit getCompilationUnit() {
			return this.compilationUnit;
		}

		@Override
		public VariableDeclarationFragment getFieldDeclaration() {
			return this.declarationFragment;
		}

		@Override
		public List<ReferenceSearchMatch> getReferences() {
			return this.references;
		}

		@Override
		public String getNewIdentifier() {
			return newIdentifier;
		}
		
		@Override
		public TextEditGroup getTextEditGroup() {
			return this.textEditGroup;
		}
	}
}
