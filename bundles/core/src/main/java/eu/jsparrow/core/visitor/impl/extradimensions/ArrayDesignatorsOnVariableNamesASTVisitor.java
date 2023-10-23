package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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
				.ifPresent(this::transform);

		} else {
			// ----------------------------
			// Not implemented yet:
			// ----------------------------
			// case where within a multiple variable declaration statement one
			// or more fragments with extra dimensions can be found.
			// In this case it is necessary to split the multiple variable
			// declaration statement in simple ones, each containing one
			// fragment, and then transform the declarations with extra
			// dimensions.
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
			.convertToTypedList(node.fragments(), VariableDeclarationFragment.class);

		if (variableDeclarationFragments.size() == 1) {
			findDimensionsTransformationData(node.getType(), variableDeclarationFragments.get(0))
				.ifPresent(this::transform);

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
		findDimensionsTransformationData(node.getType(), node).ifPresent(this::transform);
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

		Type componentType;
		int totalDimensions;
		if (typeToReplace.isArrayType()) {
			ArrayType arrayType = (ArrayType) typeToReplace;
			componentType = arrayType.getElementType();
			totalDimensions = extraDimensions + arrayType.getDimensions();
		} else {
			componentType = typeToReplace;
			totalDimensions = extraDimensions;
		}

		return Optional
			.of(new DimensionsTransformationData(componentType, totalDimensions, extraDimensionsList, typeToReplace));

	}

	private void transform(DimensionsTransformationData transformationData) {
		Type componentType = transformationData.getComponentType();
		int totalDimensions = transformationData.getTotalDimensions();
		List<Dimension> extraDimensionsList = transformationData.getExtraDimensionsList();
		Type typeToReplace = transformationData.getTypeToReplace();
		Type newComponentType = (Type) astRewrite.createCopyTarget(componentType);
		ArrayType newArrayType = astRewrite.getAST()
			.newArrayType(newComponentType, totalDimensions);
		astRewrite.replace(typeToReplace, newArrayType, null);
		extraDimensionsList.forEach(dimension -> astRewrite.remove(dimension, null));
		onRewrite();
	}

}
