package eu.jsparrow.core.visitor.impl.extradimensions;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Type;

public class DimensionsTransformationData {

	private final Type componentType;
	private final int totalDimensions;
	private final List<Dimension> extraDimensionsList;
	private final Consumer<ArrayType> newArrayTypeSetter;

	DimensionsTransformationData(ArrayTypeData arrayTypeData, List<Dimension> extraDimensionsList,
			Consumer<ArrayType> newArrayTypeSetter) {

		this.componentType = arrayTypeData.getComponentType();
		this.totalDimensions = arrayTypeData.getDimensions();
		this.extraDimensionsList = extraDimensionsList;
		this.newArrayTypeSetter = newArrayTypeSetter;

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

	Consumer<ArrayType> getNewArrayTypeSetter() {
		return newArrayTypeSetter;
	}
}
