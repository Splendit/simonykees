package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Contains all informations which are necessary to remove extra dimensions from
 * an ASTNode and increase the dimensions of a given Type by the number of extra
 * dimensions.
 * 
 * For Example, if a variable declaration fragment of a variable declaration
 * statement contains 2 extra dimensions, then these two extra dimensions can be
 * removed from the fragment and the Type of the corresponding
 * VariableDeclarationStatement can be changed to an ArrayType by adding these
 * two extra dimensions.
 *
 */
public class ExtraDimensionsToArrayData {
	private final Type componentType;
	private final int totalDimensions;
	private final List<Dimension> extraDimensionsList;

	static List<Dimension> collectSupportedExtraDimensions(VariableDeclaration variableDeclaration) {
		if (variableDeclaration.getExtraDimensions() == 0) {
			return Collections.emptyList();
		}

		List<Dimension> extraDimensionsList = ASTNodeUtil.convertToTypedList(variableDeclaration.extraDimensions(),
				Dimension.class);
		for (Dimension dimension : extraDimensionsList) {
			if (!dimension.annotations()
				.isEmpty()) {
				return Collections.emptyList();
			}
		}
		return extraDimensionsList;
	}

	static Optional<ExtraDimensionsToArrayData> findExtraDimensionsToArrayData(Type originalDeclarationType,
			VariableDeclaration variableDeclaration) {

		List<Dimension> supportedExtraDimensions = collectSupportedExtraDimensions(variableDeclaration);
		int extraDimensions = supportedExtraDimensions.size();
		if (extraDimensions >= 1) {
			if (originalDeclarationType.isArrayType()) {
				ArrayType arrayType = (ArrayType) originalDeclarationType;
				Type componentType = arrayType.getElementType();
				int totalDimensions = extraDimensions + arrayType.dimensions()
					.size();
				return Optional
					.of(new ExtraDimensionsToArrayData(componentType, totalDimensions,
							supportedExtraDimensions));
			}
			return Optional
				.of(new ExtraDimensionsToArrayData(originalDeclarationType, extraDimensions,
						supportedExtraDimensions));
		}

		return Optional.empty();
	}

	private ExtraDimensionsToArrayData(Type componentType, int totalDimensions,
			List<Dimension> extraDimensionsList) {
		this.componentType = componentType;
		this.totalDimensions = totalDimensions;
		this.extraDimensionsList = extraDimensionsList;
	}

	Type getComponentType() {
		return componentType;
	}

	int getTotalDimensions() {
		return totalDimensions;
	}

	List<Dimension> getExtraDimensionsList() {
		return extraDimensionsList;
	}
}
