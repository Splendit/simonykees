package eu.jsparrow.rules.java16.javarecords;

import java.util.List;

import org.eclipse.jdt.core.dom.BodyDeclaration;

public class SupportedBodyDeclarations {

	private final List<RecordComponentData> recordComponentDataList;
	private final List<BodyDeclaration> recordBodyDeclarations;

	public SupportedBodyDeclarations(List<RecordComponentData> recordComponentDataList,
			List<BodyDeclaration> recordBodyDeclarations) {

		this.recordComponentDataList = recordComponentDataList;
		this.recordBodyDeclarations = recordBodyDeclarations;
	}

	public List<RecordComponentData> getRecordComponentDataList() {
		return recordComponentDataList;
	}

	public List<BodyDeclaration> getRecordBodyDeclarations() {
		return recordBodyDeclarations;
	}
}
