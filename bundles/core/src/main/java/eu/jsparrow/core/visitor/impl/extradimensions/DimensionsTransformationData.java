package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.List;

import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Type;

public class DimensionsTransformationData {

	private final Type componentType;
	private final int totalDimensions;
	private final List<Dimension> extraDimensionsList;
	private final Type typeToReplace;

	DimensionsTransformationData(Type componentType, int totalDimensions, List<Dimension> extraDimensionsList,
			Type typeToReplace) {

		this.componentType = componentType;
		this.totalDimensions = totalDimensions;
		this.extraDimensionsList = extraDimensionsList;
		this.typeToReplace = typeToReplace;

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

	Type getTypeToReplace() {
		return typeToReplace;
	}

}
