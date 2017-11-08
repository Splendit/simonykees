package eu.jsparrow.core.visitor.renaming;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * A visitor for renaming the name of a field and its references. Requires a
 * list of {@link FieldMetadata} for providing information about the fields to
 * be renamed and the compilation units containing references of it.
 * <p/>
 * Creates a {@link TextEditGroup} for storing the all the updates related to
 * one field on a compilation unit. Therefore, the overall changes related to
 * one field will be represented by a list of {@link TextEditGroup}s, each
 * representing the changes in one compilation unit.
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class PublicFieldsRenamingASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String DASH = "-"; //$NON-NLS-1$
	private static final String COMMENT_TEMPLLATE = "Rename %s to comply with naming conventions."; //$NON-NLS-1$
	private static final String TODO_TAG = "TODO"; //$NON-NLS-1$
	private static final String STARTIGN_POSITION = "field-starting-position"; //$NON-NLS-1$

	private Map<String, FieldMetadata> cuRelatedReplacements;
	private Map<String, List<String>> cuRelatedUnmodifiable;
	private List<FieldMetadata> metaData;
	private ICompilationUnit iCompilationUnit;
	private List<FieldMetadata> unmodifiableFields;
	private Map<ICompilationUnit, TextEditGroup> todosEditGroups;

	/**
	 * Creates an instance of a visitor for renaming fields and inserting
	 * comment nodes above the fields that cannot be renamed due to naming
	 * conflicts or illegal identifier names.
	 * 
	 * @param metaData
	 *            metadata for the fields to be renamed
	 * @param unmodifiableFields
	 *            metadata for the fields to insert comments to
	 */
	public PublicFieldsRenamingASTVisitor(List<FieldMetadata> metaData, List<FieldMetadata> unmodifiableFields) {
		this.metaData = metaData;
		this.unmodifiableFields = unmodifiableFields;
		this.todosEditGroups = new HashMap<>();
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.iCompilationUnit = (ICompilationUnit) compilationUnit.getJavaElement();
		this.cuRelatedReplacements = findCuRelatedData(compilationUnit);
		this.cuRelatedReplacements.putAll(findRelatedCuDeclarationFragments(compilationUnit));
		this.cuRelatedUnmodifiable = findCuRelatedUnmodifiable(iCompilationUnit);
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		findReplacement(simpleName).ifPresent(mData -> {
			AST ast = astRewrite.getAST();
			String newIdentifier = mData.getNewIdentifier();
			SimpleName newName = ast.newSimpleName(newIdentifier);
			TextEditGroup editGroup = mData.getTextEditGroup(iCompilationUnit);
			astRewrite.replace(simpleName, newName, editGroup);
		});

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		String fieldKeyId = calcFieldIdentifier(fieldDeclaration);
		if (cuRelatedUnmodifiable.containsKey(fieldKeyId)) {
			List<String> identifiers = cuRelatedUnmodifiable.get(fieldKeyId);
			insertJavadocNode(fieldDeclaration, identifiers);
		}
		return true;
	}

	/**
	 * Insert a {@link Javadoc} node to the given field declaration. The content
	 * of the node is obtained by using the template {@link #COMMENT_TEMPLLATE}
	 * and the given list of identifiers.
	 * 
	 * @param fieldDecl
	 *            field to add the javadoc
	 * @param identifiers
	 *            list of identifiers to be mentioned int the javadoc.
	 */
	private void insertJavadocNode(FieldDeclaration fieldDecl, List<String> identifiers) {

		String fragmentNames = identifiers.stream().collect(Collectors.joining(", ")); //$NON-NLS-1$
		Javadoc javaDoc = fieldDecl.getAST().newJavadoc();

		TagElement tagElement = fieldDecl.getAST().newTagElement();
		TextElement textElement = fieldDecl.getAST().newTextElement();
		textElement.setText(String.format(COMMENT_TEMPLLATE, fragmentNames));
		ListRewrite commentRewriter = astRewrite.getListRewrite(tagElement, TagElement.FRAGMENTS_PROPERTY);

		TextEditGroup editGroup = findTodoEditGroup();
		commentRewriter.insertFirst(textElement, editGroup);
		tagElement.setTagName(TODO_TAG);

		ListRewrite javaDocRewrite = astRewrite.getListRewrite(javaDoc, Javadoc.TAGS_PROPERTY);
		javaDocRewrite.insertFirst(tagElement, editGroup);

		astRewrite.set(fieldDecl, FieldDeclaration.JAVADOC_PROPERTY, javaDoc, editGroup);
	}

	/**
	 * Creates or finds the {@link TextEditGroup} storing the comments to be
	 * added in this current compilation unit. If the {@code TextEditGroup} is
	 * newly created, then it is also stored in the {@link #todosEditGroups}
	 * 
	 * @return the found/created TextEditGroup.
	 */
	private TextEditGroup findTodoEditGroup() {
		TextEditGroup group;
		if (this.todosEditGroups.containsKey(this.iCompilationUnit)) {
			group = todosEditGroups.get(this.iCompilationUnit);
		} else {
			group = new TextEditGroup(this.iCompilationUnit.getResource().getName());
			todosEditGroups.put(this.iCompilationUnit, group);
		}
		return group;
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
		metaData.stream().filter(mData -> {
			IPath originDeclarationPath = mData.getCompilationUnit().getJavaElement().getPath();
			IPath path = compilationUnit.getJavaElement().getPath();
			return matchingIPaths(originDeclarationPath, path);
		}).forEach(mData -> {
			VariableDeclarationFragment fragment = mData.getFieldDeclaration();
			SimpleName oldName = fragment.getName();
			declarations.put(calcIdentifier(oldName), mData);
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
	 * Finds the list of the metadata related to that contain a
	 * {@link ReferenceSearchMatch} falling in the given compilation unit.
	 * 
	 * @param cu
	 *            compilation unit to search for.
	 * @return a map representing the all the meta data that are related to the
	 *         fields referenced in the given compilation unit. Makes use of
	 *         {@link #calcIdentifier(SimpleName)} for computing a unique
	 *         identifier of a field.
	 */
	private Map<String, FieldMetadata> findCuRelatedData(CompilationUnit cu) {
		IResource cuResource = cu.getJavaElement().getResource();
		List<ReferenceSearchMatch> relatedcuReferences = metaData.stream()
				.flatMap(mData -> mData.getReferences().stream()).filter(match -> isMatchingResource(cuResource, match))
				.collect(Collectors.toList());
		Map<String, FieldMetadata> oldToNewKeys = new HashMap<>();
		relatedcuReferences.forEach(match -> {
			FieldMetadata relatedMatchData = match.getMetadata();
			String oldName = match.getMatchedName();
			oldToNewKeys.put(calcIdentifier(oldName, match.getOffset()), relatedMatchData);
		});

		return oldToNewKeys;
	}

	/**
	 * Finds the list of the fields that are declared in the given compilation
	 * unit and a comment node have to be added to.
	 * 
	 * @param compilationUnit
	 *            the compilation unit to find the fields from
	 * @return a map of the starting position of the field declaration to the
	 *         list of the declared identifiers that need to be mentioned in the
	 *         comment node.
	 */
	private Map<String, List<String>> findCuRelatedUnmodifiable(ICompilationUnit compilationUnit) {
		IPath currentPath = compilationUnit.getPath();
		Map<String, List<String>> data = new HashMap<>();

		unmodifiableFields.stream().filter(mData -> {
			CompilationUnit cu = mData.getCompilationUnit();
			return matchingIPaths(cu.getJavaElement().getPath(), currentPath);
		}).forEach(mData -> {
			FieldDeclaration field = (FieldDeclaration) mData.getFieldDeclaration().getParent();
			String key = calcFieldIdentifier(field);
			if (data.containsKey(key)) {
				List<String> fragmentNames = data.get(key);
				fragmentNames.add(mData.getNewIdentifier());
			} else {
				List<String> fragmentNames = new ArrayList<>();
				fragmentNames.add(mData.getNewIdentifier());
				data.put(key, fragmentNames);
			}
		});

		return data;
	}

	/**
	 * Checks whether the given {@link SearchMatch} falls into the given
	 * {@link IResource}.
	 * 
	 * @param cuResource
	 *            the resource to check for.
	 * @param match
	 *            a search match
	 * @return {@code true} if the match belongs to the given resource or
	 *         {@code false} otherwise.
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
	 * Uses {@link #calcIdentifier(String, int)} for computing a unique key
	 * identifier for a given simple name. The identifier will be unique per
	 * compilation unit.
	 * 
	 * @param name
	 *            a simple name to generate a key identifier for.
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

	private String calcFieldIdentifier(int startingPosition) {
		return STARTIGN_POSITION + ":" + startingPosition; //$NON-NLS-1$
	}

	private String calcFieldIdentifier(FieldDeclaration field) {
		return calcFieldIdentifier(field.getStartPosition());
	}
}
