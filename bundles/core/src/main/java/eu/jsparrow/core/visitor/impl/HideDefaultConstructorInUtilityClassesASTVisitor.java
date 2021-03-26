package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThrowStatement;
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
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor checks, if the current class is a utility class. This is defined
 * by the following:
 * <ul>
 * <li>no constructor is declared</li>
 * <li>only static methods are defined</li>
 * <li>a main method isn't present</li>
 * <li>the default constructor isn't invoked anywhere</li>
 * </ul>
 * If it is a utility class, the default constructor will be hidden by
 * introducing a private constructor, which throws an
 * {@link IllegalStateException} should it be invoked.
 *
 * @since 3.11.0
 */
@SuppressWarnings({ "unchecked", "nls" })
public class HideDefaultConstructorInUtilityClassesASTVisitor extends AbstractASTRewriteASTVisitor {

	public static final Logger logger = LoggerFactory.getLogger(HideDefaultConstructorInUtilityClassesASTVisitor.class);

	private static final String EXCEPTION_MESSAGE = "Utility class";

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		if (typeDeclaration.getFields().length == 0 && typeDeclaration.getMethods().length == 0) {
			return false;
		}

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

		AST ast = astRewrite.getAST();
		String classNameString = typeDeclaration.getName()
			.getIdentifier();
		SimpleName className = ast.newSimpleName(classNameString);
		Modifier privateModifier = ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
		Type illegalStateExceptionType = ast.newSimpleType(ast.newName("IllegalStateException"));

		StringLiteral exceptionMessageLiteral = ast.newStringLiteral();
		exceptionMessageLiteral.setLiteralValue(EXCEPTION_MESSAGE);

		ClassInstanceCreation classInstanceCreation = ast.newClassInstanceCreation();
		classInstanceCreation.setType(illegalStateExceptionType);
		astRewrite.getListRewrite(classInstanceCreation, ClassInstanceCreation.ARGUMENTS_PROPERTY)
			.insertFirst(exceptionMessageLiteral, null);

		ThrowStatement throwStatement = ast.newThrowStatement();
		throwStatement.setExpression(classInstanceCreation);

		Block throwBody = ast.newBlock();
		astRewrite.getListRewrite(throwBody, Block.STATEMENTS_PROPERTY)
			.insertFirst(throwStatement, null);

		MethodDeclaration privateConstructor = astRewrite.getAST()
			.newMethodDeclaration();
		privateConstructor.setConstructor(true);
		privateConstructor.setName(className);
		privateConstructor.modifiers()
			.add(privateModifier);
		privateConstructor.setBody(throwBody);

		astRewrite.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY)
			.insertFirst(privateConstructor, null);

		onRewrite();

		return false;
	}

	private boolean isValidUtilityClass(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isInterface()) {
			return false;
		}

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

		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();

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

		@SuppressWarnings("rawtypes")
		List modifiers = methodDeclaration.modifiers();
		if (!ASTNodeUtil.hasModifier(modifiers, Modifier::isStatic)
				|| !ASTNodeUtil.hasModifier(modifiers, Modifier::isPublic)) {
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

		return ClassRelationUtil.isContentOfType(paramTypeBinding.getElementType(), java.lang.String.class.getName())
				&& paramTypeBinding.getDimensions() == 1;
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
