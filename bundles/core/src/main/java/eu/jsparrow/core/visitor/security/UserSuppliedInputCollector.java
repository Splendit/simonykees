package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Here it is shown for cases where input between quotation marks is needed, for
 * example in dynamic SQL queries.
 * <p>
 * Note that this class will take part of the work which had been as yet done by
 * {@link AbstractQueryComponentsAnalyzer}.
 * 
 * @since 3.19.0
 *
 */
public class UserSuppliedInputCollector {

	private final boolean needStringLiteralAtEnd;
	private final Predicate<String> predicatePrevious;
	private final Predicate<String> predicateNext;

	public UserSuppliedInputCollector(boolean needStringLiteralAtEnd, Predicate<String> predicatePrevious,
			Predicate<String> predicateNext) {
		super();
		this.needStringLiteralAtEnd = needStringLiteralAtEnd;
		this.predicatePrevious = predicatePrevious;
		this.predicateNext = predicateNext;
	}

	List<UserSuppliedInput> collectUserSuppliedInput(List<Expression> components) {
		List<UserSuppliedInput> userSuppliedInputList = new ArrayList<>();
		for (int i = 0; i < components.size(); i++) {
			UserSuppliedInput userSuppliedInput = findUserSuppliedInput(components, i);
			if (userSuppliedInput != null) {
				userSuppliedInputList.add(userSuppliedInput);
			}
		}
		return userSuppliedInputList;
	}
	
	private UserSuppliedInput findUserSuppliedInput(List<Expression> components, int i) {

		Expression input = components.get(i);
		if (input.getNodeType() == ASTNode.STRING_LITERAL) {
			return null;
		}

		int iPrevious = i - 1;
		StringLiteral previous = findStringLiteral(components, iPrevious, predicatePrevious);
		if (previous == null) {
			return null;
		}

		int iNext = i + 1;
		StringLiteral next = findStringLiteral(components, iNext, predicateNext);
		if (next == null) {
			if (!needStringLiteralAtEnd && iNext >= components.size()) {
				return new UserSuppliedInput(previous, input, null);
			}
			return null;
		}
		return new UserSuppliedInput(previous, input, next);
	}

	private static StringLiteral findStringLiteral(List<Expression> components, int i, Predicate<String> predicate) {
		if (i < 0) {
			return null;
		}
		if (i >= components.size()) {
			return null;
		}
		Expression expression = components.get(i);
		if (expression.getNodeType() != ASTNode.STRING_LITERAL) {
			return null;
		}
		StringLiteral literal = (StringLiteral) expression;
		if (!predicate.test(literal.getLiteralValue())) {
			return null;
		}
		return literal;
	}

}
