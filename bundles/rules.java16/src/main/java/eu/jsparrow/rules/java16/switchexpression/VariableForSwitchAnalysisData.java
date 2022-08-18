package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Stores informations about a variable which may be used in connection with the
 * transformation of an if statement to a switch statement or to a switch
 * expression.
 * <p>
 * Examples for operations where such a variable can be found:
 * <ul>
 * <li>Variable {@code x} in {@code x == 1}</li>
 * <li>Variable {@code s} in {@code "A".equals(s)}</li>
 * </ul>
 * 
 * @since 4.13.0
 * 
 */
public class VariableForSwitchAnalysisData {
	private final int operationNodeType;
	private final SimpleName variableForSwitch;
	private final ITypeBinding operandType;

	public VariableForSwitchAnalysisData(int operationNodeType, SimpleName variableForSwitch,
			ITypeBinding operandType) {
		this.operationNodeType = operationNodeType;
		this.variableForSwitch = variableForSwitch;
		this.operandType = operandType;
	}

	public int getOperationNodeType() {
		return operationNodeType;
	}

	public SimpleName getVariableForSwitch() {
		return variableForSwitch;
	}

	public ITypeBinding getOperandType() {
		return operandType;
	}

}
