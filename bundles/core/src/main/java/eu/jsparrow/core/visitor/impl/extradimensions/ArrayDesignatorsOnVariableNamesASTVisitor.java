package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.IterateMapEntrySetEvent;
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
		List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
			.convertToTypedList(node.fragments(), VariableDeclarationFragment.class);

		if (variableDeclarationFragments.size() == 1) {
			findDimensionsTransformationData(node.getType(), variableDeclarationFragments.get(0))
				.ifPresent(this::transformDimensions);

		} else {
			boolean containsFragmentWithExtraDimension = variableDeclarationFragments.stream()
				.anyMatch(fragment -> !fragment.extraDimensions()
					.isEmpty());
			if (containsFragmentWithExtraDimension && node.getLocationInParent() == Block.STATEMENTS_PROPERTY) {
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

					int extraDimensions = fragment.getExtraDimensions();
					if (extraDimensions > 0) {
						ArrayTypeData arrayTypeData = ArrayTypeData.createArrayTypeData(node.getType(),
								extraDimensions);
						Consumer<ArrayType> newArrayTypeSetter = newVariableDeclarationStatement::setType;
						List<Dimension> extraDimensionsList = ASTNodeUtil.convertToTypedList(
								fragment.extraDimensions(),
								Dimension.class);

						DimensionsTransformationData dimensionsTransformationData = new DimensionsTransformationData(
								arrayTypeData, extraDimensionsList, newArrayTypeSetter);
						transformDimensions(dimensionsTransformationData);
					}

					statementsRewrite.insertAfter(newVariableDeclarationStatement, node, null);
				});

				astRewrite.remove(node, null);
				onRewrite();

			}
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
			.convertToTypedList(node.fragments(), VariableDeclarationFragment.class);

		if (variableDeclarationFragments.size() == 1) {
			findDimensionsTransformationData(node.getType(), variableDeclarationFragments.get(0))
				.ifPresent(this::transformDimensions);
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
		findDimensionsTransformationData(node.getType(), node).ifPresent(this::transformDimensions);
		return true;
	}

	private boolean isTypeContainingAnnotation(Type type) {
		ContainingAnnotationVisitor firstAnnotationVisitor = new ContainingAnnotationVisitor();
		type.accept(firstAnnotationVisitor);
		return firstAnnotationVisitor.isContainingAnnotation();
	}

	private boolean isExtraDimensionContainingAnnotation(List<Dimension> extraDimensionsList) {
		for (Dimension dimension : extraDimensionsList) {
			if (!dimension.annotations()
				.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private Optional<DimensionsTransformationData> findDimensionsTransformationData(Type typeToReplace,
			VariableDeclaration variableDeclaration) {

		int extraDimensions = variableDeclaration.getExtraDimensions();
		if (extraDimensions < 1) {
			return Optional.empty();
		}

		if (isTypeContainingAnnotation(typeToReplace)) {
			return Optional.empty();
		}

		List<Dimension> extraDimensionsList = ASTNodeUtil.convertToTypedList(
				variableDeclaration.extraDimensions(),
				Dimension.class);

		if (isExtraDimensionContainingAnnotation(extraDimensionsList)) {
			return Optional.empty();
		}
		ArrayTypeData arrayTypeData = ArrayTypeData.createArrayTypeData(typeToReplace, extraDimensions);

		Consumer<ArrayType> newArrayTypeSetter = newArrayType -> astRewrite.replace(typeToReplace, newArrayType, null);
		return Optional
			.of(new DimensionsTransformationData(arrayTypeData, extraDimensionsList, newArrayTypeSetter));

	}

	private void transformDimensions(DimensionsTransformationData transformationData) {
		Type componentType = transformationData.getComponentType();
		int totalDimensions = transformationData.getTotalDimensions();
		List<Dimension> extraDimensionsList = transformationData.getExtraDimensionsList();
		Type newComponentType = (Type) astRewrite.createCopyTarget(componentType);
		ArrayType newArrayType = astRewrite.getAST()
			.newArrayType(newComponentType, totalDimensions);
		transformationData.getNewArrayTypeSetter()
			.accept(newArrayType);
		extraDimensionsList.forEach(dimension -> astRewrite.remove(dimension, null));
		onRewrite();
	}
}
