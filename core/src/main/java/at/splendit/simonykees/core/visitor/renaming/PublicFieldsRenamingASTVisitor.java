package at.splendit.simonykees.core.visitor.renaming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.text.edits.TextEditGroup;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class PublicFieldsRenamingASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String DASH = "-"; //$NON-NLS-1$
	
	private Map<String, FieldMetadata> cuRelatedReplacements;
	private List<FieldMetadata> metaData;

	public PublicFieldsRenamingASTVisitor(List<FieldMetadata> metaData) {
		this.metaData = metaData;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.cuRelatedReplacements = findCuRelatedData(compilationUnit);
		this.cuRelatedReplacements.putAll(findRelatedCuDeclarationFragments(compilationUnit));
		return true;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (IBinding.VARIABLE == binding.getKind()) {
			findReplacement(simpleName).ifPresent(metaData -> {
				AST ast = astRewrite.getAST();
				String newIdentifier = metaData.getNewIdentifier();
				SimpleName newName = ast.newSimpleName(newIdentifier);
				TextEditGroup editGroup = metaData.getTextEditGroup();
				astRewrite.replace(simpleName, newName, editGroup);
			});			
		}

		return true;
	}

	/**
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private Map<String, FieldMetadata> findRelatedCuDeclarationFragments(CompilationUnit compilationUnit) {
		
		Map<String, FieldMetadata> declarations = new HashMap<>();
		metaData.stream().filter(metaData -> {
			IPath originDeclarationPath = metaData.getCompilationUnit().getJavaElement().getPath();
			IPath path = compilationUnit.getJavaElement().getPath();
			return matchingIPaths(originDeclarationPath, path);
		}).forEach(metaData -> {
			VariableDeclarationFragment fragment = metaData.getFieldDeclaration();
			SimpleName oldName = fragment.getName();
			declarations.put(calcIdentifier(oldName), metaData);
		});
		
		return declarations;
	}
	
	/**
	 * 
	 * @param cuPath
	 * @param cuOriginPath
	 * @return
	 */
	private boolean matchingIPaths(IPath cuPath, IPath cuOriginPath) {
		return cuPath.toString().equals(cuOriginPath.toString());
	}
	
	/**
	 * 
	 * @param cu
	 * @return
	 */
	private Map<String, FieldMetadata> findCuRelatedData(CompilationUnit cu) {
		IResource cuResource = cu.getJavaElement().getResource();
		List<ReferenceSearchMatch> relatedcuReferences = metaData.stream().flatMap(metaData -> metaData.getReferences().stream()).
				filter(match -> isMatchingResource(cuResource, match)).collect(Collectors.toList());
		Map<String, FieldMetadata> oldToNewKeys = new HashMap<>();
		relatedcuReferences.forEach(match -> {
			FieldMetadata relatedMatchData = match.getMetadata();
			String oldName = match.getMatchedName();
			oldToNewKeys.put(calcIdentifier(oldName, match.getOffset()), relatedMatchData);
		});
		
		return oldToNewKeys;
	}

	/**
	 * 
	 * @param cuResource
	 * @param match
	 * @return
	 */
	private boolean isMatchingResource(IResource cuResource, SearchMatch match) {
		IResource resource = match.getResource();
		return resource.getFullPath().toString().equals(cuResource.getFullPath().toString());
	}
	
	/**
	 * 
	 * @param simpleName
	 * @return
	 */
	private Optional<FieldMetadata> findReplacement(SimpleName simpleName) {
		String nameIdentifier = calcIdentifier(simpleName);
		if(cuRelatedReplacements.containsKey(nameIdentifier)) {
			FieldMetadata replacement = cuRelatedReplacements.get(nameIdentifier);
			return Optional.of(replacement);
		}
		return Optional.empty();
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	private String calcIdentifier(SimpleName name) {
		return calcIdentifier(name.getIdentifier(), name.getStartPosition());
	}
	
	/**
	 * 
	 * @param identifier
	 * @param startingPosition
	 * @return
	 */
	private String calcIdentifier(String identifier, int startingPosition) {
		return identifier + DASH + startingPosition;
	}
}
