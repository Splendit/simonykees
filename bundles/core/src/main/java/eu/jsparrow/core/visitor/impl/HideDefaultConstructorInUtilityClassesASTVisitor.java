package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

@SuppressWarnings({ "unchecked", "nls" })
public class HideDefaultConstructorInUtilityClassesASTVisitor extends AbstractASTRewriteASTVisitor {

	public static final Logger logger = LoggerFactory.getLogger(HideDefaultConstructorInUtilityClassesASTVisitor.class);

	IJavaElement[] searchScopeArray;

	public HideDefaultConstructorInUtilityClassesASTVisitor() {

	}

	protected HideDefaultConstructorInUtilityClassesASTVisitor(IJavaElement[] searchScope) {
		this.searchScopeArray = searchScope;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		if (!isValidUtilityClass(typeDeclaration)) {
			return false;
		}

		boolean isDefaultConstructorInvoked = true;
		try {
			isDefaultConstructorInvoked = hasDefaultConstructorInvocations(typeDeclaration);
		} catch (CoreException e) {
			logger.debug("Exception while searching for invocations of the default constructor: " + e.getMessage(), e);
			return false;
		}

		if (isDefaultConstructorInvoked) {
			return false;
		}

		String classNameString = typeDeclaration.getName()
			.getIdentifier();
		SimpleName className = astRewrite.getAST()
			.newSimpleName(classNameString);
		Modifier privateModifier = astRewrite.getAST()
			.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
		Block emptyBody = astRewrite.getAST()
			.newBlock();

		MethodDeclaration privateConstructor = astRewrite.getAST()
			.newMethodDeclaration();
		privateConstructor.setConstructor(true);
		privateConstructor.setName(className);
		privateConstructor.modifiers()
			.add(privateModifier);
		privateConstructor.setBody(emptyBody);

		astRewrite.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY)
			.insertFirst(privateConstructor, null);

		onRewrite();

		return false;
	}

	private boolean isValidUtilityClass(TypeDeclaration typeDeclaration) {
		MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();

		boolean isMethodsValid = Arrays.stream(methodDeclarations)
			.allMatch((MethodDeclaration method) -> {
				boolean isConstructor = method.isConstructor();
				boolean isStatic = Modifier.isStatic(method.getModifiers());

				return !isConstructor && isStatic && !isMainMethod(method);
			});

		FieldDeclaration[] fieldDeclarations = typeDeclaration.getFields();
		boolean isFieldsValid = Arrays.stream(fieldDeclarations)
			.allMatch((FieldDeclaration field) -> Modifier.isStatic(field.getModifiers()));

		return isMethodsValid && isFieldsValid;
	}

	private boolean hasDefaultConstructorInvocations(TypeDeclaration typeDeclaration) throws CoreException {
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		String qualifiedName = typeBinding.getQualifiedName();

		List<SearchMatch> matches = new LinkedList<>();

		SearchPattern searchPattern = SearchPattern.createPattern(qualifiedName, IJavaSearchConstants.CONSTRUCTOR,
				IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);

		IJavaSearchScope searchScope = createSearchScope();

		SearchRequestor searchRequestor = createSearchRequestor(matches);

		SearchEngine searchEngine = new SearchEngine();
		searchEngine.search(searchPattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
				searchScope, searchRequestor, null);

		return !matches.isEmpty();
	}

	private boolean isMainMethod(MethodDeclaration methodDeclaration) {
		if (!"main".equals(methodDeclaration.getName()
			.getIdentifier())) {
			return false;
		}

		int modifiers = methodDeclaration.getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			return false;
		}

		Type t = methodDeclaration.getReturnType2();
		ITypeBinding returnTypeBinding = t.resolveBinding();
		if (!"void".equals(returnTypeBinding.getName())) {
			return false;
		}

		List<VariableDeclaration> params = ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(),
				VariableDeclaration.class);

		if (params.size() != 1) {
			return false;
		}

		VariableDeclaration param = params.get(0);
		IVariableBinding paramVariableBinding = param.resolveBinding();
		ITypeBinding paramTypeBinding = paramVariableBinding.getType();

		if (!paramTypeBinding.isArray()) {
			return false;
		}

		if (!"java.lang.String[]".equals(paramTypeBinding.getQualifiedName())) {
			return false;
		}

		return true;
	}

	private IJavaSearchScope createSearchScope() {
		if (searchScopeArray != null && searchScopeArray.length != 0) {
			return SearchEngine.createJavaSearchScope(searchScopeArray);
		} else {
			return SearchEngine.createWorkspaceScope();
		}
	}

	private SearchRequestor createSearchRequestor(List<SearchMatch> matches) {
		return new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				matches.add(match);
			}
		};
	}
}
