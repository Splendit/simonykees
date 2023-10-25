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

	DimensionsTransformationData(ExtraDimensionsToArrayData extraDimensionsToArrayData,
			Consumer<ArrayType> newArrayTypeSetter) {
		this.componentType = extraDimensionsToArrayData.getComponentType();
		this.totalDimensions = extraDimensionsToArrayData.getTotalDimensions();
		this.extraDimensionsList = extraDimensionsToArrayData.getExtraDimensionsList();
		this.newArrayTypeSetter = newArrayTypeSetter;
	}

	DimensionsTransformationData(Type componentType, int totalDimensions, List<Dimension> extraDimensionsList,
			Consumer<ArrayType> newArrayTypeSetter) {

		this.componentType = componentType;
		this.totalDimensions = totalDimensions;
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
