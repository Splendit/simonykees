package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class SupportedBodyDeclarations {

	private final List<RecordComponentData> recordComponentDataList;
	private final List<FieldDeclaration> staticFields;
	private final List<MethodDeclaration> methodDeclarations;

	public SupportedBodyDeclarations(List<RecordComponentData> recordComponentDataList,
			List<FieldDeclaration> staticFields, List<MethodDeclaration> methodDeclarations) {

		this.recordComponentDataList = recordComponentDataList;
		this.staticFields = staticFields;
		this.methodDeclarations = methodDeclarations;
	}

	public List<RecordComponentData> getRecordComponentDataList() {
		return recordComponentDataList;
	}

	public List<FieldDeclaration> getStaticFields() {
		return staticFields;
	}

	public List<MethodDeclaration> getMethodDeclarations() {
		return methodDeclarations;
	}
}
