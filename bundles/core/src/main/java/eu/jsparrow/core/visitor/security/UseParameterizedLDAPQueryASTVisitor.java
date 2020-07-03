package eu.jsparrow.core.visitor.security;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

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

}
