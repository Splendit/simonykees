package at.splendit.simonykees.core.visitor.renaming;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.search.SearchMatch;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class PublicFieldsRenamingASTVisitor extends AbstractASTRewriteASTVisitor {

	private CompilationUnit referenceCompilationUnit;
	private String newIdentifier;
	private VariableDeclarationFragment originDeclaration;
	private List<SearchMatch> referenes;
	private List<SearchMatch> referencesOnCompilationUnit;
	private CompilationUnit compilationUnit;

	public PublicFieldsRenamingASTVisitor(FieldDeclarationMetadata metaData) {
		this.referenceCompilationUnit = metaData.getCompilationUnit();
		this.newIdentifier = metaData.getNewIdentifier();
		this.originDeclaration = metaData.getFieldDeclaration();
		this.referenes = metaData.getReferences();
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.referencesOnCompilationUnit = findReferencesRelatedToCompilationUnit(compilationUnit);
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (IBinding.VARIABLE == binding.getKind() && isReference(simpleName)) {
			AST ast = astRewrite.getAST();
			SimpleName newName = ast.newSimpleName(newIdentifier);
			astRewrite.replace(simpleName, newName, null);
		}

		return true;
	}
	
	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		IPath cuPath = compilationUnit.getJavaElement().getPath();
		IPath cuOriginPath = referenceCompilationUnit.getJavaElement().getPath();

		if (matchingIPaths(cuPath, cuOriginPath)) {
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
					VariableDeclarationFragment.class);
			fragments.stream().filter(
					fragment -> fragment.getName().getIdentifier().equals(originDeclaration.getName().getIdentifier()))
					.filter(fragment -> fragment.getStartPosition() == originDeclaration.getStartPosition())
					.forEach(fragment -> {
						/*
						 * Renaming the declaration itself. 
						 */
						astRewrite.replace(fragment.getName(), astRewrite.getAST().newSimpleName(newIdentifier), null);
					});
		}
		return true;
	}
	
	private boolean matchingIPaths(IPath cuPath, IPath cuOriginPath) {
		return cuPath.toString().equals(cuOriginPath.toString());
	}

	private List<SearchMatch> findReferencesRelatedToCompilationUnit(CompilationUnit cu) {
		IResource cuResource = cu.getJavaElement().getResource();
		return referenes.stream().filter(match -> isMatchingResource(cuResource, match))
				.collect(Collectors.toList());
	}

	private boolean isMatchingResource(IResource cuResource, SearchMatch match) {
		IResource resource = match.getResource();
		return resource.getFullPath().toString().equals(cuResource.getFullPath().toString());
	}

	private boolean isReference(SimpleName simpleName) {
		int startingPosition = simpleName.getStartPosition();
		int length = simpleName.getLength();
		String simpleNameId = simpleName.getIdentifier();
		String originId = originDeclaration.getName().getIdentifier();
		return originId.equals(simpleNameId) && referencesOnCompilationUnit.stream()
				.anyMatch(match -> (match.getOffset() == startingPosition) && (match.getLength() == length));
	}
	
	/*
	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		for (VariableDeclarationFragment fragment : fragments) {
			IJavaElement iVariableBinding = fragment.resolveBinding().getJavaElement();

			IField iField = (IField) iVariableBinding;
			SearchPattern searchPattern = SearchPattern.createPattern(iField, IJavaSearchConstants.REFERENCES);
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

			List<String> references = new ArrayList<>();

			SearchRequestor requestor = new SearchRequestor() {

				@Override
				public void acceptSearchMatch(SearchMatch match) throws CoreException {
					int offset = match.getOffset();
					int length = match.getLength();
					IResource resource = match.getResource();
					references.add(match.getElement().toString());
					IJavaElement javaElement = JavaCore.create(resource);

					RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
					refactoringPipeline.clearStates();
					refactoringPipeline.setRules(
							Collections.singletonList(new RenameRule(RenameVisitor.class, offset, length, astRewrite)));
					try {
						refactoringPipeline.prepareRefactoring(Collections.singletonList(javaElement), null);
						refactoringPipeline.doRefactoring(null);
						refactoringPipeline.commitRefactoring();
						refactoringPipeline.clearStates();
					} catch (RefactoringException | RuleException e) {
						e.printStackTrace();
					} catch (ReconcileException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			};

			SearchEngine searchEngine = new SearchEngine();
			try {
				searchEngine.search(searchPattern,
						new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}
		return true;
	}
	*/

	public class RenameRule extends RefactoringRule<RenameVisitor> {

		private int length;
		private int offset;
		private ASTRewrite rewriter;

		public RenameRule(Class<RenameVisitor> visitor, int offSet, int length, ASTRewrite rewriter) {
			super(visitor);
			this.name = "renamer";
			this.description = "description";
			this.offset = offSet;
			this.length = length;
			this.rewriter = rewriter;
		}

		@Override
		protected JavaVersion provideRequiredJavaVersion() {
			return JavaVersion.JAVA_1_1;
		}

		@Override
		public RenameVisitor visitorFactory() {
			return new RenameVisitor(rewriter, length, offset);
		}
	}

	class RenameVisitor extends AbstractASTRewriteASTVisitor {
		private int length;
		private int offset;
		// TODO: it will be a pain to check if the new identifier is safe!!!
		private List<SimpleName> references = new ArrayList<>();

		public RenameVisitor(ASTRewrite rewriter, int length, int offset) {
			this.astRewrite = rewriter;
			this.length = length;
			this.offset = offset;
		}

		@Override
		public boolean visit(SimpleName name) {
			if (name.getStartPosition() == offset && name.getLength() == length) {
				references.add(name);
				astRewrite.replace(name, astRewrite.getAST().newSimpleName("newId"), null);
			}
			return true;
		}

		public List<SimpleName> getReferences() {
			return this.references;
		}
	}

}
