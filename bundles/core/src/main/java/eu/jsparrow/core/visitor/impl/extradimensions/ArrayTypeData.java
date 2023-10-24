package eu.jsparrow.core.visitor.impl.extradimensions;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Type;

public class ArrayTypeData {

	private final Type componentType;
	private final int dimensions;

	public static ArrayTypeData createArrayTypeData(Type type, int extraDimensions) {

		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			Type componentType = arrayType.getElementType();
			int totalDimensions = extraDimensions + arrayType.getDimensions();
			return new ArrayTypeData(componentType, totalDimensions);
		}
		return new ArrayTypeData(type, extraDimensions);
	}

	private ArrayTypeData(Type componentType, int dimensions) {
		this.componentType = componentType;
		this.dimensions = dimensions;
	}

	Type getComponentType() {
		return componentType;
	}

	int getDimensions() {
		return dimensions;
	}

}
