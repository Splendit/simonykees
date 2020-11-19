package eu.jsparrow.core.visitor.impl.comparatormethods;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * Stores all informations which are necessary for the transformation of a given
 * lambda representing a {@link java.util.Comparator}.
 * 
 * @since 3.23.0
 */
class LambdaAnalysisResult {

	private final VariableDeclaration lambdaParameterLeftHS;
	private final boolean reversed;
	private final IMethodBinding comparisonKeyMethodName;
	private CastExpression parentCastExpression;

	public LambdaAnalysisResult(VariableDeclaration lambdaParameterLeftHS,
			boolean reversed) {
		this.lambdaParameterLeftHS = lambdaParameterLeftHS;
		this.comparisonKeyMethodName = null;
		this.reversed = reversed;
	}

	public LambdaAnalysisResult(VariableDeclaration lambdaParameterLeftHS,
			IMethodBinding comparisonKeyMethodName, boolean reversed) {
		this.lambdaParameterLeftHS = lambdaParameterLeftHS;
		this.comparisonKeyMethodName = comparisonKeyMethodName;
		this.reversed = reversed;
	}

	boolean isReversed() {
		return reversed;
	}

	Optional<IMethodBinding> getComparisonKeyMethod() {
		return Optional.ofNullable(comparisonKeyMethodName);
	}

	String getFirstLambdaParameterIdentifier() {
		return lambdaParameterLeftHS.getName()
			.getIdentifier();
	}

	Optional<Type> getExplicitLambdaParameterType() {
		if (lambdaParameterLeftHS
			.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			return Optional.of(((SingleVariableDeclaration) lambdaParameterLeftHS).getType());
		}
		return Optional.empty();
	}

	ITypeBinding getImplicitLambdaParameterType() {
		return lambdaParameterLeftHS.resolveBinding()
			.getType();
	}

	Optional<CastExpression> getParentCastExpression() {
		return Optional.ofNullable(parentCastExpression);
	}

	public void setParentCastExpression(CastExpression parentCastExpression) {
		this.parentCastExpression = parentCastExpression;
	}
}
