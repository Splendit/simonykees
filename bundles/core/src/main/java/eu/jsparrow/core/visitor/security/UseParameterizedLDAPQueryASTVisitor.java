package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Replaces potential user supplied input concatenated into an LDAP search
 * filter by parameterizing, for example:
 * 
 * <pre>
 * String filter = "(&(uid=" + user + ")(userPassword=" + pass + "))";
 * NamingEnumeration<SearchResult> results = ctx.search("ou=system", filter, new SearchControls());
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * String filter = "(&(uid={0})(userPassword={1}))";
 * NamingEnumeration<SearchResult> results = ctx.search("ou=system", filter, new String[] { user, pass },
 * 		new SearchControls());
 * </pre>
 * 
 * @since 3.19.0
 *
 */
public class UseParameterizedLDAPQueryASTVisitor extends AbstractDynamicQueryASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		DirContextSearchInvocationAnalyzer invocationAnalyzer = new DirContextSearchInvocationAnalyzer(
				methodInvocation);
		if (!invocationAnalyzer.analyze()) {
			return true;
		}

		Expression filterExpression = invocationAnalyzer.getFilterExpression();

		List<Expression> dynamicQueryComponents = findDynamicQueryComponents(filterExpression);

		LDAPQueryComponentAnalyzer componentsAnalyzer = new LDAPQueryComponentAnalyzer(dynamicQueryComponents);
		List<ReplaceableParameter> replaceableParameters = componentsAnalyzer.createReplaceableParameterList();
		if (replaceableParameters.isEmpty()) {
			return true;
		}

		// perform replacement
		replaceQuery(replaceableParameters);
		ArrayCreation searchParameters = createSearchParameters(replaceableParameters);
		ListRewrite listRewrite = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		listRewrite.insertAfter(searchParameters, filterExpression, null);

		onRewrite();

		return true;
	}

	@SuppressWarnings("unchecked")
	private ArrayCreation createSearchParameters(List<ReplaceableParameter> replaceableParameters) {
		AST ast = astRewrite.getAST();
		ArrayCreation arrayCreation = ast.newArrayCreation();

		SimpleType elementType = ast.newSimpleType(ast.newSimpleName(Object.class.getSimpleName()));
		ArrayType stringArrayType = ast.newArrayType(elementType);
		arrayCreation.setType(stringArrayType);
		ArrayInitializer initializer = ast.newArrayInitializer();
		for (ReplaceableParameter parameter : replaceableParameters) {
			initializer.expressions()
				.add(astRewrite.createCopyTarget(parameter.getParameter()));
		}
		arrayCreation.setInitializer(initializer);
		return arrayCreation;
	}

	@Override
	protected String getNewPreviousLiteralValue(ReplaceableParameter parameter) {
		String original = parameter.getPrevious()
			.getLiteralValue();
		return String.format("%s{%s}", original, parameter.getPosition()); //$NON-NLS-1$
	}
}
