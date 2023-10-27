package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.IterateMapEntrySetEvent;
import eu.jsparrow.core.visitor.impl.extradimensions.ExtraDimensionsAnalyzer.Result;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
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

			Map<VariableDeclarationFragment, ExtraDimensionsToArrayData> fragmentsWithExtraDimensions = result
				.getMultipleResults();

			List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
				.convertToTypedList(node.fragments(), VariableDeclarationFragment.class);

			if (!fragmentsWithExtraDimensions.isEmpty() && node.getLocationInParent() == Block.STATEMENTS_PROPERTY) {
				ChildListPropertyDescriptor locationInParent = Block.STATEMENTS_PROPERTY;

				ListRewrite statementsRewrite = astRewrite.getListRewrite(node.getParent(), locationInParent);
				Collections.reverse(variableDeclarationFragments);

				variableDeclarationFragments.forEach(fragment -> {

					VariableDeclarationStatement newVariableDeclarationStatement = (VariableDeclarationStatement) ASTNode
						.copySubtree(astRewrite.getAST(), node);
					VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
						.createMoveTarget(fragment);

					newVariableDeclarationStatement.fragments()
						.clear();
					ListRewrite variableRewrite = astRewrite.getListRewrite(newVariableDeclarationStatement,
							VariableDeclarationStatement.FRAGMENTS_PROPERTY);
					variableRewrite.insertLast(newFragment, null);

					if (fragmentsWithExtraDimensions.containsKey(fragment)) {
						ExtraDimensionsToArrayData extraDimensionsToArrayData = fragmentsWithExtraDimensions
							.get(fragment);
						transformDimensions(extraDimensionsToArrayData, newVariableDeclarationStatement, VariableDeclarationStatement.TYPE_PROPERTY);
					}
					statementsRewrite.insertAfter(newVariableDeclarationStatement, node, null);
				});
				astRewrite.remove(node, null);
			}
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
			// ----------------------------
			// Not implemented yet:
			// ----------------------------
			// case where within a multiple field declaration one
			// or more fragments with extra dimensions can be found.
			// In this case it is necessary to split the multiple variable
			// declaration statement in simple ones, each containing one
			// fragment, and then transform the declarations with extra
			// dimensions.
		}
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		ExtraDimensionsAnalyzer.findResult(node)
			.getSimpleResult()
			.ifPresent(this::transformDimensions);
		return true;
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