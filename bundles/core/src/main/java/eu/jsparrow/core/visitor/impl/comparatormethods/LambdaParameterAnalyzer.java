package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class LambdaParameterAnalyzer {
	String identifierLeft;
	String identifierRight;

	boolean analyze(LambdaExpression lambda) {
		Pair<VariableDeclaration> lambdaParameterPair = Pair.fromNullableList(ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class));
		if(lambdaParameterPair.isEmpty()) {
			return false;
		}
		identifierLeft = lambdaParameterPair.getLeftHS().getName().getIdentifier();
		identifierRight = lambdaParameterPair.getRightHS().getName().getIdentifier();
	
		return true;
	}
	

}
