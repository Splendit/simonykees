package at.splendit.simonykees.core.visitor.renaming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.text.edits.TextEditGroup;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * A visitor for renaming the name of a field and its references. Requires a list of
 * {@link FieldMetadata} for providing information about the fields to be
 * renamed and the compilation units containing references of it.
 * <p/>
 * Creates a {@link TextEditGroup} for storing the all the updates related to
 * one field on a compilation unit. Therefore, the overall changes related to
 * one field will be represented by a list of {@link TextEditGroup}s, each
 * representing the changes in one compilation unit.
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class PublicFieldsRenamingASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String DASH = "-"; //$NON-NLS-1$

	private Map<String, FieldMetadata> cuRelatedReplacements;
	private List<FieldMetadata> metaData;
	private ICompilationUnit iCompilationUnit;

	public PublicFieldsRenamingASTVisitor(List<FieldMetadata> metaData) {
		this.metaData = metaData;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.iCompilationUnit = (ICompilationUnit) compilationUnit.getJavaElement();
		this.cuRelatedReplacements = findCuRelatedData(compilationUnit);
		this.cuRelatedReplacements.putAll(findRelatedCuDeclarationFragments(compilationUnit));
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		findReplacement(simpleName).ifPresent(metaData -> {
			AST ast = astRewrite.getAST();
			String newIdentifier = metaData.getNewIdentifier();
			SimpleName newName = ast.newSimpleName(newIdentifier);
			TextEditGroup editGroup = metaData.getTextEditGroup(iCompilationUnit);
			astRewrite.replace(simpleName, newName, editGroup);
		});

		return true;
	}

	/**
	 * Finds the metadata representing informations about the fields which are
	 * declared in the given compilation unit. Compares the {@link IPath}s of
	 * the given compilation unit and the compilation unit related to the
	 * metadata.
	 * 
	 * @param compilationUnit
	 *            the compilation unit to search for.
	 * @return a map representing the all the meta data that are related to the
	 *         fields declared in the given compilation unit. Makes use of
	 *         {@link #calcIdentifier(SimpleName)} for computing a unique
	 *         identifier of a field.
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
	 * Checks whether the given {@link IPath}s point to the same file.
	 * 
	 * @param cuPath
	 *            source path
	 * @param cuOriginPath
	 *            target path
	 * @return {@code true} if the paths point to the same file or {@code false}
	 *         otherwise.
	 */
	private boolean matchingIPaths(IPath cuPath, IPath cuOriginPath) {
		return cuPath.toString().equals(cuOriginPath.toString());
	}

	/**
	 * Finds the list of the metadata related to that contain a {@link ReferenceSearchMatch}
	 * falling in the given compilation unit. 
	 * 
	 * @param cu compilation unit to search for.
	 * @return a map representing the all the meta data that are related to the
	 *         fields referenced in the given compilation unit. Makes use of
	 *         {@link #calcIdentifier(SimpleName)} for computing a unique
	 *         identifier of a field.
	 */
	private Map<String, FieldMetadata> findCuRelatedData(CompilationUnit cu) {
		IResource cuResource = cu.getJavaElement().getResource();
		List<ReferenceSearchMatch> relatedcuReferences = metaData.stream()
				.flatMap(metaData -> metaData.getReferences().stream())
				.filter(match -> isMatchingResource(cuResource, match)).collect(Collectors.toList());
		Map<String, FieldMetadata> oldToNewKeys = new HashMap<>();
		relatedcuReferences.forEach(match -> {
			FieldMetadata relatedMatchData = match.getMetadata();
			String oldName = match.getMatchedName();
			oldToNewKeys.put(calcIdentifier(oldName, match.getOffset()), relatedMatchData);
		});

		return oldToNewKeys;
	}

	/**
	 * Checks whether the given {@link SearchMatch} falls into the given {@link IResource}. 
	 * 
	 * @param cuResource the resource to check for. 
	 * @param match a search match
	 * @return {@code true} if the match belongs to the given resource or {@code false} otherwise.
	 */
	private boolean isMatchingResource(IResource cuResource, SearchMatch match) {
		IResource resource = match.getResource();
		return resource.getFullPath().toString().equals(cuResource.getFullPath().toString());
	}

	/**
	 * Uses {@link #calcIdentifier(SimpleName)} to compute a unique key for a
	 * given simple name, and checks whether there is an object with the
	 * computed key in the map {@link #cuRelatedReplacements}.
	 * 
	 * @param simpleName
	 *            a simple name to check if there is a replacement recorded for
	 *            it.
	 * @return an optional of {@link FieldMetadata} that contains the relevant
	 *         information for the replacement, or an empty optional if no
	 *         replacement information is stored for the given simple name.
	 */
	private Optional<FieldMetadata> findReplacement(SimpleName simpleName) {
		String nameIdentifier = calcIdentifier(simpleName);
		if (cuRelatedReplacements.containsKey(nameIdentifier)) {
			FieldMetadata replacement = cuRelatedReplacements.get(nameIdentifier);
			return Optional.of(replacement);
		}
		return Optional.empty();
	}

	/**
	 * Uses {@link #calcIdentifier(String, int)} for computing a unique key identifier 
	 * for a given simple name. The identifier will be unique per compilation unit. 
	 * 
	 * @param name a simple name to generate a key identifier for.
	 * @return a key identifier
	 */
	private String calcIdentifier(SimpleName name) {
		return calcIdentifier(name.getIdentifier(), name.getStartPosition());
	}

	/**
	 * Joins the given identifier with the given integer by using the
	 * {@value #DASH} symbol.
	 * 
	 * @param identifier
	 *            a string representing the identifier of a simple name.
	 * @param startingPosition
	 *            a number which is unique for each simple name occurring on a
	 *            compilation unit.
	 * @return the joined string.
	 */
	private String calcIdentifier(String identifier, int startingPosition) {
		return identifier + DASH + startingPosition;
	}
}
