package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ExtraDimensionsAnalyzer {

	static Optional<ExtraDimensionsToArrayData> analyze(SingleVariableDeclaration singleVariableDeclaration) {
		Type type = singleVariableDeclaration.getType();
		if (isContainingAnnotation(type)) {
			return Optional.empty();
		}
		return ExtraDimensionsToArrayData.findExtraDimensionsToArrayData(type, singleVariableDeclaration);
	}

	static Result findResult(VariableDeclarationStatement variableDeclarationStatement) {
		return findResult(variableDeclarationStatement.getType(),
				variableDeclarationStatement.fragments(),
				() -> findPropertiesConfiguration(variableDeclarationStatement),
				() -> cloneDeclarationWithoutFragments(variableDeclarationStatement));
	}

	static Result findResult(FieldDeclaration fieldDeclaration) {
		return findResult(fieldDeclaration.getType(), fieldDeclaration.fragments(),
				() -> findPropertiesConfiguration(fieldDeclaration),
				() -> cloneDeclarationWithoutFragments(fieldDeclaration));
	}

	private static Result findResult(Type type, @SuppressWarnings("rawtypes") List fragmentRawList,
			Supplier<Optional<MultipleTransformationProperties>> propertiesSupplier,
			Supplier<ASTNode> cloneDeclarationWithoutFragmentsLambda) {

		if (isContainingAnnotation(type)) {
			return Result.EMPTY_RESULT;
		}

		List<VariableDeclaration> fragmentList = ASTNodeUtil.returnTypedList(fragmentRawList,
				VariableDeclaration.class);

		if (fragmentList.size() == 1) {
			return ExtraDimensionsToArrayData.findExtraDimensionsToArrayData(type, fragmentList.get(0))
				.map(Result::new)
				.orElse(Result.EMPTY_RESULT);
		}

		Map<VariableDeclaration, ExtraDimensionsToArrayData> fragmentsWithExtraDimensions = new HashMap<>();
		fragmentList
			.forEach(fragment -> ExtraDimensionsToArrayData.findExtraDimensionsToArrayData(type, fragment)
				.ifPresent(data -> fragmentsWithExtraDimensions.put(fragment, data)));

		if (fragmentsWithExtraDimensions.isEmpty()) {
			return Result.EMPTY_RESULT;
		}
		MultipleTransformationProperties properties = propertiesSupplier.get()
			.orElse(null);

		if (properties == null) {
			return Result.EMPTY_RESULT;
		}

		MultipleResult multipleResult = new MultipleResult(fragmentList, fragmentsWithExtraDimensions,
				cloneDeclarationWithoutFragmentsLambda, properties);
		return new Result(multipleResult);
	}

	private static boolean isContainingAnnotation(Type type) {
		ContainingAnnotationVisitor firstAnnotationVisitor = new ContainingAnnotationVisitor();
		type.accept(firstAnnotationVisitor);
		return firstAnnotationVisitor.isContainingAnnotation();
	}

	private static Optional<MultipleTransformationProperties> findPropertiesConfiguration(
			VariableDeclarationStatement variableDeclarationStatement) {

		if (variableDeclarationStatement.getLocationInParent() == Block.STATEMENTS_PROPERTY) {
			return Optional
				.of(new MultipleTransformationProperties(variableDeclarationStatement, Block.STATEMENTS_PROPERTY));
		}

		return Optional.empty();
	}

	private static Optional<MultipleTransformationProperties> findPropertiesConfiguration(
			FieldDeclaration fieldDeclaration) {

		ChildListPropertyDescriptor locationInParent;
		if (fieldDeclaration.getLocationInParent() == TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
			locationInParent = TypeDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else if (fieldDeclaration.getLocationInParent() == EnumDeclaration.BODY_DECLARATIONS_PROPERTY) {
			locationInParent = EnumDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else if (fieldDeclaration.getLocationInParent() == RecordDeclaration.BODY_DECLARATIONS_PROPERTY) {
			locationInParent = RecordDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else if (fieldDeclaration.getLocationInParent() == AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY) {
			locationInParent = AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else {
			return Optional.empty();
		}

		return Optional
			.of(new MultipleTransformationProperties(fieldDeclaration, locationInParent));

	}

	private static VariableDeclarationStatement cloneDeclarationWithoutFragments(
			VariableDeclarationStatement variableDeclarationStatement) {
		VariableDeclarationStatement newVariableDeclarationStatement = (VariableDeclarationStatement) ASTNode
			.copySubtree(variableDeclarationStatement.getAST(), variableDeclarationStatement);
		newVariableDeclarationStatement.fragments()
			.clear();
		return newVariableDeclarationStatement;
	}

	private static FieldDeclaration cloneDeclarationWithoutFragments(
			FieldDeclaration fieldDeclaration) {
		FieldDeclaration newVariableDeclarationStatement = (FieldDeclaration) ASTNode
			.copySubtree(fieldDeclaration.getAST(), fieldDeclaration);
		newVariableDeclarationStatement.fragments()
			.clear();
		return newVariableDeclarationStatement;
	}

	public static class Result {
		private static final Result EMPTY_RESULT = new Result();
		private ExtraDimensionsToArrayData simpleResult;
		private MultipleResult multipleResult;

		private Result() {
		}

		private Result(ExtraDimensionsToArrayData simpleResult) {
			this.simpleResult = simpleResult;
		}

		private Result(MultipleResult multipleResult) {
			this.multipleResult = multipleResult;
		}

		Optional<ExtraDimensionsToArrayData> getSimpleResult() {
			return Optional.ofNullable(simpleResult);
		}

		Optional<MultipleResult> getMultipleResult() {
			return Optional.ofNullable(multipleResult);
		}
	}

	static class MultipleResult {
		private final List<VariableDeclaration> declarationFragments;
		private final Map<VariableDeclaration, ExtraDimensionsToArrayData> fragmentsWithExtraDimensions;
		private final MultipleTransformationProperties transformationProperties;
		private final Supplier<ASTNode> cloneDeclarationWithoutFragmentsLambda;

		MultipleResult(List<VariableDeclaration> declarationFragments,
				Map<VariableDeclaration, ExtraDimensionsToArrayData> fragmentsWithExtraDimensions,
				Supplier<ASTNode> cloneDeclarationWithoutFragmentsLambda,
				MultipleTransformationProperties transformationProperties) {
			this.declarationFragments = declarationFragments;
			this.fragmentsWithExtraDimensions = fragmentsWithExtraDimensions;
			this.cloneDeclarationWithoutFragmentsLambda = cloneDeclarationWithoutFragmentsLambda;
			;
			this.transformationProperties = transformationProperties;
		}

		List<VariableDeclaration> getDeclarationFragments() {
			return declarationFragments;
		}

		Map<VariableDeclaration, ExtraDimensionsToArrayData> getFragmentsWithExtraDimensions() {
			return fragmentsWithExtraDimensions;
		}

		ChildListPropertyDescriptor getLocationInParent() {
			return transformationProperties.getLocationInParent();
		}

		ChildListPropertyDescriptor getFragmentsProperty() {
			return transformationProperties.getFragmentsProperty();
		}

		ChildPropertyDescriptor getTypeProperty() {
			return transformationProperties.getTypeProperty();
		}
		
		ASTNode cloneDeclarationExcludingFragments() {
			return cloneDeclarationWithoutFragmentsLambda.get();
		}
	}

	static class MultipleTransformationProperties {
		private final ChildListPropertyDescriptor locationInParent;
		private final ChildListPropertyDescriptor fragmentsProperty;
		private final ChildPropertyDescriptor typeProperty;

		private MultipleTransformationProperties(VariableDeclarationStatement variableDeclarationStatement,
				ChildListPropertyDescriptor locationInParent) {
			this.locationInParent = locationInParent;
			this.fragmentsProperty = VariableDeclarationStatement.FRAGMENTS_PROPERTY;
			this.typeProperty = VariableDeclarationStatement.TYPE_PROPERTY;
		}

		private MultipleTransformationProperties(FieldDeclaration fieldDeclaration,
				ChildListPropertyDescriptor locationInParent) {
			this.locationInParent = locationInParent;
			this.fragmentsProperty = FieldDeclaration.FRAGMENTS_PROPERTY;
			this.typeProperty = FieldDeclaration.TYPE_PROPERTY;
		}

		ChildListPropertyDescriptor getLocationInParent() {
			return locationInParent;
		}

		ChildListPropertyDescriptor getFragmentsProperty() {
			return fragmentsProperty;
		}

		ChildPropertyDescriptor getTypeProperty() {
			return typeProperty;
		}
	}

	/**
	 * private default constructor hiding implicit public one
	 */
	private ExtraDimensionsAnalyzer() {
	}

}
