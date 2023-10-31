package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.IterateMapEntrySetEvent;
import eu.jsparrow.core.visitor.impl.extradimensions.ExtraDimensionsAnalyzer.MultipleResult;
import eu.jsparrow.core.visitor.impl.extradimensions.ExtraDimensionsAnalyzer.Result;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 4.20.0
 */
public class ArrayDesignatorsOnVariableNamesASTVisitor extends AbstractASTRewriteASTVisitor
		implements IterateMapEntrySetEvent {

	@Override
	public boolean visit(VariableDeclarationStatement node) {

		Result result = ExtraDimensionsAnalyzer.findResult(node);
		ExtraDimensionsToArrayData simpleResult = result.getSimpleResult()
			.orElse(null);

		if (simpleResult != null) {
			transformDimensions(simpleResult);

		} else {
			result.getMultipleResult()
				.ifPresent(multipleResult -> transformMultiple(node, multipleResult));
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Result result = ExtraDimensionsAnalyzer.findResult(node);
		ExtraDimensionsToArrayData simpleResult = result.getSimpleResult()
			.orElse(null);
		if (simpleResult != null) {
			transformDimensions(simpleResult);
		} else {
			result.getMultipleResult()
				.ifPresent(multipleResult -> transformMultiple(node, multipleResult));
		}
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		ExtraDimensionsAnalyzer.analyze(node)
			.ifPresent(this::transformDimensions);
		return true;
	}

	private void transformMultiple(ASTNode multipleDeclaration, MultipleResult multipleResult) {

		Map<VariableDeclaration, ExtraDimensionsToArrayData> fragmentsWithExtraDimensions = multipleResult
			.getFragmentsWithExtraDimensions();

		List<VariableDeclaration> variableDeclarationFragments = multipleResult.getDeclarationFragments();
		ChildListPropertyDescriptor locationInParent = multipleResult.getLocationInParent();
		ListRewrite statementsRewrite = astRewrite.getListRewrite(multipleDeclaration.getParent(), locationInParent);
		Collections.reverse(variableDeclarationFragments);

		variableDeclarationFragments.forEach(fragment -> {

			ASTNode newVariableDeclarationStatement = multipleResult.cloneDeclarationExcludingFragments();
			VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
				.createMoveTarget(fragment);

			ChildListPropertyDescriptor fragmentsProperty = multipleResult.getFragmentsProperty();
			ListRewrite variableRewrite = astRewrite.getListRewrite(newVariableDeclarationStatement,
					fragmentsProperty);
			variableRewrite.insertLast(newFragment, null);

			if (fragmentsWithExtraDimensions.containsKey(fragment)) {
				ExtraDimensionsToArrayData extraDimensionsToArrayData = fragmentsWithExtraDimensions
					.get(fragment);

				ChildPropertyDescriptor typeProperty = multipleResult.getTypeProperty();
				transformDimensions(extraDimensionsToArrayData, newVariableDeclarationStatement,
						typeProperty);
			}
			statementsRewrite.insertAfter(newVariableDeclarationStatement, multipleDeclaration, null);
		});
		astRewrite.remove(multipleDeclaration, null);
	}

	private void transformDimensions(ExtraDimensionsToArrayData transformationData, ASTNode newDeclarationSubtreeCopy,
			ChildPropertyDescriptor typeProperty) {
		Consumer<ArrayType> newArrayTypeSetter = arrayType -> newDeclarationSubtreeCopy
			.setStructuralProperty(typeProperty, arrayType);
		transformDimensions(transformationData, newArrayTypeSetter);
	}

	private void transformDimensions(ExtraDimensionsToArrayData transformationData) {
		Type originalDeclarationType = transformationData.getOriginalDeclarationType();
		Consumer<ArrayType> newArrayTypeSetter = newArrayType -> astRewrite.replace(originalDeclarationType,
				newArrayType, null);
		transformDimensions(transformationData, newArrayTypeSetter);
	}

	private void transformDimensions(ExtraDimensionsToArrayData transformationData,
			Consumer<ArrayType> newArrayTypeSetter) {
		Type componentType = transformationData.getComponentType();
		int totalDimensions = transformationData.getTotalDimensions();
		List<Dimension> extraDimensionsList = transformationData.getExtraDimensionsList();
		Type newComponentType = (Type) astRewrite.createCopyTarget(componentType);
		ArrayType newArrayType = astRewrite.getAST()
			.newArrayType(newComponentType, totalDimensions);
		newArrayTypeSetter.accept(newArrayType);
		extraDimensionsList.forEach(dimension -> astRewrite.remove(dimension, null));
		onRewrite();
	}
}