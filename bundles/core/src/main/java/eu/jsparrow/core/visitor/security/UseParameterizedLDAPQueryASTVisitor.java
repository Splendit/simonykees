package eu.jsparrow.core.visitor.security;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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

		return true;
	}

	public List<Expression> findDynamicQueryComponents(Expression filterExpression) {

		if (filterExpression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) filterExpression;
			DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
			componentStore.storeComponents(infixExpression);
			return componentStore.getComponents();
		}

		SqlVariableAnalyzerVisitor sqlVariableVisitor = createSqlVariableAnalyzerVisitor(filterExpression);
		if (sqlVariableVisitor == null) {
			return Collections.emptyList();
		}

		return sqlVariableVisitor.getDynamicQueryComponents();
	}

	@SuppressWarnings("unchecked")
	private ArrayCreation createSearchParameters(List<ReplaceableParameter> replaceableParameters) {
		AST ast = astRewrite.getAST();
		ArrayCreation arrayCreation = ast.newArrayCreation();

		SimpleType elementType = ast.newSimpleType(ast.newSimpleName(String.class.getSimpleName()));
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
