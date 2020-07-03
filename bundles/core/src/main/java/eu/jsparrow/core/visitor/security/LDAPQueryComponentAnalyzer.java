package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class LDAPQueryComponentAnalyzer extends AbstractQueryComponentsAnalyzer {

	LDAPQueryComponentAnalyzer(List<Expression> components) {
		super(components, 0);
	}

	@Override
	protected ReplaceableParameter createReplaceableParameter(int componentIndex, int parameterPosition) {
		StringLiteral previous = findPrevious(componentIndex);
		if(previous == null) {
			return null;
		}
		StringLiteral next = findNext(componentIndex);
		if(next == null) {
			return null;
		}
		Expression nonLiteralComponent = components.get(componentIndex);
		ITypeBinding nonLiteralTypeBinding = nonLiteralComponent.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(nonLiteralTypeBinding, java.lang.String.class.getName());
		if(!isString) {
			return null;
		}
		return new ReplaceableParameter(previous, next, nonLiteralComponent, parameterPosition);
	}
	
	@Override
	protected boolean isValidPrevious(StringLiteral literal) {
		return literal.getLiteralValue()
			.endsWith("="); //$NON-NLS-1$
	}
	
	@Override
	protected boolean isValidNext(StringLiteral literal) {
		return literal.getLiteralValue().startsWith(")"); //$NON-NLS-1$
	}
	

}
