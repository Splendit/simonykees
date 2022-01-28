package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.renaming.FieldReferencesSearch;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

public class UnusedFieldsEngine {
	
	private static final Logger logger = LoggerFactory.getLogger(UnusedFieldsEngine.class);
	
	/*
	 * - Gets the list of the selected sources. 
	 * - Uses UnusedFieldsCandidatesVisitor to make a list of potential fields to be removed. 
	 * 	 -- the unused private fields are immediately classified to be removed. 
	 *   -- for non-private field candidates, we need to analyze the search scope. Either with the eclipse search engine, or by implementing our own search engine. 
	 *   -- As a result, should get a list of 'UnusedFieldWrapper' that contains all the information about the fields that should be removed. 
	 *  
	 * - From the list of 'UnusedFieldWrapper', prepare the list of relevant compilation units. 
	 * - Use the 'RemoveUnusedFieldsASTVisitor' to remove the unused fields. 
	 */

	private String scope;
	private Set<ICompilationUnit> targetCompilationUnits = new HashSet<>();
	
	public UnusedFieldsEngine(String scope) {
		this.scope = scope;
	}

	public List<UnusedFieldWrapper> findUnusedFields(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap, SubMonitor subMonitor) {
		List<CompilationUnit> compilationUnits = new ArrayList<>();
		List<UnusedFieldWrapper> list = new ArrayList<>();
		for (ICompilationUnit icu : selectedJavaElements) {
			CompilationUnit cu = RefactoringUtil.parse(icu);
			compilationUnits.add(cu);

			UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(optionsMap);
			cu.accept(visitor);
			List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
			if(!unusedPrivateFields.isEmpty()) {
				list.addAll(unusedPrivateFields);
				targetCompilationUnits.add(icu);
			}

			List<VariableDeclarationFragment> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<SimpleName> internalReassignments = visitor.getInternalReassignments();
			List<UnusedFieldWrapper> nonPrivate = findExternalUnusedReferences(cu, internalReassignments, nonPrivateCandidates);
			if (!nonPrivate.isEmpty()) {
				targetCompilationUnits.add(icu);
			}
		
			for(UnusedFieldWrapper unused : nonPrivate) {
				List<UnusedExternalReferences> externalReferences = unused.getUnusedExternalReferences();
				for(UnusedExternalReferences r : externalReferences) {
					// TOOD: taking target compilation units from a cache is going to be a lot easier.
					targetCompilationUnits.add((ICompilationUnit) r.getCompilationUnit().getJavaElement());
				}
			}
			list.addAll(nonPrivate);
			
		}
		return list;
	}
	
	private List<UnusedFieldWrapper> findExternalUnusedReferences(CompilationUnit compilationUnit, List<SimpleName>internalReassignments, List<VariableDeclarationFragment> nonPrivateCandidates) {
		List<UnusedFieldWrapper> list = new ArrayList<>();
		/* 
		 * Get the compilation units of the search scope. 
		 * Parse them and find the external references. Existing search engine? Or our own?
		 * 		--> Maybe implement both, and figure out which one performs faster? Also which one is more accurate. 
		 * Put all the reference in UnusedFieldWrapper and add it to the resulting list. 
		 */
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		for(VariableDeclarationFragment fragment : nonPrivateCandidates) { 
			UnusedFieldReferenceSearchResult searchResult = searchReferences(fragment, javaProject);
			if(!searchResult.isActiveReferenceFound() && !searchResult.isInvalidSearchEngineResult()) {
				List<UnusedExternalReferences> unusedReferences = searchResult.getUnusedReferences();
				UnusedFieldWrapper unusedFieldWrapper = new UnusedFieldWrapper(fragment, internalReassignments, unusedReferences);
				list.add(unusedFieldWrapper);
			}
		}
		return list;
	}
	
	public UnusedFieldReferenceSearchResult searchReferences(VariableDeclarationFragment fragment, IJavaProject project) {
		IJavaElement[] searchScope = createSearchScope(scope, project);
		FieldReferencesSearch fieldReferencesSearchEngine = new FieldReferencesSearch(searchScope);
		Optional<List<ReferenceSearchMatch>> references = fieldReferencesSearchEngine.findFieldReferences(fragment);
		if(!references.isPresent()) {
			return new UnusedFieldReferenceSearchResult(false, true, Collections.emptyList());
		}
		Set<ICompilationUnit> targetICUs = fieldReferencesSearchEngine.getTargetIJavaElements();
		/*
		 * Make a cache with parsed compilation units. 
		 * Keep  all the icu-s in a targetCompilationUnits field. 
		 */
		TypeDeclaration typDeclaration = ASTNodeUtil.getSpecificAncestor(fragment, TypeDeclaration.class);
		List<UnusedExternalReferences> unusedExternalreferences = new ArrayList<>();
		for(ICompilationUnit icu : targetICUs) {
			CompilationUnit cu = RefactoringUtil.parse(icu);
			ReferencesVisitor visitor = new ReferencesVisitor(fragment, typDeclaration);
			cu.accept(visitor);
			if(!visitor.hasActiveReference()) {
				List<SimpleName> reassignments = visitor.getReassignments();
				UnusedExternalReferences unusedReferences = new UnusedExternalReferences(cu, reassignments);
				unusedExternalreferences.add(unusedReferences);
			} else {
				return new UnusedFieldReferenceSearchResult(true, false, Collections.emptyList());
			}
		}
		return new UnusedFieldReferenceSearchResult(false, false, unusedExternalreferences);
	}

	private IJavaElement[] createSearchScope(String modelSearchScope, IJavaProject javaProject) {
		
		// FIXME this code is repeated

		if ("Project".equalsIgnoreCase(modelSearchScope)) { //$NON-NLS-1$
			return new IJavaElement[] { javaProject };
		}

		List<IJavaProject> projectList = new LinkedList<>();
		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
				.getRoot();
			IProject[] projects = workspaceRoot.getProjects();
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
					projectList.add(JavaCore.create(project));
				}
			}
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
		}
		return projectList.toArray(new IJavaElement[0]);
	}

	public Set<ICompilationUnit> getTargetCompilationUnits() {
		return targetCompilationUnits;
	}
	

}
