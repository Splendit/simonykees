package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
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

		if (variableDeclarationFragments.size() > 1) {
			// ----------------------------
			// Not implemented yet:
			// ----------------------------
			// case where within a multiple variable declaration statement one
			// or more fragments with extra dimensions can be found.
			// In this case it is necessary to split the multiple variable
			// declaration statement in simple ones, each containing one
			// fragment, and then transform the declarations with extra
			// dimensions.
		} else {
			findDimensionsTransformationData(node).ifPresent(this::transform);
		}

		return true;
	}
	
	
	@Override
	public boolean visit(FieldDeclaration node) {
		List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
			.convertToTypedList(node.fragments(), VariableDeclarationFragment.class);

		if (variableDeclarationFragments.size() > 1) {
			// ----------------------------
			// Not implemented yet:
			// ----------------------------
			// case where within a multiple variable declaration statement one
			// or more fragments with extra dimensions can be found.
			// In this case it is necessary to split the multiple variable
			// declaration statement in simple ones, each containing one
			// fragment, and then transform the declarations with extra
			// dimensions.
		} else {
			// ----------------------------
			// Not implemented yet:
			// ----------------------------
			// findDimensionsTransformationData(node).ifPresent(this::transform);
		}

		return true;
	}

	Optional<DimensionsTransformationData> findDimensionsTransformationData(VariableDeclarationStatement node) {
		List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
			.convertToTypedList(node.fragments(), VariableDeclarationFragment.class);

		if (variableDeclarationFragments.size() == 1) {
			VariableDeclarationFragment fragment = variableDeclarationFragments.get(0);
			int extraDimensions = fragment.getExtraDimensions();
			if (extraDimensions > 0) {
				Type type = node.getType();
				Type componentType;
				int totalDimensions;
				if (type.isArrayType()) {
					ArrayType arrayType = (ArrayType) type;
					componentType = arrayType.getElementType();
					totalDimensions = extraDimensions + arrayType.getDimensions();
				} else {
					componentType = type;
					totalDimensions = extraDimensions;
				}
				List<Dimension> extraDimensionsList = ASTNodeUtil.convertToTypedList(fragment.extraDimensions(),
						Dimension.class);
				return Optional
					.of(new DimensionsTransformationData(componentType, totalDimensions, extraDimensionsList, type));
			}

		}
		return Optional.empty();
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
	}

}
