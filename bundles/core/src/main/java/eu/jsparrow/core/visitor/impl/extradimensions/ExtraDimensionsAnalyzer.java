package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ExtraDimensionsAnalyzer {

	static Result findResult(SingleVariableDeclaration singleVariableDeclaration) {
		if (isContainingAnnotation(singleVariableDeclaration.getType())) {
			return Result.EMPTY_RESULT;
		}
		return findSimpleResult(singleVariableDeclaration.getType(), singleVariableDeclaration);
	}

	static Result findResult(VariableDeclarationStatement variableDeclarationStatement) {
		return findResult(variableDeclarationStatement.getType(),
				variableDeclarationStatement.fragments());
	}

	static Result findResult(FieldDeclaration fieldDeclaration) {
		return findResult(fieldDeclaration.getType(), fieldDeclaration.fragments());
	}

	private static Result findSimpleResult(Type type, VariableDeclaration variableDeclaration) {
		return ExtraDimensionsToArrayData
			.findExtraDimensionsToArrayData(type, variableDeclaration)
			.map(Result::new)
			.orElse(Result.EMPTY_RESULT);
	}

	private static Result findResult(
			Type type, @SuppressWarnings("rawtypes") List fragments) {

		if (isContainingAnnotation(type)) {
			return Result.EMPTY_RESULT;
		}

		VariableDeclarationFragment uniqueFragment = ASTNodeUtil
			.findSingletonListElement(fragments, VariableDeclarationFragment.class)
			.orElse(null);

		if (uniqueFragment != null) {
			return findSimpleResult(type, uniqueFragment);
		}

		Map<VariableDeclarationFragment, ExtraDimensionsToArrayData> map = new HashMap<>();
		ASTNodeUtil.convertToTypedList(fragments, VariableDeclarationFragment.class)
			.forEach(fragment -> ExtraDimensionsToArrayData.findExtraDimensionsToArrayData(type, fragment)
				.ifPresent(data -> map.put(fragment, data)));

		return new Result(map);
	}

	private static boolean isContainingAnnotation(Type type) {
		ContainingAnnotationVisitor firstAnnotationVisitor = new ContainingAnnotationVisitor();
		type.accept(firstAnnotationVisitor);
		return firstAnnotationVisitor.isContainingAnnotation();
	}

	public static class Result {
		private static final Result EMPTY_RESULT = new Result(Collections.emptyMap());
		private ExtraDimensionsToArrayData simpleResult;
		private Map<VariableDeclarationFragment, ExtraDimensionsToArrayData> multipleResults;

		private Result(ExtraDimensionsToArrayData simpleResult) {
			this.simpleResult = simpleResult;
		}

		private Result(Map<VariableDeclarationFragment, ExtraDimensionsToArrayData> multipleResults) {
			this.multipleResults = multipleResults;
		}

		Optional<ExtraDimensionsToArrayData> getSimpleResult() {
			return Optional.ofNullable(simpleResult);
		}

		Map<VariableDeclarationFragment, ExtraDimensionsToArrayData> getMultipleResults() {
			if (multipleResults == null) {
				return Collections.emptyMap();
			}
			return multipleResults;
		}

	}

	/**
	 * private default constructor hiding implicit public one
	 */
	private ExtraDimensionsAnalyzer() {
	}

}
