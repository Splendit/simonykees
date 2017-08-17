package at.splendit.simonykees.core.visitor.renaming;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class FieldDeclarationASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private CompilationUnit compilationUnit;
	private List<FieldDeclarationMetadata> fieldsMetaData = new ArrayList<>();
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
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
		
		if(ASTNodeUtil.hasModifier(modifiers, Modifier::isPrivate)) {
			/**
			 * private fields are handled in {@link FieldNameConventionASTVisitor}.
			 */
			return true;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		for (VariableDeclarationFragment fragment : fragments) {
			SimpleName fragmentName = fragment.getName();
			if (!NamingConventionUtil.isComplyingWithConventions(fragmentName.getIdentifier())) {
				NamingConventionUtil.generateNewIdetifier(fragmentName.getIdentifier())
				.filter(newIdentifier -> !isConflictingIdentifier(newIdentifier))
				.ifPresent(newIdentifier -> {
					List<SearchMatch> references = findFieldReferences(fragment);
					fieldsMetaData.add(new MetaData(compilationUnit, references, fragment, newIdentifier));
				});
			}
		}
		
		return true;
	}
	
	private boolean isConflictingIdentifier(String newIdentifier) {
		/*
		 * 1. check the rest of field's names (can potentially be outer classes)
		 * 2. check the static imports
		 * 3. check the local variables
		 */
		return false;
	}

	public List<FieldDeclarationMetadata> getFieldMetadata() {
		return this.fieldsMetaData;
	}

	private List<SearchMatch> findFieldReferences(VariableDeclarationFragment fragment) {
		IJavaElement iVariableBinding = fragment.resolveBinding().getJavaElement();
		
		IField iField = (IField)iVariableBinding;
		SearchPattern searchPattern = SearchPattern.createPattern(iField, IJavaSearchConstants.REFERENCES);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		List<SearchMatch> references = new ArrayList<>();
		
		SearchRequestor requestor = new SearchRequestor() {
			
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				references.add(match);
			}
		};
		
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(searchPattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, null);
		} catch (CoreException e) {
			//TODO: throw the exception. the renaming is not possible.
			e.printStackTrace();
		}
		
		return references;
	}
	
	private class MetaData implements FieldDeclarationMetadata {
		private CompilationUnit compilationUnit;
		private List<SearchMatch> references;
		private VariableDeclarationFragment declarationFragment;
		private String newIdentifier;
		
		public MetaData(CompilationUnit cu, List<SearchMatch> references, VariableDeclarationFragment fragment, String newIdentifier) {
			this.compilationUnit = cu;
			this.references = references;
			this.declarationFragment = fragment;
			this.newIdentifier = newIdentifier;
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
		public List<SearchMatch> getReferences() {
			return this.references;
		}

		@Override
		public String getNewIdentifier() {
			return newIdentifier;
		}
	}
}
